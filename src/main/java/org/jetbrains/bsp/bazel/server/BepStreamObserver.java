package org.jetbrains.bsp.bazel.server;

import com.google.devtools.build.v1.PublishBuildToolEventStreamRequest;
import com.google.devtools.build.v1.PublishBuildToolEventStreamResponse;
import io.grpc.stub.StreamObserver;

public class BepStreamObserver implements StreamObserver<PublishBuildToolEventStreamRequest> {

  private final BepEventHandler eventHandler;
  private final StreamObserver<PublishBuildToolEventStreamResponse> responseObserver;

  public BepStreamObserver(
      BepEventHandler eventHandler, StreamObserver<PublishBuildToolEventStreamResponse> responseObserver) {
    this.eventHandler = eventHandler;
    this.responseObserver = responseObserver;
  }

  @Override
  public void onNext(PublishBuildToolEventStreamRequest request) {
    if (request
        .getOrderedBuildEvent()
        .getEvent()
        .getBazelEvent()
        .getTypeUrl()
        .equals("type.googleapis.com/build_event_stream.BuildEvent")) {
      eventHandler.handle(request.getOrderedBuildEvent().getEvent());
    }

    PublishBuildToolEventStreamResponse response =
        PublishBuildToolEventStreamResponse.newBuilder()
            .setStreamId(request.getOrderedBuildEvent().getStreamId())
            .setSequenceNumber(request.getOrderedBuildEvent().getSequenceNumber())
            .build();

    responseObserver.onNext(response);
  }

  @Override
  public void onError(Throwable throwable) {
    System.out.println("Error from BEP stream: " + throwable);
  }

  @Override
  public void onCompleted() {
    responseObserver.onCompleted();
  }
}
