package org.jetbrains.bsp.bazel.server;

import ch.epfl.scala.bsp4j.BuildClient;
import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import ch.epfl.scala.bsp4j.Diagnostic;
import ch.epfl.scala.bsp4j.DiagnosticSeverity;
import ch.epfl.scala.bsp4j.Position;
import ch.epfl.scala.bsp4j.PublishDiagnosticsParams;
import ch.epfl.scala.bsp4j.Range;
import ch.epfl.scala.bsp4j.SourceItem;
import ch.epfl.scala.bsp4j.StatusCode;
import ch.epfl.scala.bsp4j.TaskFinishParams;
import ch.epfl.scala.bsp4j.TaskId;
import ch.epfl.scala.bsp4j.TaskStartParams;
import ch.epfl.scala.bsp4j.TextDocumentIdentifier;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos;
import com.google.devtools.build.v1.BuildEvent;
import com.google.devtools.build.v1.PublishBuildEventGrpc;
import com.google.devtools.build.v1.PublishBuildToolEventStreamRequest;
import com.google.devtools.build.v1.PublishBuildToolEventStreamResponse;
import com.google.devtools.build.v1.PublishLifecycleEventRequest;
import com.google.protobuf.Empty;
import io.bazel.rules_scala.diagnostics.Diagnostics;
import io.grpc.stub.StreamObserver;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import org.jetbrains.bsp.bazel.common.Uri;
import org.jetbrains.bsp.bazel.server.logger.BuildClientLogger;

public class BepServer extends PublishBuildEventGrpc.PublishBuildEventImplBase {

  private static final Set<String> SUPPORTED_ACTIONS =
      ImmutableSet.of(BazelBspServer.KOTLINC, BazelBspServer.JAVAC, BazelBspServer.SCALAC);
  private final BazelBspServer bspServer;
  private final BuildClient bspClient;
  private final Map<String, BuildEventStreamProtos.NamedSetOfFiles> namedSetsOfFiles =
      new HashMap<>();
  private final TreeSet<Uri> compilerClasspathTextProtos = new TreeSet<>();
  private final TreeSet<Uri> compilerClasspath = new TreeSet<>();
  private final Stack<TaskId> taskParkingLot = new Stack<>();
  private final String workspace = "WORKSPACE";
  private final String build = "BUILD";
  private final BuildClientLogger buildClientLogger;
  private Map<String, String> diagnosticsProtosLocations = new HashMap<>();

  public BepServer(
      BazelBspServer bspServer, BuildClient bspClient, BuildClientLogger buildClientLogger) {
    this.bspServer = bspServer;
    this.bspClient = bspClient;
    this.buildClientLogger = buildClientLogger;
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
    return new StreamObserver<PublishBuildToolEventStreamRequest>() {

      @Override
      public void onNext(PublishBuildToolEventStreamRequest request) {
        if (request
            .getOrderedBuildEvent()
            .getEvent()
            .getBazelEvent()
            .getTypeUrl()
            .equals("type.googleapis.com/build_event_stream.BuildEvent")) {
          handleEvent(request.getOrderedBuildEvent().getEvent());
        } else {
          //          System.out.println("Got this request " + request);
        }
        PublishBuildToolEventStreamResponse response =
            PublishBuildToolEventStreamResponse.newBuilder()
                .setStreamId(request.getOrderedBuildEvent().getStreamId())
                .setSequenceNumber(request.getOrderedBuildEvent().getSequenceNumber())
                .build();
        responseObserver.onNext(response);
      }

      private void handleEvent(BuildEvent buildEvent) {
        try {
          BuildEventStreamProtos.BuildEvent event =
              BuildEventStreamProtos.BuildEvent.parseFrom(buildEvent.getBazelEvent().getValue());
          System.out.println("Got event" + event + "\nevent-end\n");
          if (event.hasStarted() && event.getStarted().getCommand().equals("build")) {
            BuildEventStreamProtos.BuildStarted buildStarted = event.getStarted();
            TaskId taskId = new TaskId(buildStarted.getUuid());
            TaskStartParams startParams = new TaskStartParams(taskId);
            startParams.setEventTime(buildStarted.getStartTimeMillis());
            bspClient.onBuildTaskStart(startParams);
            taskParkingLot.add(taskId);
          }
          if (event.hasFinished()) {
            BuildEventStreamProtos.BuildFinished buildFinished = event.getFinished();
            if (taskParkingLot.size() == 0) {
              System.out.println("No start event id was found.");
              return;
            } else if (taskParkingLot.size() > 1) {
              System.out.println("More than 1 start event was found");
              return;
            }

            TaskFinishParams finishParams =
                new TaskFinishParams(
                    taskParkingLot.pop(), convertExitCode(buildFinished.getExitCode().getCode()));
            finishParams.setEventTime(buildFinished.getFinishTimeMillis());
            bspClient.onBuildTaskFinish(finishParams);
          }

          if (event.getId().hasNamedSet()) {
            namedSetsOfFiles.put(event.getId().getNamedSet().getId(), event.getNamedSetOfFiles());
          }
          if (event.hasCompleted()) {
            processCompletionEvent(event);
          }
          if (event.hasAction()) {
            processActionDiagnostics(event);
          }
          if (event.hasAborted()) {
            processAbortion(event.getAborted());
          }
          if (event.hasProgress()) {
            BuildEventStreamProtos.Progress progress = event.getProgress();
            processStdErrDiagnostics(progress);
            String message = progress.getStderr().trim();
            if (!message.isEmpty()) {
              buildClientLogger.logMessage(progress.getStderr().trim());
            }
          }

        } catch (IOException e) {
          System.err.println("Error deserializing BEP proto: " + e);
        }
      }

      @Override
      public void onError(Throwable throwable) {
        System.out.println("Error from BEP stream: " + throwable);
      }

      @Override
      public void onCompleted() {
        responseObserver.onCompleted();
      }
    };
  }

  private void processAbortion(BuildEventStreamProtos.Aborted aborted) {
    if (aborted.getReason() != BuildEventStreamProtos.Aborted.AbortReason.NO_BUILD)
      buildClientLogger.logError(
          "Command aborted with reason " + aborted.getReason() + ": " + aborted.getDescription());
  }

  private void processStdErrDiagnostics(BuildEventStreamProtos.Progress progress) {
    HashMap<String, List<Diagnostic>> fileDiagnostics = new HashMap<>();
    Arrays.stream(progress.getStderr().split("\n"))
        .filter(
            error ->
                error.contains("ERROR")
                    && (error.contains("/" + workspace + ":") || error.contains("/" + build + ":")))
        .forEach(
            error -> {
              String erroredFile = error.contains(workspace) ? workspace : build;
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

  private void processCompletionEvent(BuildEventStreamProtos.BuildEvent event) throws IOException {
    List<BuildEventStreamProtos.OutputGroup> outputGroups =
        event.getCompleted().getOutputGroupList();
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
                  Uri.fromExecPath(
                      "exec-root://" + parts.get(1), bspServer.getBazelData().getExecRoot()));
            }
          }
        }
      }
    }
  }

  private void processActionDiagnostics(BuildEventStreamProtos.BuildEvent event)
      throws IOException {
    BuildEventStreamProtos.ActionExecuted action = event.getAction();
    String actionType = action.getType();
    if (!SUPPORTED_ACTIONS.contains(actionType)) {
      // Ignore file template writes and such.
      // TODO(illicitonion): Maybe include them as task notifications (rather than diagnostics).
      return;
    }
    System.out.println("DWH: Event: " + event + "\n\n");
    Map<Uri, List<PublishDiagnosticsParams>> filesToDiagnostics = new HashMap<>();
    BuildTargetIdentifier target = new BuildTargetIdentifier(action.getLabel());
    boolean hasDiagnosticsOutput = diagnosticsProtosLocations.containsKey(target.getUri());
    for (BuildEventStreamProtos.File log : action.getActionMetadataLogsList()) {
      if (!log.getName().equals("diagnostics")) continue;

      System.out.println("Found diagnostics file in " + log.getUri());
      if (hasDiagnosticsOutput) diagnosticsProtosLocations.remove(target.getUri());
      getDiagnostics(filesToDiagnostics, target, log.getUri().substring(7));
    }

    if (filesToDiagnostics.isEmpty() && hasDiagnosticsOutput) {
      getDiagnostics(filesToDiagnostics, target, diagnosticsProtosLocations.get(target.getUri()));
      diagnosticsProtosLocations.remove(target.getUri());
    }

    emitDiagnostics(filesToDiagnostics, target);
  }

  public void emitDiagnostics(
      Map<Uri, List<PublishDiagnosticsParams>> filesToDiagnostics, BuildTargetIdentifier target) {
    for (SourceItem source : bspServer.getCachedBuildTargetSources(target)) {
      Uri sourceUri = Uri.fromFileUri(source.getUri());
      if (!filesToDiagnostics.containsKey(sourceUri)) {
        filesToDiagnostics.put(
            sourceUri,
            Lists.newArrayList(
                new PublishDiagnosticsParams(
                    new TextDocumentIdentifier(sourceUri.toString()),
                    target,
                    new ArrayList<>(),
                    true)));
      }
      if (bspClient != null) {
        for (List<PublishDiagnosticsParams> values : filesToDiagnostics.values()) {
          for (PublishDiagnosticsParams param : values) {
            bspClient.onBuildPublishDiagnostics(param);
          }
        }
      }
    }
  }

  public void getDiagnostics(
      Map<Uri, List<PublishDiagnosticsParams>> filesToDiagnostics,
      BuildTargetIdentifier target,
      String diagnosticsLocation)
      throws IOException {
    Diagnostics.TargetDiagnostics targetDiagnostics =
        Diagnostics.TargetDiagnostics.parseFrom(Files.readAllBytes(Paths.get(diagnosticsLocation)));
    for (Diagnostics.FileDiagnostics fileDiagnostics : targetDiagnostics.getDiagnosticsList()) {
      System.out.println("Inserting diagnostics for path: " + fileDiagnostics.getPath());
      filesToDiagnostics.put(
          Uri.fromExecOrWorkspacePath(
              fileDiagnostics.getPath(),
              bspServer.getBazelData().getExecRoot(),
              bspServer.getBazelData().getWorkspaceRoot()),
          convert(target, fileDiagnostics));
    }
  }

  private List<PublishDiagnosticsParams> convert(
      BuildTargetIdentifier target, Diagnostics.FileDiagnostics request) {
    List<Diagnostic> diagnostics = new ArrayList<>();
    for (Diagnostics.Diagnostic diagProto : request.getDiagnosticsList()) {
      DiagnosticSeverity severity = null;
      Diagnostics.Severity protoSeverity = diagProto.getSeverity();
      if (protoSeverity.equals(Diagnostics.Severity.ERROR)) {
        severity = DiagnosticSeverity.ERROR;
      } else if (protoSeverity.equals(Diagnostics.Severity.WARNING)) {
        severity = DiagnosticSeverity.WARNING;
      } else if (protoSeverity.equals(Diagnostics.Severity.INFORMATION)) {
        severity = DiagnosticSeverity.INFORMATION;
      } else if (protoSeverity.equals(Diagnostics.Severity.HINT)) {
        severity = DiagnosticSeverity.HINT;
      } else if (protoSeverity.equals(Diagnostics.Severity.UNKNOWN)) {
        severity = DiagnosticSeverity.ERROR;
      }

      Diagnostic diagnostic =
          new Diagnostic(
              new Range(
                  new Position(
                      diagProto.getRange().getStart().getLine(),
                      diagProto.getRange().getStart().getCharacter()),
                  new Position(
                      diagProto.getRange().getEnd().getLine(),
                      diagProto.getRange().getEnd().getCharacter())),
              diagProto.getMessage());
      if (severity != null) {
        diagnostic.setSeverity(severity);
      }
      diagnostics.add(diagnostic);
    }
    return Lists.newArrayList(
        new PublishDiagnosticsParams(
            new TextDocumentIdentifier(
                Uri.fromExecOrWorkspacePath(
                        request.getPath(),
                        bspServer.getBazelData().getExecRoot(),
                        bspServer.getBazelData().getWorkspaceRoot())
                    .toString()),
            target,
            diagnostics,
            true));
  }

  public TreeSet<Uri> fetchScalacClasspath() {
    return compilerClasspath;
  }

  public Map<String, String> getDiagnosticsProtosLocations() {
    return diagnosticsProtosLocations;
  }

  public void setDiagnosticsProtosLocations(Map<String, String> diagnosticsProtosLocations) {
    this.diagnosticsProtosLocations = diagnosticsProtosLocations;
  }
}
