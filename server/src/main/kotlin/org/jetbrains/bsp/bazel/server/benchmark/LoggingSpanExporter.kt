package org.jetbrains.bsp.bazel.server.benchmark

import io.opentelemetry.sdk.common.CompletableResultCode
import io.opentelemetry.sdk.trace.data.SpanData
import io.opentelemetry.sdk.trace.export.SpanExporter
import org.jetbrains.bsp.bazel.logger.BspClientLogger
import java.time.Duration

class LoggingSpanExporter(private val bspClientLogger: BspClientLogger) : SpanExporter {
  override fun export(spans: Collection<SpanData>): CompletableResultCode {
    for (span in spans) {
      bspClientLogger.logDuration(span.name, Duration.ofMillis(span.durationMs))
    }
    return CompletableResultCode.ofSuccess()
  }

  override fun flush(): CompletableResultCode = CompletableResultCode.ofSuccess()

  override fun shutdown(): CompletableResultCode = CompletableResultCode.ofSuccess()
}
