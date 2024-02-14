package org.jetbrains.bsp.bazel.logger

import ch.epfl.scala.bsp4j.BuildClient
import ch.epfl.scala.bsp4j.LogMessageParams
import ch.epfl.scala.bsp4j.MessageType
import org.jetbrains.bsp.bazel.commons.Format
import org.jetbrains.bsp.bazel.commons.Stopwatch
import java.time.Duration
import java.util.function.Supplier

private val LOG_OPERATION_THRESHOLD: Duration = Duration.ofMillis(100)

data class BspClientLogger(private val bspClient: BuildClient, private val originId: String? = null) {

  fun error(errorMessage: String) {
    log(MessageType.ERROR, errorMessage)
  }

  fun message(format: String, vararg args: Any?) {
    message(String.format(format, *args))
  }

  fun message(message: String) {
    log(MessageType.LOG, message)
  }

  fun warn(format: String, vararg args: Any?) {
    warn(String.format(format, *args))
  }

  fun warn(message: String) {
    log(MessageType.WARNING, message)
  }

  private fun log(messageType: MessageType, message: String) {
    if (message.trim { it <= ' ' }.isNotEmpty()) {
      val params = LogMessageParams(messageType, message)
      params.originId = originId
      bspClient.onBuildLogMessage(params)
    }
  }

  fun <T> timed(description: String?, supplier: Supplier<T>): T {
    val sw = Stopwatch.start()
    val result = supplier.get()
    val duration = sw.stop()
    logDuration(description, duration)
    return result
  }

  fun logDuration(description: String?, duration: Duration) {
    if (duration >= LOG_OPERATION_THRESHOLD) {
      message("Task '%s' completed in %s", description, Format.duration(duration))
    }
  }

  fun timed(description: String?, runnable: Runnable) {
    timed<Any?>(
      description
    ) {
      runnable.run()
      null
    }
  }
}
