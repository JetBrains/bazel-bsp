package org.jetbrains.bsp.bazel.server.bep.parsers.error;

import ch.epfl.scala.bsp4j.Position;

final class ErrorPositionParser {

  private static final Integer ERROR_LINE_INDEX_OF_LINE = 0;
  private static final Integer ERROR_LINE_INDEX_OF_CHARACTER = 1;

  private static final String AT_PATH = " at /";
  private static final String LINE_LOCATION_AT_PATH_DELIMITER = ":";
  private static final String LINE_LOCATION_DELIMITER = "(:)|( )";

  public static Position getErrorPosition(String error) {
    String[] lineLocation = getLineLocations(error);

    return getPosition(lineLocation);
  }

  private static String[] getLineLocations(String error) {
    String fileInfo = ErrorFileParser.extractFileInfo(error);
    String erroredFile = ErrorFileParser.getFileWithError(error);
    int locationUrlEndingIndex = fileInfo.indexOf(erroredFile) + erroredFile.length();

    String lineLocationDelimiter = getLineLocationDelimiterRegardingErrorType(error);

    return fileInfo.substring(locationUrlEndingIndex + 1).split(lineLocationDelimiter);
  }

  private static String getLineLocationDelimiterRegardingErrorType(String error) {
    if (error.contains(AT_PATH)) {
      return LINE_LOCATION_AT_PATH_DELIMITER;
    }

    return LINE_LOCATION_DELIMITER;
  }

  private static Position getPosition(String[] lineLocation) {
    Integer lineNumber = getErrorLineNumber(lineLocation);
    Integer characterIndex = getErrorCharacterIndex(lineLocation);

    return new Position(lineNumber, characterIndex);
  }

  private static Integer getErrorLineNumber(String[] lineLocation) {
    return Integer.parseInt(lineLocation[ERROR_LINE_INDEX_OF_LINE]);
  }

  private static Integer getErrorCharacterIndex(String[] lineLocation) {
    return Integer.parseInt(lineLocation[ERROR_LINE_INDEX_OF_CHARACTER]);
  }
}
