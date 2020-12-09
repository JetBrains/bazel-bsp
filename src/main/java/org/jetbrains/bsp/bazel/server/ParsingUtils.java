package org.jetbrains.bsp.bazel.server;

import ch.epfl.scala.bsp4j.Diagnostic;
import ch.epfl.scala.bsp4j.DiagnosticSeverity;
import ch.epfl.scala.bsp4j.Position;
import ch.epfl.scala.bsp4j.Range;
import com.google.common.base.Splitter;
import com.google.common.io.Files;
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos.Progress;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.bsp.bazel.common.Constants;

public class ParsingUtils {

  private static final Logger LOGGER = LogManager.getLogger(ParsingUtils.class);

  public static URI parseUri(String uri) {
    try {
      return new URI(uri);
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  // TODO FOR REVIEW: naming suggestions? what exactly is the thing that it returns?
  public static List<String> parseClasspathFromFile(URI protoPathUri) {
    try {
      return Files.readLines(new File(protoPathUri), StandardCharsets.UTF_8).stream()
          .map(line -> Splitter.on("\"").splitToList(line))
          .peek(
              parts -> {
                if (parts.size() != 3) {
                  // TODO FOR REVIEW: verify the slight change of semantics
                  // TODO is it OK to throw an exception at the beginning if any line is corrupted?
                  throw new RuntimeException("Wrong parts in sketchy textproto parsing: " + parts);
                }
              })
          .map(parts -> parts.get(1))
          .collect(Collectors.toList());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static Map<String, List<Diagnostic>> parseStderrDiagnostics(Progress progress) {
    return Arrays.stream(progress.getStderr().split("\n"))
        .filter(
            error -> error.contains("ERROR") && (isInWorkspaceFile(error) || isInBuildFile(error)))
        .map(ParsingUtils::parseFileDiagnostic)
        .collect(
            Collectors.groupingBy(
                FileDiagnostic::getFileLocation,
                Collectors.mapping(FileDiagnostic::getDiagnostic, Collectors.toList())));
  }

  private static FileDiagnostic parseFileDiagnostic(String error) {
    String erroredFile =
        error.contains(Constants.WORKSPACE_FILE_NAME)
            ? Constants.WORKSPACE_FILE_NAME
            : Constants.BUILD_FILE_NAME;
    int urlEnd = error.indexOf(erroredFile) + erroredFile.length();
    String fileInfo = extractFileInfo(error);
    String fileLocation = fileInfo.substring(0, urlEnd);

    String lineLocationDelimiter = error.contains(" at /") ? ":" : "(:)|( )";
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
        error.contains(" at /") ? error.indexOf(" at /") + 4 : error.indexOf("ERROR: ") + 7;
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
