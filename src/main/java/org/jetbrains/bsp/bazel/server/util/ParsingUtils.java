package org.jetbrains.bsp.bazel.server.util;

import ch.epfl.scala.bsp4j.BuildTargetTag;
import ch.epfl.scala.bsp4j.Diagnostic;
import ch.epfl.scala.bsp4j.DiagnosticSeverity;
import ch.epfl.scala.bsp4j.Position;
import ch.epfl.scala.bsp4j.Range;
import ch.epfl.scala.bsp4j.StatusCode;
import com.google.common.base.Splitter;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.bsp.bazel.common.Constants;

public class ParsingUtils {

  private static final Logger LOGGER = LogManager.getLogger(ParsingUtils.class);
  private static final String ERROR = "ERROR";
  private static final String ERROR_PREAMBLE = ERROR + ": ";
  private static final String AT_PATH = " at /";

  public static URI parseUri(String uri) {
    try {
      return new URI(uri);
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  public static StatusCode parseExitCode(int exitCode) {
    switch (exitCode) {
      case 0:
        return StatusCode.OK;
      case 8:
        return StatusCode.CANCELLED;
      default:
        return StatusCode.ERROR;
    }
  }

  public static String convertOutputToPath(String output, String prefix) {
    String pathToFile = output.replaceAll("(//|:)", "/");
    return prefix + pathToFile;
  }

  public static String getRuleType(String ruleClass) {
    if (ruleClass.contains(Constants.LIBRARY_RULE_TYPE)) {
      return BuildTargetTag.LIBRARY;
    }

    if (ruleClass.contains(Constants.BINARY_RULE_TYPE)) {
      return BuildTargetTag.APPLICATION;
    }

    if (ruleClass.contains(Constants.TEST_RULE_TYPE)) {
      return BuildTargetTag.TEST;
    }

    return BuildTargetTag.NO_IDE;
  }

  public static String getJavaVersion() {
    String version = System.getProperty("java.version");
    if (version.startsWith("1.")) {
      version = version.substring(0, 3);
    } else {
      int dot = version.indexOf(".");
      if (dot != -1) {
        version = version.substring(0, dot);
      }
    }
    return version;
  }

  public static String getMnemonics(String targetsUnion, List<String> languageIds) {
    return languageIds.stream()
        .filter(Objects::nonNull)
        .map(mnemonic -> "mnemonic(" + mnemonic + ", " + targetsUnion + ")")
        .collect(Collectors.joining(" union "));
  }

  public static List<String> parseClasspathFromAspect(URI dependenciesAspectOutput) {
    try {
      return Files.readLines(new File(dependenciesAspectOutput), StandardCharsets.UTF_8).stream()
          .map(line -> Splitter.on("\"").splitToList(line))
          .peek(
              parts -> {
                if (parts.size() != 3) {
                  throw new RuntimeException("Wrong parts in sketchy textproto parsing: " + parts);
                }
              })
          .map(parts -> parts.get(1))
          .collect(Collectors.toList());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static Map<String, List<Diagnostic>> parseStderrDiagnostics(String stderr) {
    return Arrays.stream(stderr.split("\n"))
        .filter(
            error -> error.contains(ERROR) && (isInWorkspaceFile(error) || isInBuildFile(error)))
        .map(ParsingUtils::parseFileDiagnostic)
        .collect(
            Collectors.groupingBy(
                FileDiagnostic::getFileLocation,
                Collectors.mapping(FileDiagnostic::getDiagnostic, Collectors.toList())));
  }

  private static FileDiagnostic parseFileDiagnostic(String error) {
    String erroredFile =
        isInWorkspaceFile(error) ? Constants.WORKSPACE_FILE_NAME : Constants.BUILD_FILE_NAME;
    String fileInfo = extractFileInfo(error);
    int urlEnd = fileInfo.indexOf(erroredFile) + erroredFile.length();
    String fileLocation = fileInfo.substring(0, urlEnd);

    String lineLocationDelimiter = error.contains(AT_PATH) ? ":" : "(:)|( )";
    String[] lineLocation = fileInfo.substring(urlEnd + 1).split(lineLocationDelimiter);

    LOGGER.info("Error: {}", error);

    Position position =
        new Position(Integer.parseInt(lineLocation[0]), Integer.parseInt(lineLocation[1]));
    Diagnostic diagnostic = new Diagnostic(new Range(position, position), error);
    diagnostic.setSeverity(DiagnosticSeverity.ERROR);

    return new FileDiagnostic(diagnostic, fileLocation);
  }

  private static String extractFileInfo(String error) {
    int fileInfoStartIndex =
        error.contains(AT_PATH)
            ? error.indexOf(AT_PATH) + AT_PATH.length() - 1
            : error.indexOf(ERROR_PREAMBLE) + ERROR_PREAMBLE.length();
    return error.substring(fileInfoStartIndex);
  }

  private static boolean isInWorkspaceFile(String error) {
    return error.contains("/" + Constants.WORKSPACE_FILE_NAME + ":");
  }

  private static boolean isInBuildFile(String error) {
    return error.contains("/" + Constants.BUILD_FILE_NAME + ":");
  }

  private static class FileDiagnostic {

    private final Diagnostic diagnostic;

    private final String fileLocation;

    public FileDiagnostic(Diagnostic diagnostic, String fileLocation) {
      this.diagnostic = diagnostic;
      this.fileLocation = fileLocation;
    }

    public Diagnostic getDiagnostic() {
      return diagnostic;
    }

    public String getFileLocation() {
      return fileLocation;
    }
  }
}
