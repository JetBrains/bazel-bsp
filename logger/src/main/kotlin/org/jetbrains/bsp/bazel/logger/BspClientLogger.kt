package org.jetbrains.bsp.bazel.logger

import ch.epfl.scala.bsp4j.BuildClient
import ch.epfl.scala.bsp4j.LogMessageParams
import ch.epfl.scala.bsp4j.MessageType
import org.jetbrains.bsp.bazel.commons.Format
import org.jetbrains.bsp.bazel.commons.Stopwatch
import java.time.Duration

object BspClientLogger {

    private val LOG_OPERATION_THRESHOLD = Duration.ofMillis(100)

    private var bspClient: BuildClient? = null

    fun initialize(buildClient: BuildClient) {
        bspClient = buildClient
    }

    fun <T> timed(description: String, action: () -> T): T {
        val stopwatch = Stopwatch.start()
        val result = action()
        val duration = stopwatch.stop()
        
        if (duration >= LOG_OPERATION_THRESHOLD) {
            val formattedDuration = Format.duration(duration)
            message("$description completed in $formattedDuration")
        }
        
        return result
    }
    
    fun error(errorMessage: String) =
        log(MessageType.ERROR, errorMessage)

    fun message(message: String) =
        log(MessageType.LOG, message)

    private fun log(messageType: MessageType, message: String) {
        if (message.isNotBlank()) {
            val params = LogMessageParams(messageType, message)
            bspClient?.onBuildLogMessage(params)
        }
    }
}
