package org.jetbrains.bsp.bazel.server.bep.parsers.error;

import com.google.common.base.Splitter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class StderrDiagnosticsParser {

  private static final String ERROR = "ERROR";

  public static Map<String, List<FileDiagnostic>> parse(String stderr) {
    return splitStderr(stderr)
        .filter(StderrDiagnosticsParser::isError)
        .filter(StderrDiagnosticsParser::isBazelError)
        .flatMap(FileDiagnostic::fromError)
        .collect(Collectors.groupingBy(FileDiagnostic::getFileLocation, Collectors.toList()));
  }

  private static Stream<String> splitStderr(String stderr) {
    return Splitter.onPattern("(?=(ERROR:))").splitToList(stderr).stream();
  }

  private static boolean isError(String stderrPart) {
    return stderrPart.contains(ERROR);
  }

  private static boolean isBazelError(String error) {
    return ErrorFileParser.isInWorkspaceFile(error) || ErrorFileParser.isInBuildFile(error);
  }
}
