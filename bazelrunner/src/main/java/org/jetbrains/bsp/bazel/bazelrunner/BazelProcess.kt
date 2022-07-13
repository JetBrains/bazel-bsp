package org.jetbrains.bsp.bazel.bazelrunner

import org.apache.logging.log4j.LogManager
import org.jetbrains.bsp.bazel.bazelrunner.outputs.AsyncOutputProcessor
import org.jetbrains.bsp.bazel.commons.Format
import org.jetbrains.bsp.bazel.commons.Stopwatch
import org.jetbrains.bsp.bazel.logger.BspClientLogger
import java.time.Duration

class BazelProcess internal constructor(
    private val process: Process,
) {

  fun waitAndGetResult(): BazelProcessResult {
    val outputProcessor = AsyncOutputProcessor(process, BspClientLogger::message, LOGGER::info)
    val stopwatch = Stopwatch.start()

    val exitCode = outputProcessor.waitForExit()
    val duration = stopwatch.stop()
    logCompletion(exitCode, duration)
    return BazelProcessResult(outputProcessor.stdoutCollector, outputProcessor.stderrCollector, exitCode)
  }

  // TODO, can we use .timed somehow?
  private fun logCompletion(exitCode: Int, duration: Duration) {
    BspClientLogger.message("Command completed in ${Format.duration(duration)} (exit code $exitCode)")
  }

  companion object {
    private val LOGGER = LogManager.getLogger(BazelProcess::class.java)
  }
}
