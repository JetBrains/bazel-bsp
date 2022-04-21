package org.jetbrains.bsp.bazel.bazelrunner.outputs

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.Future

class AsyncOutputProcessor {
  private val executorService = Executors.newCachedThreadPool()
  private val runningProcessors = mutableListOf<Future<*>>()

  fun start(inputStream: InputStream, vararg handlers: OutputHandler) {
    val runnable = Runnable {
      try {
        BufferedReader(InputStreamReader(inputStream)).use { reader ->
          var prevLine: String? = null
          while (!Thread.currentThread().isInterrupted) {
            val line = reader.readLine() ?: return@Runnable
            if (line == prevLine) continue
            prevLine = line
            handlers.forEach { it.onNextLine(line) }
          }
        }
      } catch (e: IOException) {
        if (Thread.currentThread().isInterrupted) return@Runnable
        throw RuntimeException(e)
      }
    }
    executorService.submit(runnable).also { runningProcessors.add(it) }
  }

  fun shutdown() {
    runningProcessors.forEach { it.get() }
    executorService.shutdown()
  }
}
