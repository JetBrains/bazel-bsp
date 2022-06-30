package org.jetbrains.bsp.bazel.bazelrunner.outputs

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicBoolean

class AsyncOutputProcessor(
  private val process: Process,
  vararg loggers: OutputHandler
) {
  private val executorService = Executors.newCachedThreadPool()
  private val runningProcessors = mutableListOf<Future<*>>()
  private val isRunning = AtomicBoolean(true)

  val stdoutCollector = OutputCollector()
  val stderrCollector = OutputCollector()

  init {
    start(process.inputStream, stdoutCollector, *loggers)
    start(process.errorStream, stderrCollector, *loggers)
  }

  private fun start(inputStream: InputStream, vararg handlers: OutputHandler) {
    val runnable = Runnable {
      try {
        BufferedReader(InputStreamReader(inputStream)).use { reader ->
          var prevLine: String? = null

          while (!Thread.currentThread().isInterrupted) {
            val line = reader.readLine() ?: return@Runnable
            if (line == prevLine) continue
            prevLine = line
            if (isRunning.get()) {
              handlers.forEach { it.onNextLine(line) }
            } else {
              break
            }
          }
        }
      } catch (e: IOException) {
        if (Thread.currentThread().isInterrupted) return@Runnable
        throw RuntimeException(e)
      }
    }

    executorService.submit(runnable).also { runningProcessors.add(it) }
  }

  fun waitForExit(): Int {
    val exitCode = process.waitFor()
    shutdown()
    return exitCode
  }

  private fun shutdown() {
    isRunning.set(false)
    runningProcessors.forEach {
      try {
        it.get(500, TimeUnit.MILLISECONDS)
      } catch (_: TimeoutException) {
        // it's cool
      }
    }
    executorService.shutdown()
  }
}
