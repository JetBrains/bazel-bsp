package org.jetbrains.bsp.bazel.server;

import ch.epfl.scala.bsp4j.Diagnostic;
import ch.epfl.scala.bsp4j.DiagnosticSeverity;
import ch.epfl.scala.bsp4j.Position;
import ch.epfl.scala.bsp4j.Range;
import com.google.common.base.Splitter;
import com.google.common.io.Files;
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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

  // TODO(gerardd33) refactor method
  public static Map<String, List<Diagnostic>> parseStderrDiagnostics(
      BuildEventStreamProtos.Progress progress) {
    Map<String, List<Diagnostic>> fileDiagnostics = new HashMap<>();

    Arrays.stream(progress.getStderr().split("\n"))
        .filter(
            error ->
                error.contains("ERROR")
                    && (error.contains("/" + Constants.WORKSPACE_FILE_NAME + ":")
                        || error.contains("/" + Constants.BUILD_FILE_NAME + ":")))
        .forEach(
            error -> {
              String erroredFile =
                  error.contains(Constants.WORKSPACE_FILE_NAME)
                      ? Constants.WORKSPACE_FILE_NAME
                      : Constants.BUILD_FILE_NAME;
              String[] lineLocation;
              String fileLocation;

              if (error.contains(" at /")) {
                int endOfMessage = error.indexOf(" at /");
                String fileInfo = error.substring(endOfMessage + 4);

                int urlEnd = fileInfo.indexOf(erroredFile) + erroredFile.length();
                fileLocation = fileInfo.substring(0, urlEnd);
                lineLocation = fileInfo.substring(urlEnd + 1).split(":");
              } else {
                int urlEnd = error.indexOf(erroredFile) + erroredFile.length();
                fileLocation = error.substring(error.indexOf("ERROR: ") + 7, urlEnd);
                lineLocation = error.substring(urlEnd + 1).split("(:)|( )");
              }

              LOGGER.info("Error: {}", error);
              LOGGER.info("File location: {}", fileLocation);
              LOGGER.info("Line location: {}", Arrays.toString(lineLocation));

              Position position =
                  new Position(
                      Integer.parseInt(lineLocation[0]), Integer.parseInt(lineLocation[1]));

              Diagnostic diagnostic = new Diagnostic(new Range(position, position), error);
              diagnostic.setSeverity(DiagnosticSeverity.ERROR);

              List<Diagnostic> diagnostics =
                  fileDiagnostics.getOrDefault(fileLocation, new ArrayList<>());
              diagnostics.add(diagnostic);

              fileDiagnostics.put(fileLocation, diagnostics);
            });

    return fileDiagnostics;
  }
}
