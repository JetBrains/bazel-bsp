package org.jetbrains.bsp.bazel.server.bep.parsers;

import com.google.common.base.Splitter;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public final class ClasspathParser {

  private static final String ASPECTS_OUTPUT_FILE_LINE_DELIMITER = "\"";
  private static final Integer ASPECTS_OUTPUT_FILE_CLASSPATH_VALID_PARTS_NUMBER = 3;
  private static final Integer ASPECTS_OUTPUT_FILE_CLASSPATH_LINE_INDEX = 1;

  public static List<String> fromAspect(URI dependenciesAspectOutput) {
    try {
      return fromAspectOrThrow(dependenciesAspectOutput);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static List<String> fromAspectOrThrow(URI dependenciesAspectOutput) throws IOException {
    File dependenciesAspectOutputFile = new File(dependenciesAspectOutput);

    return Files.readLines(dependenciesAspectOutputFile, StandardCharsets.UTF_8).stream()
        .map(ClasspathParser::splitAspectsOutputFileLine)
        .peek(ClasspathParser::throwExceptionIfLineHasWrongFormat)
        .map(parts -> parts.get(ASPECTS_OUTPUT_FILE_CLASSPATH_LINE_INDEX))
        .collect(Collectors.toList());
  }

  private static List<String> splitAspectsOutputFileLine(String line) {
    return Splitter.on(ASPECTS_OUTPUT_FILE_LINE_DELIMITER).splitToList(line);
  }

  private static void throwExceptionIfLineHasWrongFormat(List<String> lineParts) {
    if (lineParts.size() != ASPECTS_OUTPUT_FILE_CLASSPATH_VALID_PARTS_NUMBER) {
      throw new RuntimeException("Wrong parts in sketchy textproto parsing: " + lineParts);
    }
  }
}
