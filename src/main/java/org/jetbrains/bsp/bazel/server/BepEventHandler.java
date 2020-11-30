package org.jetbrains.bsp.bazel.server;

import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import ch.epfl.scala.bsp4j.Diagnostic;
import ch.epfl.scala.bsp4j.DiagnosticSeverity;
import ch.epfl.scala.bsp4j.Position;
import ch.epfl.scala.bsp4j.PublishDiagnosticsParams;
import ch.epfl.scala.bsp4j.Range;
import ch.epfl.scala.bsp4j.TaskFinishParams;
import ch.epfl.scala.bsp4j.TaskId;
import ch.epfl.scala.bsp4j.TaskStartParams;
import ch.epfl.scala.bsp4j.TextDocumentIdentifier;
import com.google.common.base.Splitter;
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos;
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos.OutputGroup;
import com.google.devtools.build.v1.BuildEvent;
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
import org.jetbrains.bsp.bazel.common.Constants;
import org.jetbrains.bsp.bazel.common.Uri;

public class BepEventHandler {

  // TODO(gerardd33) access to BepServer's attributes

  private final Stack<TaskId> taskParkingLot = new Stack<>();

  public void handle(BuildEvent buildEvent) {
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

  private void processCompletionEvent(BuildEventStreamProtos.BuildEvent event) throws IOException {
    List<OutputGroup> outputGroups = event.getCompleted().getOutputGroupList();

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
                  Uri.fromExecPath("exec-root://" + parts.get(1), bspServer.getExecRoot()));
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

    if (!Constants.SUPPORTED_ACTIONS.contains(actionType)) {
      // Ignore file template writes and such.
      // TODO(illicitonion): Maybe include them as task notifications (rather than diagnostics).
      return;
    }

    System.out.println("DWH: Event: " + event + "\n\n");

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

      collectDiagnostics(filesToDiagnostics, target, log.getUri().substring(7));
    }

    if (filesToDiagnostics.isEmpty() && hasDiagnosticsOutput) {
      collectDiagnostics(
          filesToDiagnostics, target, diagnosticsProtosLocations.get(target.getUri()));
      diagnosticsProtosLocations.remove(target.getUri());
    }

    emitDiagnostics(filesToDiagnostics, target);
  }

  private void processAbortion(BuildEventStreamProtos.Aborted aborted) {
    if (aborted.getReason() != BuildEventStreamProtos.Aborted.AbortReason.NO_BUILD) {
      buildClientLogger.logError(
          "Command aborted with reason " + aborted.getReason() + ": " + aborted.getDescription());
    }
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
