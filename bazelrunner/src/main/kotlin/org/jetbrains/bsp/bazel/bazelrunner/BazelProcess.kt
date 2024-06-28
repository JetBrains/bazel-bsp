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
import java.util.concurrent.CompletableFuture

class BazelProcess internal constructor(
    private val process: Process,
    private val logger: BspClientLogger? = null,
    private val serverPidFuture: CompletableFuture<Long>?
) {

    fun waitAndGetResult(
      cancelChecker: CancelChecker,
      ensureAllOutputRead: Boolean = false,
    ): BazelProcessResult {
        val stopwatch = Stopwatch.start()
        val outputProcessor: OutputProcessor = getOutputProcessor(ensureAllOutputRead)

        val exitCode = outputProcessor.waitForExit(cancelChecker, logger)
        val duration = stopwatch.stop()
        logCompletion(exitCode, duration)
        return BazelProcessResult(outputProcessor.stdoutCollector, outputProcessor.stderrCollector, exitCode)
    }


    // Returns a Future that completes with a result of the process.
    // When the execution is cancelled, the returned Future also interrupts the task on the Bazel server. In that case
    // the Future completes after BepReader's `serverPidFuture`.
    fun waitAndGetResultAsync(
        cancelChecker: CancelChecker,
        ensureAllOutputRead: Boolean = false,
    ): CompletableFuture<BazelProcessResult> {
        val stopwatch = Stopwatch.start()
        val outputProcessor: OutputProcessor = getOutputProcessor(ensureAllOutputRead)

        val exitCode = outputProcessor.waitForExitAsync(cancelChecker, serverPidFuture, logger)
        return exitCode.thenApply {
            val duration = stopwatch.stop()
            logCompletion(it, duration)
            BazelProcessResult(outputProcessor.stdoutCollector, outputProcessor.stderrCollector, it)
        }
    }

    private fun getOutputProcessor(ensureAllOutputRead: Boolean): OutputProcessor {
        return if (logger != null) {
            if (ensureAllOutputRead) SyncOutputProcessor(process, logger::message, LOGGER::info)
            else AsyncOutputProcessor(process, logger::message, LOGGER::info)
        } else {
            if (ensureAllOutputRead) SyncOutputProcessor(process, LOGGER::info)
            else AsyncOutputProcessor(process, LOGGER::info)
        }
    }

    private fun logCompletion(exitCode: Int, duration: Duration) {
        logger?.message("Command completed in %s (exit code %d)", Format.duration(duration), exitCode)
    }

    companion object {
        private val LOGGER = LogManager.getLogger(BazelProcess::class.java)
    }
}
