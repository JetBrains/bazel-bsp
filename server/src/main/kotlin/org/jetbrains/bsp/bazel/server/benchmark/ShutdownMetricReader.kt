package org.jetbrains.bsp.bazel.server.benchmark

import io.opentelemetry.sdk.common.CompletableResultCode
import io.opentelemetry.sdk.metrics.InstrumentType
import io.opentelemetry.sdk.metrics.data.AggregationTemporality
import io.opentelemetry.sdk.metrics.export.CollectionRegistration
import io.opentelemetry.sdk.metrics.export.MetricExporter
import io.opentelemetry.sdk.metrics.export.MetricReader

class ShutdownMetricReader(private val exporter: MetricExporter) : MetricReader {
  private lateinit var registration: CollectionRegistration

  override fun getAggregationTemporality(instrumentType: InstrumentType): AggregationTemporality =
    exporter.getAggregationTemporality(instrumentType)

  override fun register(registration: CollectionRegistration) {
    this.registration = registration
  }

  override fun forceFlush(): CompletableResultCode =
    exporter.export(registration.collectAllMetrics())

  override fun shutdown(): CompletableResultCode {
    val result = forceFlush()
    if (!result.isSuccess) return result
    return exporter.shutdown()
  }
}
