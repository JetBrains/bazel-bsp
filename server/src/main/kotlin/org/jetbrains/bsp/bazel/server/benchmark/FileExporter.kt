package org.jetbrains.bsp.bazel.server.benchmark

import io.opentelemetry.sdk.common.CompletableResultCode
import io.opentelemetry.sdk.metrics.InstrumentType
import io.opentelemetry.sdk.metrics.data.AggregationTemporality
import io.opentelemetry.sdk.metrics.data.MetricData
import io.opentelemetry.sdk.metrics.data.MetricDataType
import io.opentelemetry.sdk.metrics.export.MetricExporter
import io.opentelemetry.sdk.trace.data.SpanData
import io.opentelemetry.sdk.trace.export.SpanExporter
import org.apache.logging.log4j.LogManager
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.io.path.writer

private val LOGGER = LogManager.getLogger(FileExporter::class.java)

class FileExporter(private val logFile: Path) {
  private val logs: ConcurrentHashMap<String, Long> = ConcurrentHashMap()
  private val shutdownRequests = AtomicInteger(0)

  fun spanExporter(): SpanExporter = object : SpanExporter {
    override fun export(spans: MutableCollection<SpanData>): CompletableResultCode {
      for (span in spans) {
        val taskKey = span.name.lowercase().replace("\\s+".toRegex(), ".")
        logs["$taskKey.time.ms"] = span.durationMs
      }
      return CompletableResultCode.ofSuccess()
    }

    override fun flush(): CompletableResultCode = CompletableResultCode.ofSuccess()

    override fun shutdown(): CompletableResultCode {
      shutdownRequest()
      return CompletableResultCode.ofSuccess()
    }
  }

  fun metricExporter(): MetricExporter = object : MetricExporter {
    override fun getAggregationTemporality(instrumentType: InstrumentType): AggregationTemporality =
      AggregationTemporality.CUMULATIVE

    override fun export(metrics: MutableCollection<MetricData>): CompletableResultCode {
      for (metric in metrics) {
        if (metric.type == MetricDataType.LONG_GAUGE) {
          logs[metric.name] = metric.longGaugeData.points.last().value
        } else {
          LOGGER.warn("Unsupported metric type ${metric.type}")
        }
      }
      return CompletableResultCode.ofSuccess()
    }

    override fun flush(): CompletableResultCode = CompletableResultCode.ofSuccess()

    override fun shutdown(): CompletableResultCode {
      shutdownRequest()
      return CompletableResultCode.ofSuccess()
    }
  }

  private fun shutdownRequest() {
    if (shutdownRequests.incrementAndGet() != 2) return
    doShutdown()
  }

  private fun doShutdown(): CompletableResultCode? {
    logFile.writer().use { writer ->
      for ((k, v) in logs) {
        writer.write("$k $v\n")
      }
    }
    return CompletableResultCode.ofSuccess()
  }
}
