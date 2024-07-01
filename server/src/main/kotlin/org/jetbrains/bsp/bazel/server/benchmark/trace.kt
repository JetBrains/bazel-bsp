package org.jetbrains.bsp.bazel.server.benchmark

import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.SpanBuilder
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.context.Context
import io.opentelemetry.extension.kotlin.asContextElement
import io.opentelemetry.sdk.trace.data.SpanData
import io.opentelemetry.semconv.ExceptionAttributes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import java.util.concurrent.CancellationException
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

// Utility functions taken from IDEA

/**
 * Starts a new span and adds it to the current scope for the [operation].
 * That way the spans created inside the [operation] will be nested to the created span.
 *
 * See [span concept](https://opentelemetry.io/docs/concepts/signals/traces/#spans) for more details on span nesting.
 */
inline fun <T> SpanBuilder.use(operation: (Span) -> T): T {
  return startSpan().useWithoutActiveScope { span ->
    span.makeCurrent().use {
      operation(span)
    }
  }
}

/**
 * Does not activate the span scope, so **new spans created inside will not be linked to [this] span**.
 * Consider using [use] to also activate the scope.
 */
inline fun <T> Span.useWithoutActiveScope(operation: (Span) -> T): T {
  try {
    return operation(this)
  } catch (e: CancellationException) {
    recordException(e, Attributes.of(ExceptionAttributes.EXCEPTION_ESCAPED, true))
    throw e
  } catch (e: Throwable) {
    recordException(e, Attributes.of(ExceptionAttributes.EXCEPTION_ESCAPED, true))
    setStatus(StatusCode.ERROR)
    throw e
  } finally {
    end()
  }
}

@Suppress("unused")
suspend inline fun <T> SpanBuilder.useWithScope(
  context: CoroutineContext = EmptyCoroutineContext,
  crossinline operation: suspend CoroutineScope.(Span) -> T
): T {
  val span = startSpan()
  return withContext(Context.current().with(span).asContextElement() + context) {
    try {
      operation(span)
    }
    catch (e: CancellationException) {
      span.recordException(e, Attributes.of(ExceptionAttributes.EXCEPTION_ESCAPED, true))
      throw e
    }
    catch (e: Throwable) {
      span.recordException(e, Attributes.of(ExceptionAttributes.EXCEPTION_ESCAPED, true))
      span.setStatus(StatusCode.ERROR)
      throw e
    }
    finally {
      span.end()
    }
  }
}

val SpanData.durationMs: Long
  get() = (this.endEpochNanos - this.startEpochNanos) / 1_000_000
