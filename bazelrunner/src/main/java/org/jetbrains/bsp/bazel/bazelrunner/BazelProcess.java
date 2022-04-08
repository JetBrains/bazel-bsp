package org.jetbrains.bsp.bazel.bazelrunner;

import java.io.InputStream;
import java.time.Duration;
import java.util.function.Function;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.bsp.bazel.bazelrunner.outputs.AsyncOutputProcessor;
import org.jetbrains.bsp.bazel.bazelrunner.outputs.OutputCollector;
import org.jetbrains.bsp.bazel.commons.Format;
import org.jetbrains.bsp.bazel.commons.Stopwatch;
import org.jetbrains.bsp.bazel.logger.BspClientLogger;

public class BazelProcess {

  private final Process process;
  private final BspClientLogger logger;

  private static final Logger LOGGER = LogManager.getLogger(BazelProcess.class);

  BazelProcess(Process process, BspClientLogger logger) {
    this.process = process;
    this.logger = logger;
  }

  public BazelProcessResult waitAndGetResult() {
    try {
      var stdoutCollector = new OutputCollector();
      var stderrCollector = new OutputCollector();
      var outputProcessor = new AsyncOutputProcessor();
      var stopwatch = Stopwatch.start();
      outputProcessor.start(
          process.getInputStream(), stdoutCollector, logger::message, LOGGER::info);
      outputProcessor.start(process.getErrorStream(), stderrCollector, logger::error, LOGGER::info);
      var exitCode = process.waitFor();
      outputProcessor.shutdown();
      var duration = stopwatch.stop();
      logCompletion(exitCode, duration);
      return new BazelProcessResult(stdoutCollector, stderrCollector, exitCode);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  public <T> T processBinaryOutput(Function<InputStream, T> func) {
    try {
      var stopwatch = Stopwatch.start();
      var result = func.apply(process.getInputStream());
      var exitCode = process.waitFor();
      var duration = stopwatch.stop();
      logCompletion(exitCode, duration);
      return result;
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  private void logCompletion(int exitCode, Duration duration) {
    logger.message("Command completed in %s (exit code %d)", Format.duration(duration), exitCode);
  }
}
