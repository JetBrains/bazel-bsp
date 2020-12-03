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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import io.bazel.rules_scala.diagnostics.Diagnostics;
import io.bazel.rules_scala.diagnostics.Diagnostics.Severity;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.bsp.bazel.common.Uri;

public class DiagnosticsDispatcher {

  private static final Logger LOGGER = LogManager.getLogger(DiagnosticsDispatcher.class);

  private final ImmutableMap<Severity, DiagnosticSeverity> CONVERTED_SEVERITY =
      new ImmutableMap.Builder<Diagnostics.Severity, DiagnosticSeverity>()
          .put(Severity.UNKNOWN, DiagnosticSeverity.ERROR)
          .put(Severity.ERROR, DiagnosticSeverity.ERROR)
          .put(Severity.WARNING, DiagnosticSeverity.WARNING)
          .put(Severity.INFORMATION, DiagnosticSeverity.INFORMATION)
          .put(Severity.HINT, DiagnosticSeverity.HINT)
          .build();

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
      LOGGER.info("Inserting diagnostics for path: {}", fileDiagnostics.getPath());

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

  private void addSource(
      Map<Uri, List<PublishDiagnosticsParams>> filesToDiagnostics,
      Uri sourceUri,
      BuildTargetIdentifier target) {
    PublishDiagnosticsParams publishDiagnosticsParams =
        new PublishDiagnosticsParams(
            new TextDocumentIdentifier(sourceUri.toString()), target, new ArrayList<>(), true);

    filesToDiagnostics.put(sourceUri, Lists.newArrayList(publishDiagnosticsParams));
  }

  private List<PublishDiagnosticsParams> convertDiagnostics(
      BuildTargetIdentifier target, Diagnostics.FileDiagnostics request) {
    List<Diagnostic> diagnostics =
        request.getDiagnosticsList().stream()
            .map(this::convertDiagnostic)
            .collect(Collectors.toList());

    PublishDiagnosticsParams publishDiagnosticsParams =
        new PublishDiagnosticsParams(
            new TextDocumentIdentifier(getUriForPath(request.getPath()).toString()),
            target,
            diagnostics,
            true);

    return Lists.newArrayList(publishDiagnosticsParams);
  }

  private Diagnostic convertDiagnostic(Diagnostics.Diagnostic diagProto) {
    Optional<DiagnosticSeverity> severity =
        Optional.ofNullable(CONVERTED_SEVERITY.get(diagProto.getSeverity()));

    Position startPosition =
        new Position(
            diagProto.getRange().getStart().getLine(),
            diagProto.getRange().getStart().getCharacter());
    Position endPosition =
        new Position(
            diagProto.getRange().getEnd().getLine(), diagProto.getRange().getEnd().getCharacter());
    Range range = new Range(startPosition, endPosition);

    Diagnostic diagnostic = new Diagnostic(range, diagProto.getMessage());
    severity.ifPresent(diagnostic::setSeverity);

    return diagnostic;
  }

  private Uri getUriForPath(String path) {
    return Uri.fromExecOrWorkspacePath(
        path, bspServer.getBazelData().getExecRoot(), bspServer.getBazelData().getWorkspaceRoot());
  }
}
