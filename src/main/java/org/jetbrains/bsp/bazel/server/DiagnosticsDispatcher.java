package org.jetbrains.bsp.bazel.server;

import ch.epfl.scala.bsp4j.BuildClient;
import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import ch.epfl.scala.bsp4j.Diagnostic;
import ch.epfl.scala.bsp4j.DiagnosticSeverity;
import ch.epfl.scala.bsp4j.Position;
import ch.epfl.scala.bsp4j.PublishDiagnosticsParams;
import ch.epfl.scala.bsp4j.Range;
import ch.epfl.scala.bsp4j.SourceItem;
import ch.epfl.scala.bsp4j.TextDocumentIdentifier;
import com.google.common.collect.Lists;
import io.bazel.rules_scala.diagnostics.Diagnostics;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jetbrains.bsp.bazel.common.Uri;

public class DiagnosticsDispatcher {

  private final BazelBspServer bspServer;
  private final BuildClient bspClient;

  public DiagnosticsDispatcher(BazelBspServer bspServer, BuildClient bspClient) {
    this.bspServer = bspServer;
    this.bspClient = bspClient;
  }

  public void collectDiagnostics(
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
          convertDiagnostics(target, fileDiagnostics));
    }
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

  private List<PublishDiagnosticsParams> convertDiagnostics(
      BuildTargetIdentifier target, Diagnostics.FileDiagnostics request) {
    List<Diagnostic> diagnostics = new ArrayList<>();

    for (Diagnostics.Diagnostic diagProto : request.getDiagnosticsList()) {
      DiagnosticSeverity severity = null;
      Diagnostics.Severity protoSeverity = diagProto.getSeverity();

      switch (protoSeverity) {
        case ERROR:
        case UNKNOWN:
          severity = DiagnosticSeverity.ERROR;
          break;
        case WARNING:
          severity = DiagnosticSeverity.WARNING;
          break;
        case INFORMATION:
          severity = DiagnosticSeverity.INFORMATION;
          break;
        case HINT:
          severity = DiagnosticSeverity.HINT;
          break;
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
}
