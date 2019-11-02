package com.illicitonion.bazelbsp;

import ch.epfl.scala.bsp4j.BuildClient;
import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import ch.epfl.scala.bsp4j.Diagnostic;
import ch.epfl.scala.bsp4j.DiagnosticSeverity;
import ch.epfl.scala.bsp4j.Position;
import ch.epfl.scala.bsp4j.PublishDiagnosticsParams;
import ch.epfl.scala.bsp4j.Range;
import ch.epfl.scala.bsp4j.SourceItem;
import ch.epfl.scala.bsp4j.TextDocumentIdentifier;
import com.google.common.base.Splitter;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public class BepServer extends PublishBuildEventGrpc.PublishBuildEventImplBase {

  private final BazelBspServer bspServer;
  private final BuildClient bspClient;
  private final Map<String, BuildEventStreamProtos.NamedSetOfFiles> namedSetsOfFiles = new HashMap<>();
  private final TreeSet<Uri> compilerClasspathTextProtos = new TreeSet<>();
  private final TreeSet<Uri> compilerClasspath = new TreeSet<>();

  public BepServer(BazelBspServer bspServer, BuildClient bspClient) {
    this.bspServer = bspServer;
    this.bspClient = bspClient;
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
          if (event.getId().hasNamedSet()) {
            namedSetsOfFiles.put(event.getId().getNamedSet().getId(), event.getNamedSetOfFiles());
          }
          if (event.hasCompleted()) {
            List<BuildEventStreamProtos.OutputGroup> outputGroups = event.getCompleted().getOutputGroupList();
            if (outputGroups.size() == 1) {
              BuildEventStreamProtos.OutputGroup outputGroup = outputGroups.get(0);
              if ("scala_compiler_classpath_files".equals(outputGroup.getName())) {
                for (BuildEventStreamProtos.BuildEventId.NamedSetOfFilesId fileSetId : outputGroup.getFileSetsList()) {
                  for (BuildEventStreamProtos.File file : namedSetsOfFiles.get(fileSetId.getId()).getFilesList()) {
                    URI protoPathUri;
                    try {
                      protoPathUri = new URI(file.getUri());
                    } catch (URISyntaxException e) {
                      throw new RuntimeException(e);
                    }
                    List<String> lines = com.google.common.io.Files.readLines(new File(protoPathUri), StandardCharsets.UTF_8);
                    for (String line : lines) {
                      List<String> parts = Splitter.on("\"").splitToList(line);
                      if (parts.size() != 3) {
                        throw new RuntimeException("Wrong parts in sketchy textproto parsing: " + parts);
                      }
                      compilerClasspath.add(Uri.fromExecPath("exec-root://" + parts.get(1), bspServer.getExecRoot()));
                    }
                  }
                }
              }
            }
          }
          if (event.hasAction()) {
            BuildEventStreamProtos.ActionExecuted action = event.getAction();
            if (!"Scalac".equals(action.getType())) {
              // Ignore file template writes and such.
              // TODO: Maybe include them as task notifications (rather than diagnostics).
              return;
            }
            if (!action.hasDiagnosticOutput()) {
              System.out.println("Skipping action missing diagnostic output");
              return;
            }
            // TODO: Handle "No file" diagnostics
            System.out.println("DWH: Event: " + event);
            Map<Uri, List<PublishDiagnosticsParams>> filesToDiagnostics = new HashMap<>();
            BuildTargetIdentifier target = new BuildTargetIdentifier(action.getLabel());
            Diagnostics.TargetDiagnostics targetDiagnostics = Diagnostics.TargetDiagnostics.parseFrom(Files.readAllBytes(Paths.get(action.getDiagnosticOutput().getUri().substring(7))));
            for (Diagnostics.FileDiagnostics fileDiagnostics : targetDiagnostics.getDiagnosticsList()) {
              filesToDiagnostics.put(Uri.fromExecOrWorkspacePath(fileDiagnostics.getPath(), bspServer.getExecRoot(), bspServer.getWorkspaceRoot()), convert(target, fileDiagnostics));
            }
            for (SourceItem source : bspServer.getCachedBuildTargetSources(target)) {
              Uri sourceUri = Uri.fromFileUri(source.getUri());
              if (!filesToDiagnostics.containsKey(sourceUri)) {
                filesToDiagnostics.put(sourceUri, Lists.newArrayList(new PublishDiagnosticsParams(
                        new TextDocumentIdentifier(sourceUri.toString()),
                        target,
                        new ArrayList<>(),
                        true)));
              }
              // TODO: Somehow signal that a failure with no diagnostics was a failure.
              System.out.println("Diagnostics: " + filesToDiagnostics);
              if (bspClient != null) {
                for (List<PublishDiagnosticsParams> values : filesToDiagnostics.values()) {
                  for (PublishDiagnosticsParams param : values) {
                    bspClient.onBuildPublishDiagnostics(param);
                  }
                }
              }
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

  private List<PublishDiagnosticsParams> convert(BuildTargetIdentifier target, Diagnostics.FileDiagnostics request) {
    List<Diagnostic> diagnostics = new ArrayList<>();
    for (Diagnostics.Diagnostic diagProto: request.getDiagnosticsList()){
      DiagnosticSeverity severity = null;
      if (diagProto.getSeverity().equals(Diagnostics.Severity.ERROR)) {
        severity = DiagnosticSeverity.ERROR;
      } else if (diagProto.getSeverity().equals(Diagnostics.Severity.WARNING)) {
        severity = DiagnosticSeverity.WARNING;
      }
      // TODO: Other severities
      Diagnostic diagnostic =
          new Diagnostic(
              new Range(
                  new Position(diagProto.getRange().getStart().getLine(), diagProto.getRange().getStart().getCharacter()),
                  new Position(diagProto.getRange().getEnd().getLine(), diagProto.getRange().getEnd().getCharacter())),
              diagProto.getMessage()
          );
      if (severity != null) {
        diagnostic.setSeverity(severity);
      }
      diagnostics.add(diagnostic);
    }
    return Lists.newArrayList(new PublishDiagnosticsParams(
        new TextDocumentIdentifier(Uri.fromExecOrWorkspacePath(request.getPath(), bspServer.getExecRoot(), bspServer.getWorkspaceRoot()).toString()),
        target,
        diagnostics,
        true));
  }

  public TreeSet<Uri> fetchScalacClasspath() {
    return compilerClasspath;
  }
}
