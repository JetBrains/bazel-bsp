package org.jetbrains.bsp.bazel.bazelrunner.data;

import ch.epfl.scala.bsp4j.StatusCode;
import io.vavr.Lazy;
import java.io.InputStream;
import java.util.List;
import java.util.function.Supplier;
import org.jetbrains.bsp.bazel.bazelrunner.utils.BazelStreamReader;
import org.jetbrains.bsp.bazel.commons.ExitCodeMapper;

public class BazelProcessResult {

  private static final String LINES_DELIMITER = "\n";

  private final Supplier<List<String>> stdout;
  private final Supplier<List<String>> stderr;
  private final int exitCode;

  public BazelProcessResult(InputStream stdout, InputStream stderr, int exitCode) {
    this.stdout = memoized(stdout);
    this.stderr = memoized(stderr);
    this.exitCode = exitCode;
  }

  public boolean isNotSuccess() {
    return getStatusCode() != StatusCode.OK;
  }

  public StatusCode getStatusCode() {
    return ExitCodeMapper.mapExitCode(exitCode);
  }

  public List<String> getStdoutLines() {
    return stdout.get();
  }

  public String getStdout() {
    return String.join(LINES_DELIMITER, getStdoutLines());
  }

  public String getStderr() {
    List<String> lines = stderr.get();
    return String.join(LINES_DELIMITER, lines);
  }

  private Lazy<List<String>> memoized(InputStream input) {
    return Lazy.of(() -> BazelStreamReader.drainStream(input));
  }
}
