package org.jetbrains.bsp.bazel.bazelrunner

import org.apache.logging.log4j.LogManager
import org.jetbrains.bsp.bazel.bazelrunner.outputs.AsyncOutputProcessor
import org.jetbrains.bsp.bazel.commons.Format
import org.jetbrains.bsp.bazel.commons.Stopwatch
import org.jetbrains.bsp.bazel.logger.BspClientLogger
import java.time.Duration

class BazelProcess internal constructor(
    private val process: Process,
    private val logger: BspClientLogger
) {

  fun waitAndGetResult(): BazelProcessResult {
    val outputProcessor = AsyncOutputProcessor(process, logger::message, LOGGER::info)
    val stopwatch = Stopwatch.start()

    val exitCode = outputProcessor.waitForExit()
    val duration = stopwatch.stop()
    logCompletion(exitCode, duration)
    return BazelProcessResult(outputProcessor.stdoutCollector, outputProcessor.stderrCollector, exitCode)
  }

  private fun logCompletion(exitCode: Int, duration: Duration) {
    logger.message("Command completed in %s (exit code %d)", Format.duration(duration), exitCode)
  }

  companion object {
    private val LOGGER = LogManager.getLogger(BazelProcess::class.java)
  }
}
