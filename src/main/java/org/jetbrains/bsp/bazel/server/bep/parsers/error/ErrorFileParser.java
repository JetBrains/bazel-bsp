package org.jetbrains.bsp.bazel.server.bep.parsers.error;

import org.jetbrains.bsp.bazel.commons.Constants;

final class ErrorFileParser {

  private static final String AT_PATH = " at /";
  private static final String ERROR_PREAMBLE = "ERROR: ";

  public static String getFileWithError(String error) {
    if (isInWorkspaceFile(error)) {
      return Constants.WORKSPACE_FILE_NAME;
    }

    return Constants.BUILD_FILE_NAME;
  }

  public static boolean isInWorkspaceFile(String error) {
    return error.contains("/" + Constants.WORKSPACE_FILE_NAME + ":");
  }

  public static boolean isInBuildFile(String error) {
    return error.contains("/" + Constants.BUILD_FILE_NAME + ":");
  }

  public static String extractFileInfo(String error) {
    int fileInfoStartIndex = getFileInfoStartIndex(error);

    return error.substring(fileInfoStartIndex);
  }

  private static int getFileInfoStartIndex(String error) {
    int atPathPrefixEndingIndex = error.indexOf(AT_PATH) + AT_PATH.length() - 1;
    int errorPreamblePrefixEndingIndex = error.indexOf(ERROR_PREAMBLE) + ERROR_PREAMBLE.length();

    if (error.contains(AT_PATH)) {
      return atPathPrefixEndingIndex;
    }

    return errorPreamblePrefixEndingIndex;
  }
}
