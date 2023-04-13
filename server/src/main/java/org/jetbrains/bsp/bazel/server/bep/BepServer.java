package org.jetbrains.bsp.bazel.server.bep;

import ch.epfl.scala.bsp4j.BuildClient;
import ch.epfl.scala.bsp4j.StatusCode;
import ch.epfl.scala.bsp4j.TaskFinishParams;
import ch.epfl.scala.bsp4j.TaskId;
import ch.epfl.scala.bsp4j.TaskStartParams;
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos;
import com.google.devtools.build.v1.BuildEvent;
import com.google.devtools.build.v1.PublishBuildEventGrpc;
import com.google.devtools.build.v1.PublishBuildToolEventStreamRequest;
import com.google.devtools.build.v1.PublishBuildToolEventStreamResponse;
import com.google.devtools.build.v1.PublishLifecycleEventRequest;
import com.google.protobuf.Empty;
import io.grpc.netty.NettyServerBuilder;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.bsp.bazel.commons.Constants;
import org.jetbrains.bsp.bazel.commons.ExitCodeMapper;
import org.jetbrains.bsp.bazel.server.diagnostics.DiagnosticsService;

public class BepServer extends PublishBuildEventGrpc.PublishBuildEventImplBase {

  private static final Logger LOGGER = LogManager.getLogger(BepServer.class);

  private final BuildClient bspClient;

  private final Deque<Map.Entry<TaskId, String>> startedEvents = new ArrayDeque<>();
  private final DiagnosticsService diagnosticsService;
  private BepOutputBuilder bepOutputBuilder = new BepOutputBuilder();

  public BepServer(BuildClient bspClient, DiagnosticsService diagnosticsService) {
    this.bspClient = bspClient;
    this.diagnosticsService = diagnosticsService;
  }

  public static BepServer newBepServer(BuildClient client, Path workspaceRoot) {
    return new BepServer(client, new DiagnosticsService(workspaceRoot));
  }

  public static NettyServerBuilder nettyServerBuilder() {
    return NettyServerBuilder.forAddress(new InetSocketAddress("localhost", 0));
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

      LOGGER.debug("Got event {}", event);

      processBuildStartedEvent(event);
      processFinishedEvent(event);
      processActionCompletedEvent(event);
      fetchNamedSet(event);
      processCompletedEvent(event);
      processAbortedEvent(event);
    } catch (IOException e) {
      LOGGER.error("Error deserializing BEP proto: {}", e.toString());
    }
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

  private void consumeBuildStartedEvent(BuildEventStreamProtos.BuildStarted buildStarted) {
    bepOutputBuilder = new BepOutputBuilder();
    TaskId taskId = new TaskId(buildStarted.getUuid());
    String originId = extractOriginIdFromOptions(buildStarted.getOptionsDescription());
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

  private String extractOriginIdFromOptions(String optionsDescription) {
    Pattern pattern = Pattern.compile("(?<=ORIGINID=)(.*?)(?=')");
    Matcher matcher = pattern.matcher(optionsDescription);
    if (matcher.find()) {
      return matcher.group();
    }
    return null;
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
            stdErrText, targetLabel, startedEvents.getFirst().getValue());
    events.forEach(bspClient::onBuildPublishDiagnostics);
  }

  private void consumeCompletedEvent(BuildEventStreamProtos.BuildEvent event) {
    var label = event.getId().getTargetCompleted().getLabel();
    var targetComplete = event.getCompleted();
    var outputGroups = targetComplete.getOutputGroupList();
    LOGGER.debug("Consuming target completed event " + targetComplete);
    bepOutputBuilder.storeTargetOutputGroups(label, outputGroups);
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
