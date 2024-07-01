package org.jetbrains.bsp.bazel.server.benchmark

import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.metrics.Meter
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator
import io.opentelemetry.context.propagation.ContextPropagators
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.OpenTelemetrySdkBuilder
import io.opentelemetry.sdk.metrics.SdkMeterProvider
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor
import org.jetbrains.bsp.bazel.commons.Constants
import org.jetbrains.bsp.bazel.logger.BspClientLogger
import java.nio.file.Path

private const val INSTRUMENTATION_SCOPE_NAME = "bazel-bsp"

lateinit var openTelemetry: OpenTelemetrySdk
  private set

val openTelemetryInitialized: Boolean
  get() = ::openTelemetry.isInitialized

val tracer: Tracer
  get() = openTelemetry.getTracer(INSTRUMENTATION_SCOPE_NAME)

val meter: Meter
  get() = openTelemetry.getMeter(INSTRUMENTATION_SCOPE_NAME)

private val shutdownRoutines = mutableListOf<() -> Unit>()

data class TelemetryConfig(
  val bspClientLogger: BspClientLogger? = null,
  val metricsFile: Path? = null,
  val openTelemetryEndpoint: String? = null,
)

fun setupTelemetry(config: TelemetryConfig) {
  val resource = Resource.create(
    Attributes.builder()
      .put(AttributeKey.stringKey("service.name"), "Bazel-BSP")
      .put(AttributeKey.stringKey("service.version"), Constants.VERSION)
      .build()
  )
  val sdkBuilder = OpenTelemetrySdk.builder()
  val fileExporter = config.metricsFile?.let { FileExporter(it) }
  setupTracing(sdkBuilder, resource, config, fileExporter)
  setupMetrics(sdkBuilder, resource, fileExporter)
  sdkBuilder.setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
  openTelemetry = sdkBuilder.buildAndRegisterGlobal()

  if (config.metricsFile != null) {
    MemoryProfiler.startRecording()
    shutdownRoutines.add { MemoryProfiler.stopRecording() }
  }
}

private fun setupTracing(
  sdkBuilder: OpenTelemetrySdkBuilder,
  resource: Resource,
  config: TelemetryConfig,
  fileExporter: FileExporter?,
) {
  val tracerProviderBuilder = SdkTracerProvider.builder()
  tracerProviderBuilder.setResource(resource)

  if (fileExporter != null) {
    tracerProviderBuilder.addSpanProcessor(BatchSpanProcessor.builder(fileExporter.spanExporter()).build())
  }

  if (config.openTelemetryEndpoint != null) {
    val httpSpanExporter = OtlpHttpSpanExporter.builder().setEndpoint(config.openTelemetryEndpoint).build()
    tracerProviderBuilder.addSpanProcessor(BatchSpanProcessor.builder(httpSpanExporter).build())
  }

  // Use SimpleSpanProcessor to log spans immediately
  if (config.bspClientLogger != null) {
    tracerProviderBuilder.addSpanProcessor(SimpleSpanProcessor.create(LoggingSpanExporter(config.bspClientLogger)))
  }

  val sdkTracerProvider = tracerProviderBuilder.build()
  shutdownRoutines.add { sdkTracerProvider.shutdown() }
  sdkBuilder.setTracerProvider(sdkTracerProvider)
}

private fun setupMetrics(
  sdkBuilder: OpenTelemetrySdkBuilder,
  resource: Resource,
  fileExporter: FileExporter?,
) {
  val meterProviderBuilder = SdkMeterProvider.builder().setResource(resource)

  if (fileExporter != null) {
    meterProviderBuilder.registerMetricReader(ShutdownMetricReader(fileExporter.metricExporter()))
  }

  val meterProvider = meterProviderBuilder.build()
  shutdownRoutines.add { meterProvider.shutdown() }
  sdkBuilder.setMeterProvider(meterProvider)
}

fun shutdownTelemetry() {
  shutdownRoutines.asReversed().forEach { it() }
}
