package org.jetbrains.bsp.bazel.server.bep.parsers.error;

import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import ch.epfl.scala.bsp4j.Diagnostic;
import ch.epfl.scala.bsp4j.DiagnosticSeverity;
import ch.epfl.scala.bsp4j.Position;
import ch.epfl.scala.bsp4j.Range;
import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FileDiagnostic {

  private static final Logger LOGGER = LogManager.getLogger(FileDiagnostic.class);

  private final Diagnostic diagnostic;
  private final String fileLocation;
  private final BuildTargetIdentifier target;

  FileDiagnostic(Diagnostic diagnostic, String fileLocation, BuildTargetIdentifier target) {
    this.diagnostic = diagnostic;
    this.fileLocation = fileLocation;
    this.target = target;
  }

  public static Stream<FileDiagnostic> fromError(String error) {
    LOGGER.info("Error: {}", error);

    var targetId = findTargetId(error);

    return Stream.concat(
        Stream.of(createBUILDFileDiagnostic(error, targetId)),
        createSourceFilesDiagnostics(error, targetId));
  }

  private static BuildTargetIdentifier findTargetId(String error) {
    var matcher = Pattern.compile("//[^ :]*:?[^ :]+").matcher(error);

    if (matcher.find()) {
      var rawTargetId = matcher.group();
      return new BuildTargetIdentifier(rawTargetId);
    }

    return new BuildTargetIdentifier("");
  }

  private static FileDiagnostic createBUILDFileDiagnostic(
      String error, BuildTargetIdentifier targetId) {
    var erroredFile = ErrorFileParser.getFileWithError(error);
    var fileInfo = ErrorFileParser.extractFileInfo(error);
    var fileLocation = getFileLocation(erroredFile, fileInfo);
    var message = error.split("\n", 2)[0];

    return createFileDiagnostic(message, fileLocation, targetId);
  }

  private static FileDiagnostic createFileDiagnostic(
      String error, String fileLocation, BuildTargetIdentifier targetId) {
    var position = ErrorPositionParser.getErrorPosition(error);
    var diagnosticRange = new Range(position, position);
    var diagnostic = new Diagnostic(diagnosticRange, error);
    diagnostic.setSeverity(DiagnosticSeverity.ERROR);

    return new FileDiagnostic(diagnostic, fileLocation, targetId);
  }

  private static String getFileLocation(String erroredFile, String fileInfo) {
    var fileLocationUrlEndIndex = fileInfo.indexOf(erroredFile) + erroredFile.length();

    return fileInfo.substring(0, fileLocationUrlEndIndex);
  }

  private static Stream<FileDiagnostic> createSourceFilesDiagnostics(
      String error, BuildTargetIdentifier targetId) {
    var pattern = Pattern.compile("\n((([^\\s\\/:]+\\/)*[^\\/:]+):(\\d+):[^\\^]*\\^)");
    var matcher = pattern.matcher(error);

    var result = new ArrayList<FileDiagnostic>();

    while (matcher.find()) {
      var message = matcher.group(1);
      var filePath = matcher.group(2);
      var errorLine = matcher.group(4);

      result.add(createSourceFileDiagnostic(message, filePath, errorLine, targetId));
    }

    return result.stream();
  }

  private static FileDiagnostic createSourceFileDiagnostic(
      String message, String filePath, String errorLine, BuildTargetIdentifier targetId) {
    var position = new Position(Integer.parseInt(errorLine), 0);
    var diagnosticRange = new Range(position, position);
    var diagnostic = new Diagnostic(diagnosticRange, message);
    diagnostic.setSeverity(DiagnosticSeverity.ERROR);

    return new FileDiagnostic(diagnostic, filePath, targetId);
  }

  public Diagnostic getDiagnostic() {
    return diagnostic;
  }

  public String getFileLocation() {
    return fileLocation;
  }

  public BuildTargetIdentifier getTarget() {
    return target;
  }

  @Override
  public String toString() {
    return "FileDiagnostic{"
        + "diagnostic="
        + diagnostic
        + ", fileLocation='"
        + fileLocation
        + '\''
        + ", target="
        + target
        + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    FileDiagnostic that = (FileDiagnostic) o;
    // TODO normal .equals() fails don't know why...
    return Objects.equals(diagnostic.getMessage(), that.diagnostic.getMessage())
        && Objects.equals(diagnostic.getRange(), that.diagnostic.getRange())
        && Objects.equals(diagnostic.getSeverity(), that.diagnostic.getSeverity())
        && Objects.equals(fileLocation, that.fileLocation)
        && Objects.equals(target, that.target);
  }

  @Override
  public int hashCode() {
    return Objects.hash(diagnostic, fileLocation, target);
  }
}
