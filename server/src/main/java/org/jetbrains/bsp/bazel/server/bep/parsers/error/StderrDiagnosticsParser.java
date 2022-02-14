package org.jetbrains.bsp.bazel.server.bep.parsers.error;

import ch.epfl.scala.bsp4j.Diagnostic;
import com.google.common.base.Splitter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class StderrDiagnosticsParser {

  private static final String ERROR = "ERROR";

  private static final String STDERR_DELIMITER = "\n";

  public static Map<String, List<FileDiagnostic>> parse2(String stderr) {
    return splitStderr2(stderr)
        .filter(StderrDiagnosticsParser::isError)
        .filter(StderrDiagnosticsParser::isBazelError)
        .flatMap(FileDiagnostic::fromError2)
        .collect(Collectors.groupingBy(FileDiagnostic::getFileLocation, Collectors.toList()));
  }

  public static Map<String, List<Diagnostic>> parse(String stderr) {
    return splitStderr(stderr)
        .filter(StderrDiagnosticsParser::isError)
        .filter(StderrDiagnosticsParser::isBazelError)
        .map(FileDiagnostic::fromError)
        .collect(
            Collectors.groupingBy(
                FileDiagnostic::getFileLocation,
                Collectors.mapping(FileDiagnostic::getDiagnostic, Collectors.toList())));
  }

  private static Stream<String> splitStderr(String stderr) {
    return Splitter.on(STDERR_DELIMITER).splitToList(stderr).stream();
  }

  private static Stream<String> splitStderr2(String stderr) {
    return Splitter.onPattern("(?=(ERROR:))").splitToList(stderr).stream();
  }

  private static boolean isError(String stderrPart) {
    return stderrPart.contains(ERROR);
  }

  private static boolean isBazelError(String error) {
    return ErrorFileParser.isInWorkspaceFile(error) || ErrorFileParser.isInBuildFile(error);
  }
}
