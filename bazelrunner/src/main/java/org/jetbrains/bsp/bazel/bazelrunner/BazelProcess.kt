package org.jetbrains.bsp.bazel.bazelrunner

import org.apache.logging.log4j.LogManager
import org.jetbrains.bsp.bazel.bazelrunner.outputs.AsyncOutputProcessor
import org.jetbrains.bsp.bazel.bazelrunner.outputs.OutputCollector
import org.jetbrains.bsp.bazel.commons.Format
import org.jetbrains.bsp.bazel.commons.Stopwatch
import org.jetbrains.bsp.bazel.logger.BspClientLogger
import java.time.Duration

class BazelProcess internal constructor(
    private val process: Process,
    private val logger: BspClientLogger
) {

  fun waitAndGetResult(): BazelProcessResult {
    val stdoutCollector = OutputCollector()
    val stderrCollector = OutputCollector()
    val outputProcessor = AsyncOutputProcessor()
    val stopwatch = Stopwatch.start()
    outputProcessor.start(process.inputStream, stdoutCollector, logger::message, LOGGER::info)
    outputProcessor.start(process.errorStream, stderrCollector, logger::error, LOGGER::info)
    val exitCode = process.waitFor()
    outputProcessor.shutdown()
    val duration = stopwatch.stop()
    logCompletion(exitCode, duration)
    return BazelProcessResult(stdoutCollector, stderrCollector, exitCode)
  }

  private fun logCompletion(exitCode: Int, duration: Duration) {
    logger.message("Command completed in %s (exit code %d)", Format.duration(duration), exitCode)
  }

  companion object {
    private val LOGGER = LogManager.getLogger(BazelProcess::class.java)
  }
}
