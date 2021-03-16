package org.jetbrains.bsp.bazel.server.bazel.data;

import ch.epfl.scala.bsp4j.StatusCode;
import java.io.InputStream;
import java.util.List;
import org.jetbrains.bsp.bazel.server.bazel.utils.BazelStreamReader;
import org.jetbrains.bsp.bazel.server.bazel.utils.ExitCodeMapper;

public class BazelProcessResult {

  private static final String LINES_DELIMITER = "\n";

  private final InputStream stdout;
  private final InputStream stderr;
  private final int exitCode;

  public BazelProcessResult(InputStream stdout, InputStream stderr, int exitCode) {
    this.stdout = stdout;
    this.stderr = stderr;
    this.exitCode = exitCode;
  }

  public StatusCode getStatusCode() {
    return ExitCodeMapper.mapExitCode(exitCode);
  }

  public List<String> getStdout() {
    return BazelStreamReader.drainStream(stdout);
  }

  public String getJoinedStderr() {
    List<String> lines = BazelStreamReader.drainStream(stderr);
    return String.join(LINES_DELIMITER, lines);
  }
}
