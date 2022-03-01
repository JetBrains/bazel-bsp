package org.jetbrains.bsp.bazel.bazelrunner;

import java.io.InputStream;
import java.util.Optional;
import org.jetbrains.bsp.bazel.bazelrunner.data.BazelProcessResult;
import org.jetbrains.bsp.bazel.server.loggers.BuildClientLogger;

public class BazelProcess {
  private final Process process;

  BazelProcess(Process process) {
    this.process = process;
  }

  /**
   * Waits for the process to finish and returns the result.
   * WARNING: if used incorrectly, this method may cause a deadlock.
   */
  public BazelProcessResult waitAndGetResult() {
    try {
      int exitCode = process.waitFor();
      return new BazelProcessResult(process.getInputStream(), process.getErrorStream(), exitCode);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  public InputStream getStdoutStream() {
    return process.getInputStream();
  }

  public InputStream getStderrStream() {
    return process.getErrorStream();
  }
}
