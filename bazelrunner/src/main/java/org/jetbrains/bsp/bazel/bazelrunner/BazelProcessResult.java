package org.jetbrains.bsp.bazel.bazelrunner;

import ch.epfl.scala.bsp4j.StatusCode;
import io.vavr.collection.List;
import org.jetbrains.bsp.bazel.bazelrunner.outputs.OutputCollector;
import org.jetbrains.bsp.bazel.commons.ExitCodeMapper;

public class BazelProcessResult {

  private final OutputCollector stdout;
  private final OutputCollector stderr;
  private final int exitCode;

  public BazelProcessResult(OutputCollector stdout, OutputCollector stderr, int exitCode) {
    this.stdout = stdout;
    this.stderr = stderr;
    this.exitCode = exitCode;
  }

  public boolean isNotSuccess() {
    return getStatusCode() != StatusCode.OK;
  }

  public StatusCode getStatusCode() {
    return ExitCodeMapper.mapExitCode(exitCode);
  }

  public List<String> stdoutLines() {
    return stdout.lines();
  }

  public String stdout() {
    return stdout.output();
  }

  public List<String> stderrLines() {
    return stderr.lines();
  }

  public String stderr() {
    return stderr.output();
  }
}
