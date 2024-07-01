package org.jetbrains.bsp.bazel.server.bsp

import io.opentelemetry.api.incubator.propagation.ExtendedContextPropagators
import org.eclipse.lsp4j.jsonrpc.Launcher
import org.eclipse.lsp4j.jsonrpc.MessageIssueHandler
import org.eclipse.lsp4j.jsonrpc.json.MessageJsonHandler
import org.eclipse.lsp4j.jsonrpc.json.StreamMessageProducer
import org.jetbrains.bsp.bazel.server.benchmark.openTelemetry
import org.jetbrains.bsp.bazel.server.benchmark.openTelemetryInitialized
import java.io.InputStream
import java.util.concurrent.Executors

class TelemetryContextPropagatingLauncherBuilder<T> : Launcher.Builder<T>() {
  override fun create(): Launcher<T> {
    val jsonHandler = createJsonHandler()
    val remoteEndpoint = createRemoteEndpoint(jsonHandler)
    val remoteProxy = createProxy(remoteEndpoint)
    val reader = TelemetryContextPropagatingStreamMessageProducer(input, jsonHandler, remoteEndpoint)
    val messageConsumer = wrapMessageConsumer(remoteEndpoint)
    val msgProcessor = createMessageProcessor(reader, messageConsumer, remoteProxy)
    val execService = if (executorService != null) executorService else Executors.newCachedThreadPool()
    return createLauncher(execService, remoteProxy, remoteEndpoint, msgProcessor)
  }
}

private class TelemetryContextPropagatingStreamMessageProducer(
  input: InputStream,
  jsonHandler: MessageJsonHandler,
  issueHandler: MessageIssueHandler,
) : StreamMessageProducer(input, jsonHandler, issueHandler) {

  private val currentHeaders = mutableMapOf<String, String>()

  override fun parseHeader(line: String, headers: Headers) {
    val (key, value) = line.split(":", limit = 2).map { it.trim() }
    currentHeaders[key] = value
    super.parseHeader(line, headers)
  }

  override fun handleMessage(input: InputStream?, headers: Headers?): Boolean {
    if (!openTelemetryInitialized) {
      // The first call via BSP is build/initialize, at which point the telemetry isn't initialized yet.
      return super.handleMessage(input, headers)
    }
    val context = ExtendedContextPropagators
      .extractTextMapPropagationContext(currentHeaders, openTelemetry.propagators)
      .also { currentHeaders.clear() }
    context.makeCurrent().use {
      return super.handleMessage(input, headers)
    }
  }
}
