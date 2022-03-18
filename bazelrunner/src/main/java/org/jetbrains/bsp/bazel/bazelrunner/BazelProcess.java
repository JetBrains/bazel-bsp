package org.jetbrains.bsp.bazel.bazelrunner;

import java.io.InputStream;
import org.jetbrains.bsp.bazel.bazelrunner.outputs.AsyncOutputProcessor;
import org.jetbrains.bsp.bazel.bazelrunner.outputs.OutputCollector;
import org.jetbrains.bsp.bazel.logger.BuildClientLogger;

public class BazelProcess {

  private final Process process;
  private final BuildClientLogger logger;

  BazelProcess(Process process, BuildClientLogger logger) {
    this.process = process;
    this.logger = logger;
  }

  public BazelProcessResult waitAndGetResult() {
    try {
      var stdoutCollector = new OutputCollector();
      var stderrCollector = new OutputCollector();
      var outputProcessor = new AsyncOutputProcessor();
      outputProcessor.start(process.getInputStream(), stdoutCollector, logger::logMessage);
      outputProcessor.start(process.getErrorStream(), stderrCollector, logger::logError);
      var exitCode = process.waitFor();
      outputProcessor.shutdown();
      return new BazelProcessResult(stdoutCollector, stderrCollector, exitCode);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  public InputStream getInputStream() {
    return process.getInputStream();
  }
}
