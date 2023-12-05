package org.jetbrains.bsp.bazel.server.bsp

import org.apache.logging.log4j.LogManager
import org.eclipse.lsp4j.jsonrpc.CancelChecker
import org.eclipse.lsp4j.jsonrpc.CompletableFutures
import org.eclipse.lsp4j.jsonrpc.ResponseErrorException
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.jsonrpc.messages.ResponseError
import org.eclipse.lsp4j.jsonrpc.messages.ResponseErrorCode
import java.io.PrintWriter
import java.io.StringWriter
import java.util.concurrent.CompletableFuture
import java.util.function.BiFunction
import java.util.function.Function

class BspRequestsRunner(private val serverLifetime: BazelBspServerLifetime) {
    fun <T, R> handleRequest(
        methodName: String, function: BiFunction<CancelChecker?, T, R>, arg: T,
    ): CompletableFuture<R> {
        LOGGER.info("{} call with param: {}", methodName, arg)
        return serverIsRunning<R>(methodName)
            ?: runAsync(
                methodName,
                Function { cancelChecker: CancelChecker? -> function.apply(cancelChecker, arg) })
    }


    fun <R> handleRequest(
        methodName: String, supplier: Function<CancelChecker, R>,
    ): CompletableFuture<R> {
        LOGGER.info("{} call", methodName)
        return serverIsRunning(methodName) ?: runAsync(
            methodName,
            supplier
        )
    }

    fun handleNotification(methodName: String, runnable: Runnable) {
        LOGGER.info("{} call", methodName)
        runnable.run()
    }

    fun <R> handleRequest(
        methodName: String,
        supplier: Function<CancelChecker, R>,
        precondition: Function<String?, CompletableFuture<R>?>,
    ): CompletableFuture<R> {
        LOGGER.info("{} call", methodName)
        return precondition.apply(methodName) ?: runAsync(methodName, supplier)
    }

    fun <T> serverIsRunning(methodName: String): CompletableFuture<T>? =
        serverIsInitialized(methodName) ?: serverIsNotFinished(methodName)

    fun <T> serverIsInitialized(methodName: String): CompletableFuture<T>? =
        if (!serverLifetime.isInitialized) {
            failure(
                methodName,
                ResponseError(
                    ResponseErrorCode.ServerNotInitialized,
                    "Server has not been initialized yet!",
                    false
                )
            )
        } else {
            null
        }

    fun <T> serverIsNotFinished(methodName: String): CompletableFuture<T>? =
        if (serverLifetime.isFinished) {
            failure(
                methodName,
                ResponseError(
                    ResponseErrorCode.ServerNotInitialized, "Server has already shutdown!", false
                )
            )
        } else {
            null
        }

    private fun <T> runAsync(methodName: String, request: Function<CancelChecker, T>): CompletableFuture<T> =
        CancellableFuture.from(CompletableFutures.computeAsync(request))
            .thenApply<Either<Throwable, T>> { right: T -> Either.forRight(right) }
            .exceptionally { left: Throwable? -> Either.forLeft(left) }
            .thenCompose { either: Either<Throwable, T> ->
                if (either.isLeft) failure(
                    methodName,
                    either.left
                ) else success<T>(methodName, either.right)
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
                ResponseError(ResponseErrorCode.InternalError, message, null)
            )
        )
    }

    companion object {
        private val LOGGER = LogManager.getLogger(
            BspRequestsRunner::class.java
        )
    }
}
