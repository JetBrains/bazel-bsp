package org.jetbrains.bsp.bazel.server.bep;

import ch.epfl.scala.bsp4j.BuildClient;
import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import ch.epfl.scala.bsp4j.Diagnostic;
import ch.epfl.scala.bsp4j.DiagnosticSeverity;
import ch.epfl.scala.bsp4j.Position;
import ch.epfl.scala.bsp4j.PublishDiagnosticsParams;
import ch.epfl.scala.bsp4j.Range;
import ch.epfl.scala.bsp4j.TextDocumentIdentifier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import io.bazel.rules_scala.diagnostics.Diagnostics;
import io.bazel.rules_scala.diagnostics.Diagnostics.FileDiagnostics;
import io.bazel.rules_scala.diagnostics.Diagnostics.Severity;
import io.bazel.rules_scala.diagnostics.Diagnostics.TargetDiagnostics;
import io.vavr.control.Option;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.bsp.bazel.bazelrunner.BazelData;
import org.jetbrains.bsp.bazel.commons.Uri;

public class BepDiagnosticsDispatcher {

  private static final Logger LOGGER = LogManager.getLogger(BepDiagnosticsDispatcher.class);

  private static final Map<Severity, DiagnosticSeverity> CONVERTED_SEVERITY =
      new ImmutableMap.Builder<Severity, DiagnosticSeverity>()
          .put(Severity.UNKNOWN, DiagnosticSeverity.ERROR)
          .put(Severity.ERROR, DiagnosticSeverity.ERROR)
          .put(Severity.WARNING, DiagnosticSeverity.WARNING)
          .put(Severity.INFORMATION, DiagnosticSeverity.INFORMATION)
          .put(Severity.HINT, DiagnosticSeverity.HINT)
          .build();

  private final BazelData bazelData;
  private final BuildClient bspClient;

  private final Map<BuildTargetIdentifier, List<URI>> buildTargetsSources = new HashMap<>();

  public BepDiagnosticsDispatcher(BazelData bazelData, BuildClient bspClient) {
    this.bazelData = bazelData;
    this.bspClient = bspClient;
  }

  public Map<Uri, List<PublishDiagnosticsParams>> collectDiagnostics(
      BuildTargetIdentifier target, String diagnosticsLocation) {
    try {
      var diagnosticsPath = Paths.get(diagnosticsLocation);

      if (Files.notExists(diagnosticsPath)) {
        LOGGER.warn("Diagnostics file does not exist at: {}", diagnosticsLocation);
        return Collections.emptyMap();
      }

      var targetDiagnostics = TargetDiagnostics.parseFrom(Files.readAllBytes(diagnosticsPath));

      return targetDiagnostics.getDiagnosticsList().stream()
          .peek(diagnostics -> LOGGER.debug("Collected diagnostics at: {}", diagnostics.getPath()))
          .collect(
              Collectors.toMap(
                  diagnostics -> getUriForPath(diagnostics.getPath()),
                  diagnostics -> convertDiagnostics(target, diagnostics)));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void emitDiagnostics(
      Map<Uri, List<PublishDiagnosticsParams>> filesToDiagnostics, BuildTargetIdentifier target) {
    buildTargetsSources.getOrDefault(target, ImmutableList.of()).stream()
        .map(source -> Uri.fromFileUri(source.toString()))
        .forEach(sourceUri -> addSourceAndPublish(sourceUri, filesToDiagnostics, target));
  }

  private void addSourceAndPublish(
      Uri sourceUri,
      Map<Uri, List<PublishDiagnosticsParams>> filesToDiagnostics,
      BuildTargetIdentifier target) {
    PublishDiagnosticsParams publishDiagnosticsParams =
        new PublishDiagnosticsParams(
            new TextDocumentIdentifier(sourceUri.toString()), target, ImmutableList.of(), true);

    filesToDiagnostics.putIfAbsent(sourceUri, Lists.newArrayList(publishDiagnosticsParams));

    filesToDiagnostics.values().stream()
        .flatMap(List::stream)
        .forEach(bspClient::onBuildPublishDiagnostics);
  }

  private List<PublishDiagnosticsParams> convertDiagnostics(
      BuildTargetIdentifier target, FileDiagnostics request) {
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
    var severity = Option.of(CONVERTED_SEVERITY.get(diagProto.getSeverity()));

    Position startPosition =
        new Position(
            diagProto.getRange().getStart().getLine(),
            diagProto.getRange().getStart().getCharacter());
    Position endPosition =
        new Position(
            diagProto.getRange().getEnd().getLine(), diagProto.getRange().getEnd().getCharacter());
    Range range = new Range(startPosition, endPosition);

    Diagnostic diagnostic = new Diagnostic(range, diagProto.getMessage());
    severity.forEach(diagnostic::setSeverity);

    return diagnostic;
  }

  private Uri getUriForPath(String path) {
    return Uri.fromExecOrWorkspacePath(path, bazelData.getExecRoot(), bazelData.getWorkspaceRoot());
  }

  public Map<BuildTargetIdentifier, List<URI>> getBuildTargetsSources() {
    return buildTargetsSources;
  }
}
