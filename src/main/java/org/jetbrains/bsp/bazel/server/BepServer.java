package org.jetbrains.bsp.bazel.server;

import ch.epfl.scala.bsp4j.BuildClient;
import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import ch.epfl.scala.bsp4j.Diagnostic;
import ch.epfl.scala.bsp4j.DiagnosticSeverity;
import ch.epfl.scala.bsp4j.Position;
import ch.epfl.scala.bsp4j.PublishDiagnosticsParams;
import ch.epfl.scala.bsp4j.Range;
import ch.epfl.scala.bsp4j.StatusCode;
import ch.epfl.scala.bsp4j.TaskFinishParams;
import ch.epfl.scala.bsp4j.TaskId;
import ch.epfl.scala.bsp4j.TaskStartParams;
import ch.epfl.scala.bsp4j.TextDocumentIdentifier;
import com.google.common.base.Splitter;
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos;
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos.OutputGroup;
import com.google.devtools.build.v1.BuildEvent;
import com.google.devtools.build.v1.PublishBuildEventGrpc;
import com.google.devtools.build.v1.PublishBuildToolEventStreamRequest;
import com.google.devtools.build.v1.PublishBuildToolEventStreamResponse;
import com.google.devtools.build.v1.PublishLifecycleEventRequest;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeSet;
import org.jetbrains.bsp.bazel.common.Constants;
import org.jetbrains.bsp.bazel.common.Uri;
import org.jetbrains.bsp.bazel.server.logger.BuildClientLogger;

public class BepServer extends PublishBuildEventGrpc.PublishBuildEventImplBase {

  private final BazelBspServer bspServer;
  private final DiagnosticsDispatcher diagnosticsDispatcher;

  private final BuildClient bspClient;
  private final BuildClientLogger buildClientLogger;

  private final Stack<TaskId> taskParkingLot = new Stack<>();
  private final TreeSet<Uri> compilerClasspath = new TreeSet<>();
  private final Map<String, String> diagnosticsProtosLocations = new HashMap<>();
  private final Map<String, BuildEventStreamProtos.NamedSetOfFiles> namedSetsOfFiles =
      new HashMap<>();

  public BepServer(
      BazelBspServer bspServer, BuildClient bspClient, BuildClientLogger buildClientLogger) {
    this.bspServer = bspServer;
    this.bspClient = bspClient;
    this.buildClientLogger = buildClientLogger;
    this.diagnosticsDispatcher = new DiagnosticsDispatcher(bspServer, bspClient);
  }

  public static StatusCode convertExitCode(int exitCode) {
    switch (exitCode) {
      case 0:
        return StatusCode.OK;
      case 8:
        return StatusCode.CANCELLED;
      default:
        return StatusCode.ERROR;
    }
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

  public TreeSet<Uri> getCompilerClasspath() {
    return compilerClasspath;
  }

  public Map<String, String> getDiagnosticsProtosLocations() {
    return diagnosticsProtosLocations;
  }

  public void collectDiagnostics(
      Map<Uri, List<PublishDiagnosticsParams>> filesToDiagnostics,
      BuildTargetIdentifier target,
      String diagnosticsLocation)
      throws IOException {
    diagnosticsDispatcher.collectDiagnostics(filesToDiagnostics, target, diagnosticsLocation);
  }

  public void emitDiagnostics(
      Map<Uri, List<PublishDiagnosticsParams>> filesToDiagnostics, BuildTargetIdentifier target) {
    diagnosticsDispatcher.emitDiagnostics(filesToDiagnostics, target);
  }

  public void handleEvent(BuildEvent buildEvent) {
    try {
      BuildEventStreamProtos.BuildEvent event =
          BuildEventStreamProtos.BuildEvent.parseFrom(buildEvent.getBazelEvent().getValue());

      System.out.println("Got event" + event + "\nevent-end\n");

      if (event.getId().hasNamedSet()) {
        namedSetsOfFiles.put(event.getId().getNamedSet().getId(), event.getNamedSetOfFiles());
      }

      if (event.hasStarted() && event.getStarted().getCommand().equals("build")) {
        processedStartedEvent(event.getStarted());
      }

      if (event.hasFinished()) {
        processFinishedEvent(event.getFinished());
      }

      if (event.hasCompleted()) {
        processCompletedEvent(event.getCompleted());
      }

      if (event.hasAction()) {
        processActionEvent(event.getAction());
      }

      if (event.hasAborted()) {
        processAbortedEvent(event.getAborted());
      }

      if (event.hasProgress()) {
        processProgressEvent(event.getProgress());
      }
    } catch (IOException e) {
      System.err.println("Error deserializing BEP proto: " + e);
    }
  }

  private void processedStartedEvent(BuildEventStreamProtos.BuildStarted buildStarted) {
    TaskId taskId = new TaskId(buildStarted.getUuid());
    TaskStartParams startParams = new TaskStartParams(taskId);
    startParams.setEventTime(buildStarted.getStartTimeMillis());

    bspClient.onBuildTaskStart(startParams);
    taskParkingLot.add(taskId);
  }

  private void processFinishedEvent(BuildEventStreamProtos.BuildFinished buildFinished) {
    if (taskParkingLot.empty()) {
      System.out.println("No start event id was found.");
      return;
    } else if (taskParkingLot.size() > 1) {
      System.out.println("More than 1 start event was found");
      return;
    }

    TaskFinishParams finishParams =
        new TaskFinishParams(
            taskParkingLot.pop(),
            BepServer.convertExitCode(buildFinished.getExitCode().getCode()));

    finishParams.setEventTime(buildFinished.getFinishTimeMillis());
    bspClient.onBuildTaskFinish(finishParams);
  }

  private void processProgressEvent(BuildEventStreamProtos.Progress progress) {
    processStdErrDiagnostics(progress);

    String message = progress.getStderr().trim();
    if (!message.isEmpty()) {
      buildClientLogger.logMessage(progress.getStderr().trim());
    }
  }

  private void processCompletedEvent(BuildEventStreamProtos.TargetComplete targetComplete) throws IOException {
    List<OutputGroup> outputGroups = targetComplete.getOutputGroupList();

    if (outputGroups.size() == 1) {
      BuildEventStreamProtos.OutputGroup outputGroup = outputGroups.get(0);

      if ("scala_compiler_classpath_files".equals(outputGroup.getName())) {
        for (BuildEventStreamProtos.BuildEventId.NamedSetOfFilesId fileSetId :
            outputGroup.getFileSetsList()) {
          for (BuildEventStreamProtos.File file :
              namedSetsOfFiles.get(fileSetId.getId()).getFilesList()) {
            URI protoPathUri;
            try {
              protoPathUri = new URI(file.getUri());
            } catch (URISyntaxException e) {
              throw new RuntimeException(e);
            }

            List<String> lines =
                com.google.common.io.Files.readLines(
                    new File(protoPathUri), StandardCharsets.UTF_8);

            for (String line : lines) {
              List<String> parts = Splitter.on("\"").splitToList(line);
              if (parts.size() != 3) {
                throw new RuntimeException("Wrong parts in sketchy textproto parsing: " + parts);
              }

              compilerClasspath.add(
                  Uri.fromExecPath("exec-root://" + parts.get(1),
                      bspServer.getBazelData().getExecRoot()));
            }
          }
        }
      }
    }
  }

  private void processAbortedEvent(BuildEventStreamProtos.Aborted aborted) {
    if (aborted.getReason() != BuildEventStreamProtos.Aborted.AbortReason.NO_BUILD) {
      buildClientLogger.logError(
          "Command aborted with reason " + aborted.getReason() + ": " + aborted.getDescription());
    }
  }

  private void processActionEvent(BuildEventStreamProtos.ActionExecuted action)
      throws IOException {
    String actionType = action.getType();

    if (!Constants.SUPPORTED_ACTIONS.contains(actionType)) {
      // Ignore file template writes and such.
      // TODO(illicitonion): Maybe include them as task notifications (rather than diagnostics).
      return;
    }

    Map<Uri, List<PublishDiagnosticsParams>> filesToDiagnostics = new HashMap<>();
    BuildTargetIdentifier target = new BuildTargetIdentifier(action.getLabel());
    boolean hasDiagnosticsOutput = diagnosticsProtosLocations.containsKey(target.getUri());

    for (BuildEventStreamProtos.File log : action.getActionMetadataLogsList()) {
      if (!log.getName().equals("diagnostics")) {
        continue;
      }

      System.out.println("Found diagnostics file in " + log.getUri());

      if (hasDiagnosticsOutput) {
        diagnosticsProtosLocations.remove(target.getUri());
      }

      diagnosticsDispatcher.collectDiagnostics(filesToDiagnostics, target, log.getUri().substring(7));
    }

    if (filesToDiagnostics.isEmpty() && hasDiagnosticsOutput) {
      diagnosticsDispatcher.collectDiagnostics(
          filesToDiagnostics, target, diagnosticsProtosLocations.get(target.getUri()));
      diagnosticsProtosLocations.remove(target.getUri());
    }

    diagnosticsDispatcher.emitDiagnostics(filesToDiagnostics, target);
  }

  private void processStdErrDiagnostics(BuildEventStreamProtos.Progress progress) {
    HashMap<String, List<Diagnostic>> fileDiagnostics = new HashMap<>();

    Arrays.stream(progress.getStderr().split("\n"))
        .filter(
            error ->
                error.contains("ERROR")
                    && (error.contains("/" + Constants.WORKSPACE_FILE_NAME + ":")
                        || error.contains("/" + Constants.BUILD_FILE_NAME + ":")))
        .forEach(
            error -> {
              String erroredFile =
                  error.contains(Constants.WORKSPACE_FILE_NAME)
                      ? Constants.WORKSPACE_FILE_NAME
                      : Constants.BUILD_FILE_NAME;
              String[] lineLocation;
              String fileLocation;

              if (error.contains(" at /")) {
                int endOfMessage = error.indexOf(" at /");
                String fileInfo = error.substring(endOfMessage + 4);

                int urlEnd = fileInfo.indexOf(erroredFile) + erroredFile.length();
                fileLocation = fileInfo.substring(0, urlEnd);
                lineLocation = fileInfo.substring(urlEnd + 1).split(":");
              } else {
                int urlEnd = error.indexOf(erroredFile) + erroredFile.length();
                fileLocation = error.substring(error.indexOf("ERROR: ") + 7, urlEnd);
                lineLocation = error.substring(urlEnd + 1).split("(:)|( )");
              }

              System.out.println("Error: " + error);
              System.out.println("File location: " + fileLocation);
              System.out.println("Line location: " + Arrays.toString(lineLocation));

              Position position =
                  new Position(
                      Integer.parseInt(lineLocation[0]), Integer.parseInt(lineLocation[1]));

              Diagnostic diagnostic = new Diagnostic(new Range(position, position), error);
              diagnostic.setSeverity(DiagnosticSeverity.ERROR);

              List<Diagnostic> diagnostics =
                  fileDiagnostics.getOrDefault(fileLocation, new ArrayList<>());
              diagnostics.add(diagnostic);

              fileDiagnostics.put(fileLocation, diagnostics);
            });

    fileDiagnostics.forEach(
        (fileLocation, diagnostics) ->
            bspClient.onBuildPublishDiagnostics(
                new PublishDiagnosticsParams(
                    new TextDocumentIdentifier(Uri.fromAbsolutePath(fileLocation).toString()),
                    new BuildTargetIdentifier(""),
                    diagnostics,
                    false)));
  }
}
