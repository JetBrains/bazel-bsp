package org.jetbrains.bsp.bazel.server.sync

import org.jetbrains.bsp.bazel.commons.Stopwatch
import org.jetbrains.bsp.bazel.logger.BspClientLogger
import java.util.Locale

object Measurements {
    fun <T> measure(f: () -> T, description: String, metricsLogger: MetricsLogger?, bspClientLogger: BspClientLogger): T {
        val sw = Stopwatch.start()
        val result = f()
        val duration = sw.stop()
        val taskKey = description.lowercase(Locale.getDefault()).replace("\\s+".toRegex(), ".")
        metricsLogger?.logMemory("$taskKey.memory.mb")
        metricsLogger?.log("$taskKey.time.ms", duration.toMillis())
        bspClientLogger.logDuration(description, duration)
        return result
    }
}
