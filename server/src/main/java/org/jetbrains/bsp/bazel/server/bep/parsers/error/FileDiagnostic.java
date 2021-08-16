package org.jetbrains.bsp.bazel.server.bep.parsers.error;

import ch.epfl.scala.bsp4j.Diagnostic;
import ch.epfl.scala.bsp4j.DiagnosticSeverity;
import ch.epfl.scala.bsp4j.Position;
import ch.epfl.scala.bsp4j.Range;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

class FileDiagnostic {

  private static final Logger LOGGER = LogManager.getLogger(FileDiagnostic.class);

  private final Diagnostic diagnostic;
  private final String fileLocation;

  private FileDiagnostic(Diagnostic diagnostic, String fileLocation) {
    this.diagnostic = diagnostic;
    this.fileLocation = fileLocation;
  }

  public static FileDiagnostic fromError(String error) {
    LOGGER.info("Error: {}", error);

    String erroredFile = ErrorFileParser.getFileWithError(error);
    String fileInfo = ErrorFileParser.extractFileInfo(error);
    String fileLocation = getFileLocation(erroredFile, fileInfo);

    return createFileDiagnostic(error, fileLocation);
  }

  private static FileDiagnostic createFileDiagnostic(String error, String fileLocation) {
    Position position = ErrorPositionParser.getErrorPosition(error);
    Range diagnosticRange = new Range(position, position);
    Diagnostic diagnostic = new Diagnostic(diagnosticRange, error);
    diagnostic.setSeverity(DiagnosticSeverity.ERROR);

    return new FileDiagnostic(diagnostic, fileLocation);
  }

  private static String getFileLocation(String erroredFile, String fileInfo) {
    int fileLocationUrlEndIndex = fileInfo.indexOf(erroredFile) + erroredFile.length();

    return fileInfo.substring(0, fileLocationUrlEndIndex);
  }

  public Diagnostic getDiagnostic() {
    return diagnostic;
  }

  public String getFileLocation() {
    return fileLocation;
  }
}
