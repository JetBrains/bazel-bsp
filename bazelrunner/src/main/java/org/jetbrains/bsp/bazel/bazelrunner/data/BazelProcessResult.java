package org.jetbrains.bsp.bazel.bazelrunner.data;

import ch.epfl.scala.bsp4j.StatusCode;
import com.google.common.base.Suppliers;
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

  public StatusCode getStatusCode() {
    return ExitCodeMapper.mapExitCode(exitCode);
  }

  public List<String> getStdout() {
    return stdout.get();
  }

  public String getJoinedStderr() {
    List<String> lines = stderr.get();
    return String.join(LINES_DELIMITER, lines);
  }

  private Supplier<List<String>> memoized(InputStream input) {
    return Suppliers.memoize(() -> BazelStreamReader.drainStream(input));
  }
}
