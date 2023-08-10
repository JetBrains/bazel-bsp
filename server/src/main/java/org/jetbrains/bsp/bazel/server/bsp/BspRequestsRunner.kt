package org.jetbrains.bsp.bazel.server.bsp

import com.jetbrains.jsonrpc4kt.CancelChecker
import com.jetbrains.jsonrpc4kt.CompletableFutures.computeAsync
import com.jetbrains.jsonrpc4kt.ResponseErrorException
import com.jetbrains.jsonrpc4kt.messages.ResponseError
import com.jetbrains.jsonrpc4kt.messages.ResponseErrorCode
import org.apache.logging.log4j.LogManager
import java.io.PrintWriter
import java.io.StringWriter
import java.util.concurrent.CompletableFuture

class BspRequestsRunner(private val serverLifetime: BazelBspServerLifetime) {
  fun <T, R> handleRequest(
    methodName: String, function: (CancelChecker, T) -> R, arg: T,
  ): CompletableFuture<R> {
    LOGGER.info("{} call with param: {}", methodName, arg)
    return serverIsRunning(methodName)
      ?: runAsync(
        methodName
      ) { cancelChecker -> function(cancelChecker, arg) }
  }

  fun <R> handleRequest(
    methodName: String, supplier: (CancelChecker) -> R,
  ): CompletableFuture<R> {
    LOGGER.info("{} call", methodName)
    return serverIsRunning(methodName) ?: runAsync(methodName, supplier)
  }

  fun handleNotification(methodName: String?, runnable: Runnable) {
    LOGGER.info("{} call", methodName)
    runnable.run()
  }

  fun <R> handleRequest(
    methodName: String,
    supplier: (CancelChecker) -> R,
    precondition: (String) -> CompletableFuture<R>?,
  ): CompletableFuture<R> {
    LOGGER.info("{} call", methodName)
    return precondition(methodName) ?: runAsync(methodName, supplier)
  }

  fun <T> serverIsRunning(methodName: String): CompletableFuture<T>? {
    return serverIsInitialized(methodName) ?: serverIsNotFinished(methodName)
  }

  fun <T> serverIsInitialized(methodName: String): CompletableFuture<T>? {
    return if (!serverLifetime.isInitialized()) {
      failure(
        methodName,
        ResponseError(
          ResponseErrorCode.ServerNotInitialized.code,
          "Server has not been initialized yet!", null
        )
      )
    } else {
      null
    }
  }

  fun <T> serverIsNotFinished(methodName: String): CompletableFuture<T>? {
    return if (serverLifetime.isFinished) {
      failure(
        methodName,
        ResponseError(
          ResponseErrorCode.ServerNotInitialized.code,
          "Server has already shutdown!",
          null
        )
      )
    } else {
      null
    }
  }

  private fun <T> runAsync(methodName: String, request: (CancelChecker) -> T): CompletableFuture<T> {
    return CancellableFuture.from(computeAsync<T>(request))
      .exceptionallyCompose { failure(methodName, it) }
      .thenCompose { success(methodName, it) }
  }

  private fun <T> success(methodName: String, response: T): CompletableFuture<T> {
    LOGGER.info("{} call finishing successfully", methodName)
    return CompletableFuture.completedFuture(response)
  }

  private fun <T> failure(methodName: String, error: ResponseError): CompletableFuture<T> {
    LOGGER.error("{} call finishing with error: {}", methodName, error.message)
    return CompletableFuture.failedFuture(ResponseErrorException(error))
  }

  private fun <T> failure(methodName: String, throwable: Throwable): CompletableFuture<T> {
    LOGGER.error("{} call finishing with error: {}", methodName, throwable.message)
    if (throwable is ResponseErrorException) {
      return CompletableFuture.failedFuture(throwable)
    }
    val sw = StringWriter()
    val pw = PrintWriter(sw)
    throwable.printStackTrace(pw)
    val message = """
        ${throwable.message}
        $sw
        """.trimIndent()
    return CompletableFuture.failedFuture(
      ResponseErrorException(
        ResponseError(ResponseErrorCode.InternalError.code, message, null)
      )
    )
  }

  companion object {
    private val LOGGER = LogManager.getLogger(
      BspRequestsRunner::class.java
    )
  }
}
