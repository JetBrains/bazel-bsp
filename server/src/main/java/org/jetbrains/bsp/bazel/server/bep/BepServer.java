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
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.bsp.bazel.commons.Constants;
import org.jetbrains.bsp.bazel.commons.ExitCodeMapper;
import org.jetbrains.bsp.bazel.logger.BspClientLogger;
import org.jetbrains.bsp.bazel.server.diagnostics.DiagnosticsService;

public class BepServer extends PublishBuildEventGrpc.PublishBuildEventImplBase {

  private static final Logger LOGGER = LogManager.getLogger(BepServer.class);

  private final BuildClient bspClient;
  private final BspClientLogger logger;

  private final Deque<TaskId> startedEventTaskIds = new ArrayDeque<>();
  private BepOutputBuilder bepOutputBuilder = new BepOutputBuilder();

  private final DiagnosticsService diagnosticsService;

  public BepServer(
      BuildClient bspClient, BspClientLogger logger, DiagnosticsService diagnosticsService) {
    this.bspClient = bspClient;
    this.logger = logger;
    this.diagnosticsService = diagnosticsService;
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
      fetchNamedSet(event);
      processCompletedEvent(event);
      processAbortedEvent(event);
      processProgressEvent(event);
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
      LOGGER.debug("No start event id was found.");
      return;
    }

    if (startedEventTaskIds.size() > 1) {
      LOGGER.debug("More than 1 start event was found");
      return;
    }

    StatusCode exitCode = ExitCodeMapper.mapExitCode(buildFinished.getExitCode().getCode());
    TaskFinishParams finishParams = new TaskFinishParams(startedEventTaskIds.pop(), exitCode);
    finishParams.setEventTime(buildFinished.getFinishTimeMillis());

    bspClient.onBuildTaskFinish(finishParams);
  }

  private void processCompletedEvent(BuildEventStreamProtos.BuildEvent event) {
    if (event.hasCompleted()) {
      consumeCompletedEvent(event);
    }
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

  private void processProgressEvent(BuildEventStreamProtos.BuildEvent event) {
    if (event.hasProgress()) {
      consumeProgressEvent(event.getProgress());
    }
  }

  private void consumeProgressEvent(BuildEventStreamProtos.Progress progress) {
    diagnosticsService
        .extractDiagnostics(progress.getStderr())
        .forEach(bspClient::onBuildPublishDiagnostics);
  }

  public BepOutput getBepOutput() {
    return bepOutputBuilder.build();
  }
}
