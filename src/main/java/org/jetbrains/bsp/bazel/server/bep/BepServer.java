package org.jetbrains.bsp.bazel.server.bep;

import ch.epfl.scala.bsp4j.BuildClient;
import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import ch.epfl.scala.bsp4j.Diagnostic;
import ch.epfl.scala.bsp4j.PublishDiagnosticsParams;
import ch.epfl.scala.bsp4j.SourceItem;
import ch.epfl.scala.bsp4j.StatusCode;
import ch.epfl.scala.bsp4j.TaskFinishParams;
import ch.epfl.scala.bsp4j.TaskId;
import ch.epfl.scala.bsp4j.TaskStartParams;
import ch.epfl.scala.bsp4j.TextDocumentIdentifier;
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos;
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos.OutputGroup;
import com.google.devtools.build.v1.BuildEvent;
import com.google.devtools.build.v1.PublishBuildEventGrpc;
import com.google.devtools.build.v1.PublishBuildToolEventStreamRequest;
import com.google.devtools.build.v1.PublishBuildToolEventStreamResponse;
import com.google.devtools.build.v1.PublishLifecycleEventRequest;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.bsp.bazel.common.Constants;
import org.jetbrains.bsp.bazel.common.Uri;
import org.jetbrains.bsp.bazel.server.data.BazelData;
import org.jetbrains.bsp.bazel.server.logger.BuildClientLogger;
import org.jetbrains.bsp.bazel.server.util.ParsingUtils;

public class BepServer extends PublishBuildEventGrpc.PublishBuildEventImplBase {

  private static final Logger LOGGER = LogManager.getLogger(BepServer.class);

  private static final int URI_PREFIX_LENGTH = 7;

  private final BuildClient bspClient;
  private final BuildClientLogger buildClientLogger;

  private final BazelData bazelData;
  private final BepDiagnosticsDispatcher diagnosticsDispatcher;

  private final Set<Uri> compilerClasspath = new TreeSet<>();
  private final Deque<TaskId> startedEventTaskIds = new ArrayDeque<>();
  private final Map<String, String> diagnosticsProtosLocations = new HashMap<>();
  private final Map<String, BuildEventStreamProtos.NamedSetOfFiles> namedSetsOfFiles =
      new HashMap<>();

  public BepServer(
      BazelData bazelData, BuildClient bspClient, BuildClientLogger buildClientLogger) {
    this.bazelData = bazelData;
    this.bspClient = bspClient;
    this.buildClientLogger = buildClientLogger;
    this.diagnosticsDispatcher = new BepDiagnosticsDispatcher(bazelData, bspClient);
  }

  @Override
  public void publishLifecycleEvent(
      PublishLifecycleEventRequest request, StreamObserver<Empty> responseObserver) {
    namedSetsOfFiles.clear();
    responseObserver.onNext(Empty.getDefaultInstance());
    responseObserver.onCompleted();
  }

  @Override
  public StreamObserver<PublishBuildToolEventStreamRequest> publishBuildToolEventStream(
      StreamObserver<PublishBuildToolEventStreamResponse> responseObserver) {
    return new BepStreamObserver(this, responseObserver);
  }

  public Map<Uri, List<PublishDiagnosticsParams>> collectDiagnostics(
      BuildTargetIdentifier target, String diagnosticsLocation) {
    return diagnosticsDispatcher.collectDiagnostics(target, diagnosticsLocation);
  }

  public void emitDiagnostics(
      Map<Uri, List<PublishDiagnosticsParams>> filesToDiagnostics, BuildTargetIdentifier target) {
    diagnosticsDispatcher.emitDiagnostics(filesToDiagnostics, target);
  }

  public void handleEvent(BuildEvent buildEvent) {
    try {
      BuildEventStreamProtos.BuildEvent event =
          BuildEventStreamProtos.BuildEvent.parseFrom(buildEvent.getBazelEvent().getValue());

      LOGGER.info("Got event {}", event);

      processBuildStartedEvent(event);
      processFinishedEvent(event);
      fetchNamedSet(event);
      processCompletedEvent(event);
      processActionEvent(event);
      processAbortedEvent(event);
      processProgressEvent(event);
    } catch (IOException e) {
      LOGGER.error("Error deserializing BEP proto: {}", e.toString());
    }
  }

  private void fetchNamedSet(BuildEventStreamProtos.BuildEvent event) {
    if (event.getId().hasNamedSet()) {
      namedSetsOfFiles.put(event.getId().getNamedSet().getId(), event.getNamedSetOfFiles());
    }
  }

  private void processBuildStartedEvent(BuildEventStreamProtos.BuildEvent event) {
    if (event.hasStarted()
        && event.getStarted().getCommand().equals(Constants.BAZEL_BUILD_COMMAND)) {
      consumeBuildStartedEvent(event.getStarted());
    }
  }

  private void consumeBuildStartedEvent(BuildEventStreamProtos.BuildStarted buildStarted) {
    TaskId taskId = new TaskId(buildStarted.getUuid());
    TaskStartParams startParams = new TaskStartParams(taskId);
    startParams.setEventTime(buildStarted.getStartTimeMillis());

    bspClient.onBuildTaskStart(startParams);
    startedEventTaskIds.push(taskId);
  }

  private void processFinishedEvent(BuildEventStreamProtos.BuildEvent event) {
    if (event.hasFinished()) {
      consumeFinishedEvent(event.getFinished());
    }
  }

  private void consumeFinishedEvent(BuildEventStreamProtos.BuildFinished buildFinished) {
    if (startedEventTaskIds.isEmpty()) {
      LOGGER.info("No start event id was found.");
      return;
    }

    if (startedEventTaskIds.size() > 1) {
      LOGGER.info("More than 1 start event was found");
      return;
    }

    StatusCode exitCode = ParsingUtils.parseExitCode(buildFinished.getExitCode().getCode());
    TaskFinishParams finishParams = new TaskFinishParams(startedEventTaskIds.pop(), exitCode);
    finishParams.setEventTime(buildFinished.getFinishTimeMillis());

    bspClient.onBuildTaskFinish(finishParams);
  }

  private void processCompletedEvent(BuildEventStreamProtos.BuildEvent event) {
    if (event.hasCompleted()) {
      consumeCompletedEvent(event.getCompleted());
    }
  }

  private void consumeCompletedEvent(BuildEventStreamProtos.TargetComplete targetComplete) {
    List<OutputGroup> outputGroups = targetComplete.getOutputGroupList();
    if (outputGroups.size() == 1) {
      OutputGroup outputGroup = outputGroups.get(0);
      if (outputGroup.getName().equals(Constants.SCALA_COMPILER_CLASSPATH_FILES)) {
        fetchScalaJars(outputGroup);
      }
    }
  }

  private void fetchScalaJars(OutputGroup outputGroup) {
    outputGroup.getFileSetsList().stream()
        .flatMap(fileSetId -> namedSetsOfFiles.get(fileSetId.getId()).getFilesList().stream())
        .map(file -> ParsingUtils.parseUri(file.getUri()))
        .flatMap(pathProtoUri -> ParsingUtils.parseClasspathFromAspect(pathProtoUri).stream())
        .forEach(
            path ->
                compilerClasspath.add(
                    Uri.fromExecPath(Constants.EXEC_ROOT_PREFIX + path, bazelData.getExecRoot())));
  }

  private void processActionEvent(BuildEventStreamProtos.BuildEvent event) {
    if (event.hasAction()) {
      consumeActionEvent(event.getAction());
    }
  }

  private void consumeActionEvent(BuildEventStreamProtos.ActionExecuted action) {
    if (!Constants.SUPPORTED_COMPILERS.contains(action.getType())) {
      // Ignore file template writes and such.
      // TODO(illicitonion): Maybe include them as task notifications (rather than diagnostics).
      return;
    }

    BuildTargetIdentifier target = new BuildTargetIdentifier(action.getLabel());

    Map<Uri, List<PublishDiagnosticsParams>> filesToDiagnostics =
        action.getActionMetadataLogsList().stream()
            .filter(log -> log.getName().equals(Constants.DIAGNOSTICS))
            .peek(log -> LOGGER.info("Found diagnostics file in {}", log.getUri()))
            .map(
                log ->
                    diagnosticsDispatcher.collectDiagnostics(
                        target, log.getUri().substring(URI_PREFIX_LENGTH)))
            .collect(HashMap::new, Map::putAll, Map::putAll);

    if (hasDiagnosticsOutput(target)) {
      if (filesToDiagnostics.isEmpty()) {
        filesToDiagnostics =
            diagnosticsDispatcher.collectDiagnostics(
                target, diagnosticsProtosLocations.get(target.getUri()));
      }
      diagnosticsProtosLocations.remove(target.getUri());
    }

    diagnosticsDispatcher.emitDiagnostics(filesToDiagnostics, target);
  }

  private boolean hasDiagnosticsOutput(BuildTargetIdentifier target) {
    return diagnosticsProtosLocations.containsKey(target.getUri());
  }

  private void processAbortedEvent(BuildEventStreamProtos.BuildEvent event) {
    if (event.hasAborted()) {
      consumeAbortedEvent(event.getAborted());
    }
  }

  private void consumeAbortedEvent(BuildEventStreamProtos.Aborted aborted) {
    if (aborted.getReason() != BuildEventStreamProtos.Aborted.AbortReason.NO_BUILD) {
      buildClientLogger.logError(
          "Command aborted with reason " + aborted.getReason() + ": " + aborted.getDescription());
    }
  }

  private void processProgressEvent(BuildEventStreamProtos.BuildEvent event) {
    if (event.hasProgress()) {
      consumeProgressEvent(event.getProgress());
    }
  }

  private void consumeProgressEvent(BuildEventStreamProtos.Progress progress) {
    Map<String, List<Diagnostic>> fileDiagnostics =
        ParsingUtils.parseStderrDiagnostics(progress.getStderr());

    fileDiagnostics.entrySet().stream()
        .map(this::createParamsFromEntry)
        .forEach(bspClient::onBuildPublishDiagnostics);

    buildClientLogger.logMessage(progress.getStderr().trim());
  }

  private PublishDiagnosticsParams createParamsFromEntry(
      Map.Entry<String, List<Diagnostic>> entry) {
    String fileLocation = entry.getKey();
    List<Diagnostic> diagnostics = entry.getValue();

    return new PublishDiagnosticsParams(
        new TextDocumentIdentifier(Uri.fromAbsolutePath(fileLocation).toString()),
        new BuildTargetIdentifier(""),
        diagnostics,
        false);
  }

  public Set<Uri> getCompilerClasspath() {
    return compilerClasspath;
  }

  public Map<String, String> getDiagnosticsProtosLocations() {
    return diagnosticsProtosLocations;
  }

  public Map<BuildTargetIdentifier, List<SourceItem>> getBuildTargetsSources() {
    return diagnosticsDispatcher.getBuildTargetsSources();
  }
}
