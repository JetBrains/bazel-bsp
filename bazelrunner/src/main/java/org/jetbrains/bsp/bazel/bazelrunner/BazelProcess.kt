package org.jetbrains.bsp.bazel.bazelrunner

import java.time.Duration
import org.apache.logging.log4j.LogManager
import org.eclipse.lsp4j.jsonrpc.CancelChecker
import org.jetbrains.bsp.bazel.bazelrunner.outputs.AsyncOutputProcessor
import org.jetbrains.bsp.bazel.bazelrunner.outputs.OutputProcessor
import org.jetbrains.bsp.bazel.bazelrunner.outputs.SyncOutputProcessor
import org.jetbrains.bsp.bazel.commons.Format
import org.jetbrains.bsp.bazel.commons.Stopwatch
import org.jetbrains.bsp.bazel.logger.BspClientLogger

class BazelProcess internal constructor(
    private val process: Process,
    private val logger: BspClientLogger,
    private val originId: String?
) {

    fun waitAndGetResult(cancelChecker: CancelChecker, ensureAllOutputRead: Boolean = false): BazelProcessResult {
        val stopwatch = Stopwatch.start()
        val outputProcessor: OutputProcessor =
          if (ensureAllOutputRead) SyncOutputProcessor(process, logger::message, LOGGER::info)
          else AsyncOutputProcessor(process, logger::message, LOGGER::info)

        val exitCode = outputProcessor.waitForExit(cancelChecker)
        val duration = stopwatch.stop()
        logCompletion(exitCode, duration)
        return BazelProcessResult(outputProcessor.stdoutCollector, outputProcessor.stderrCollector, exitCode)
    }

    private fun logCompletion(exitCode: Int, duration: Duration) {
        logger.withOriginId(originId)
            .message("Command completed in %s (exit code %d)", Format.duration(duration), exitCode)
    }

    companion object {
        private val LOGGER = LogManager.getLogger(BazelProcess::class.java)
    }
}
