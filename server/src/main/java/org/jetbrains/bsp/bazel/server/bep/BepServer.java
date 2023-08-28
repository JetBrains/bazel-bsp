package org.jetbrains.bsp.bazel.server.bep;

import ch.epfl.scala.bsp4j.BuildClient;
import ch.epfl.scala.bsp4j.StatusCode;
import ch.epfl.scala.bsp4j.TaskFinishParams;
import ch.epfl.scala.bsp4j.TaskId;
import ch.epfl.scala.bsp4j.TaskStartParams;
import ch.epfl.scala.bsp4j.TextDocumentIdentifier;
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos;
import com.google.devtools.build.v1.BuildEvent;
import com.google.devtools.build.v1.PublishBuildEventGrpc;
import com.google.devtools.build.v1.PublishBuildToolEventStreamRequest;
import com.google.devtools.build.v1.PublishBuildToolEventStreamResponse;
import com.google.devtools.build.v1.PublishLifecycleEventRequest;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.bsp.bazel.commons.Constants;
import org.jetbrains.bsp.bazel.commons.ExitCodeMapper;
import org.jetbrains.bsp.bazel.server.diagnostics.DiagnosticsService;

public class BepServer extends PublishBuildEventGrpc.PublishBuildEventImplBase {

  private static final Logger LOGGER = LogManager.getLogger(BepServer.class);

  private final BuildClient bspClient;
  private final Optional<String> originId;
  private BepLogger bepLogger;

  private final Deque<Map.Entry<TaskId, Optional<String>>> startedEvents = new ArrayDeque<>();
  private final DiagnosticsService diagnosticsService;
  private BepOutputBuilder bepOutputBuilder = new BepOutputBuilder();

  public BepServer(
      BuildClient bspClient, DiagnosticsService diagnosticsService, Optional<String> originId) {
    this.bspClient = bspClient;
    this.diagnosticsService = diagnosticsService;
    this.originId = originId;
    this.bepLogger = new BepLogger(bspClient, originId);
  }

  public static BepServer newBepServer(
      BuildClient client,
      Path workspaceRoot,
      Map<String, Set<TextDocumentIdentifier>> hasAnyProblems,
      Optional<String> originId) {
    return new BepServer(client, new DiagnosticsService(workspaceRoot, hasAnyProblems), originId);
  }

  @Override
  public void publishLifecycleEvent(
      PublishLifecycleEventRequest request, StreamObserver<Empty> responseObserver) {
    responseObserver.onNext(Empty.getDefaultInstance());
    responseObserver.onCompleted();
  }

  @Override
  public StreamObserver<PublishBuildToolEventStreamRequest> publishBuildToolEventStream(
      StreamObserver<PublishBuildToolEventStreamResponse> responseObserver) {
    return new BepStreamObserver(this, responseObserver);
  }

  public void handleEvent(BuildEvent buildEvent) {
    try {
      BuildEventStreamProtos.BuildEvent event =
          BuildEventStreamProtos.BuildEvent.parseFrom(buildEvent.getBazelEvent().getValue());

      LOGGER.trace("Got event {}", event);

      handleBuildEventStreamProtosEvent(event);
    } catch (IOException e) {
      LOGGER.error("Error deserializing BEP proto: {}", e.toString());
    }
  }

  public void handleBuildEventStreamProtosEvent(BuildEventStreamProtos.BuildEvent event) {
    processBuildStartedEvent(event);
    processProgressEvent(event);
    processBuildMetrics(event);
    processFinishedEvent(event);
    processActionCompletedEvent(event);
    fetchNamedSet(event);
    processCompletedEvent(event);
    processAbortedEvent(event);
  }

  private void fetchNamedSet(BuildEventStreamProtos.BuildEvent event) {
    if (event.getId().hasNamedSet()) {
      bepOutputBuilder.storeNamedSet(
          event.getId().getNamedSet().getId(), event.getNamedSetOfFiles());
    }
  }

  private void processBuildStartedEvent(BuildEventStreamProtos.BuildEvent event) {
    if (event.hasStarted()
        && event.getStarted().getCommand().equals(Constants.BAZEL_BUILD_COMMAND)) {
      consumeBuildStartedEvent(event.getStarted());
    }
  }

  private void processProgressEvent(BuildEventStreamProtos.BuildEvent event) {
    if (event.hasProgress()) {
      // todo uncomment `onProgress` call back.
      // I had to revert this change because BEP events do not come deterministically when you use file-based BEP

      // bepLogger.onProgress(event.getProgress());
    }
  }

  private void processBuildMetrics(BuildEventStreamProtos.BuildEvent event) {
    if (event.hasBuildMetrics()) {
      bepLogger.onBuildMetrics(event.getBuildMetrics());
    }
  }

  private void consumeBuildStartedEvent(BuildEventStreamProtos.BuildStarted buildStarted) {
    bepOutputBuilder = new BepOutputBuilder();
    TaskId taskId = new TaskId(buildStarted.getUuid());
    TaskStartParams startParams = new TaskStartParams(taskId);
    startParams.setEventTime(buildStarted.getStartTimeMillis());

    bspClient.onBuildTaskStart(startParams);
    startedEvents.push(new AbstractMap.SimpleEntry<>(taskId, originId));
  }

  private void processFinishedEvent(BuildEventStreamProtos.BuildEvent event) {
    if (event.hasFinished()) {
      consumeFinishedEvent(event.getFinished());
    }
  }

  private void consumeFinishedEvent(BuildEventStreamProtos.BuildFinished buildFinished) {
    if (startedEvents.isEmpty()) {
      LOGGER.debug("No start event id was found.");
      return;
    }

    if (startedEvents.size() > 1) {
      LOGGER.debug("More than 1 start event was found");
      return;
    }

    StatusCode exitCode = ExitCodeMapper.mapExitCode(buildFinished.getExitCode().getCode());
    TaskFinishParams finishParams = new TaskFinishParams(startedEvents.pop().getKey(), exitCode);
    finishParams.setEventTime(buildFinished.getFinishTimeMillis());
    bspClient.onBuildTaskFinish(finishParams);
  }

  private void processCompletedEvent(BuildEventStreamProtos.BuildEvent event) {
    if (event.hasCompleted()) {
      consumeCompletedEvent(event);
    }
  }

  private void processActionCompletedEvent(BuildEventStreamProtos.BuildEvent event) {
    if (event.getId().hasActionCompleted()) {
      consumeActionCompletedEvent(event);
    }
  }

  private void consumeActionCompletedEvent(BuildEventStreamProtos.BuildEvent event) {
    var label = event.getId().getActionCompleted().getLabel();
    var actionEvent = event.getAction();
    if (!actionEvent.getSuccess()) {
      consumeUnsuccessfulActionCompletedEvent(actionEvent, label);
    }
  }

  private void consumeUnsuccessfulActionCompletedEvent(
      BuildEventStreamProtos.ActionExecuted actionEvent, String label) {
    if (actionEvent.getStderr().getFileCase() == BuildEventStreamProtos.File.FileCase.URI) {
      try {
        var path = Paths.get(URI.create(actionEvent.getStderr().getUri()));
        String stdErrText = Files.readString(path);
        processDiagnosticText(stdErrText, label);
      } catch (IOException e) {
        // noop
      }
    } else if (actionEvent.getStderr().getFileCase()
        == BuildEventStreamProtos.File.FileCase.CONTENTS) {
      processDiagnosticText(actionEvent.getStderr().getContents().toStringUtf8(), label);
    } else {
      processDiagnosticText("", label);
    }
  }

  private void processDiagnosticText(String stdErrText, String targetLabel) {
    var events =
        diagnosticsService.extractDiagnostics(
            stdErrText, targetLabel, startedEvents.getFirst().getValue().orElse(null));
    events.forEach(bspClient::onBuildPublishDiagnostics);
  }

  private void consumeCompletedEvent(BuildEventStreamProtos.BuildEvent event) {
    var label = event.getId().getTargetCompleted().getLabel();
    var targetComplete = event.getCompleted();
    var outputGroups = targetComplete.getOutputGroupList();
    LOGGER.trace("Consuming target completed event " + targetComplete);
    bepOutputBuilder.storeTargetOutputGroups(label, outputGroups);
    if (targetComplete.getSuccess()) {
      // clear former diagnostics by publishing an empty array of diagnostics
      // why we do this on `target_completed` instead of `action_completed`?
      // because `action_completed` won't be published on build success for a target.
      // https://github.com/bazelbuild/bazel/blob/d43737f95d28789bb2d9ef2d7f62320e9a840ab0/src/main/java/com/google/devtools/build/lib/buildeventstream/proto/build_event_stream.proto#L157-L160
      var events = diagnosticsService.clearFormerDiagnostics(label);
      events.forEach(bspClient::onBuildPublishDiagnostics);
    }
  }

  private void processAbortedEvent(BuildEventStreamProtos.BuildEvent event) {
    if (event.hasAborted()) {
      consumeAbortedEvent(event.getAborted());
    }
  }

  private void consumeAbortedEvent(BuildEventStreamProtos.Aborted aborted) {
    if (aborted.getReason() != BuildEventStreamProtos.Aborted.AbortReason.NO_BUILD) {
      LOGGER.warn(
          "Command aborted with reason {}: {}", aborted.getReason(), aborted.getDescription());
    }
  }

  public BepOutput getBepOutput() {
    return bepOutputBuilder.build();
  }
}
