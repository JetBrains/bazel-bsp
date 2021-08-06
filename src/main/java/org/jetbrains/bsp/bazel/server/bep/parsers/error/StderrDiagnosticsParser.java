package org.jetbrains.bsp.bazel.server.bep.parsers.error;

import ch.epfl.scala.bsp4j.Diagnostic;
import com.google.common.base.Splitter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class StderrDiagnosticsParser {

  private static final String ERROR = "ERROR";

  private static final String STDERR_DELIMITER = "\n";

  public static Map<String, List<Diagnostic>> parse(String stderr) {
    return splitStderr(stderr).stream()
        .filter(StderrDiagnosticsParser::isError)
        .filter(StderrDiagnosticsParser::isBazelError)
        .map(FileDiagnostic::fromError)
        .collect(
            Collectors.groupingBy(
                FileDiagnostic::getFileLocation,
                Collectors.mapping(FileDiagnostic::getDiagnostic, Collectors.toList())));
  }

  private static List<String> splitStderr(String stderr) {
    return Splitter.on(STDERR_DELIMITER).splitToList(stderr);
  }

  private static boolean isError(String stderrPart) {
    return stderrPart.contains(ERROR);
  }

  private static boolean isBazelError(String error) {
    return ErrorFileParser.isInWorkspaceFile(error) || ErrorFileParser.isInBuildFile(error);
  }
}
