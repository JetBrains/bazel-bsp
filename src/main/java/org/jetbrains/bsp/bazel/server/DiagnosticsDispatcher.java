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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.bsp.bazel.common.Uri;

public class DiagnosticsDispatcher {

  private static final Logger LOGGER = LogManager.getLogger(DiagnosticsDispatcher.class);

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
      LOGGER.info("Inserting diagnostics for path: " + fileDiagnostics.getPath());

      filesToDiagnostics.put(
          getUriForPath(fileDiagnostics.getPath()), convertDiagnostics(target, fileDiagnostics));
    }
  }

  public void emitDiagnostics(
      Map<Uri, List<PublishDiagnosticsParams>> filesToDiagnostics, BuildTargetIdentifier target) {
    for (SourceItem source : bspServer.getCachedBuildTargetSources(target)) {
      Uri sourceUri = Uri.fromFileUri(source.getUri());
      if (!filesToDiagnostics.containsKey(sourceUri)) {
        addSource(filesToDiagnostics, sourceUri, target);
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

  private void addSource(Map<Uri, List<PublishDiagnosticsParams>> filesToDiagnostics,
      Uri sourceUri, BuildTargetIdentifier target) {
    PublishDiagnosticsParams publishDiagnosticsParams = new PublishDiagnosticsParams(
        new TextDocumentIdentifier(sourceUri.toString()),
        target,
        new ArrayList<>(),
        true);

    filesToDiagnostics.put(
        sourceUri,
        Lists.newArrayList(publishDiagnosticsParams));
  }

  private List<PublishDiagnosticsParams> convertDiagnostics(
      BuildTargetIdentifier target, Diagnostics.FileDiagnostics request) {
    List<Diagnostic> diagnostics = new ArrayList<>();

    for (Diagnostics.Diagnostic diagProto : request.getDiagnosticsList()) {
      Diagnostic diagnostic = convertDiagnostic(diagProto);
      diagnostics.add(diagnostic);
    }

    PublishDiagnosticsParams publishDiagnosticsParams = new PublishDiagnosticsParams(
        new TextDocumentIdentifier(getUriForPath(request.getPath()).toString()),
        target,
        diagnostics,
        true);

    return Lists.newArrayList(publishDiagnosticsParams);
  }

  private Diagnostic convertDiagnostic(Diagnostics.Diagnostic diagProto) {
    DiagnosticSeverity severity = convertSeverity(diagProto.getSeverity());

    Position startPosition = new Position(
        diagProto.getRange().getStart().getLine(),
        diagProto.getRange().getStart().getCharacter());
    Position endPosition = new Position(
        diagProto.getRange().getEnd().getLine(),
        diagProto.getRange().getEnd().getCharacter());
    Range range = new Range(startPosition, endPosition);

    Diagnostic diagnostic = new Diagnostic(range, diagProto.getMessage());
    if (severity != null) {
      diagnostic.setSeverity(severity);
    }

    return diagnostic;
  }

  private DiagnosticSeverity convertSeverity(Diagnostics.Severity protoSeverity) {
    switch (protoSeverity) {
      case ERROR:
      case UNKNOWN:
        return DiagnosticSeverity.ERROR;
      case WARNING:
        return DiagnosticSeverity.WARNING;
      case INFORMATION:
        return DiagnosticSeverity.INFORMATION;
      case HINT:
        return DiagnosticSeverity.HINT;
      default:
        return null;
    }
  }

  private Uri getUriForPath(String path) {
    return Uri.fromExecOrWorkspacePath(
        path, bspServer.getBazelData().getExecRoot(), bspServer.getBazelData().getWorkspaceRoot());
  }
}
