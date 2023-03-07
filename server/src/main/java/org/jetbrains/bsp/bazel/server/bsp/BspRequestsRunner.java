package org.jetbrains.bsp.bazel.server.bsp;

import io.vavr.control.Option;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.CompletableFutures;
import org.eclipse.lsp4j.jsonrpc.ResponseErrorException;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseError;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseErrorCode;

public class BspRequestsRunner {

  private static final Logger LOGGER = LogManager.getLogger(BspRequestsRunner.class);

  private final BazelBspServerLifetime serverLifetime;

  public BspRequestsRunner(BazelBspServerLifetime serverLifetime) {
    this.serverLifetime = serverLifetime;
  }

  public <T, R> CompletableFuture<R> handleRequest(
          String methodName, BiFunction<CancelChecker, T, R> function, T arg) {
    LOGGER.info("{} call with param: {}", methodName, arg);
    return this.<R>serverIsRunning(methodName)
        .getOrElse(() -> runAsync(methodName, cancelChecker -> function.apply(cancelChecker, arg)));
  }

  public <R> CompletableFuture<R> handleRequest(String methodName, Function<CancelChecker, R> supplier) {
    LOGGER.info("{} call", methodName);
    return this.<R>serverIsRunning(methodName).getOrElse(() -> runAsync(methodName, supplier));
  }

  public void handleNotification(String methodName, Runnable runnable) {
    LOGGER.info("{} call", methodName);
    runnable.run();
  }

  public <R> CompletableFuture<R> handleRequest(
      String methodName,
      Function<CancelChecker, R> supplier,
      Function<String, Option<CompletableFuture<R>>> precondition) {
    LOGGER.info("{} call", methodName);
    return precondition.apply(methodName).getOrElse(() -> runAsync(methodName, supplier));
  }

  public <T> Option<CompletableFuture<T>> serverIsRunning(String methodName) {
    return this.<T>serverIsInitialized(methodName).orElse(() -> serverIsNotFinished(methodName));
  }

  public <T> Option<CompletableFuture<T>> serverIsInitialized(String methodName) {
    if (!serverLifetime.isInitialized()) {
      return Option.some(
          failure(
              methodName,
              new ResponseError(
                  ResponseErrorCode.serverNotInitialized,
                  "Server has not been initialized yet!",
                  false)));
    } else {
      return Option.none();
    }
  }

  public <T> Option<CompletableFuture<T>> serverIsNotFinished(String methodName) {
    if (serverLifetime.isFinished()) {
      return Option.some(
          failure(
              methodName,
              new ResponseError(
                  ResponseErrorCode.serverNotInitialized, "Server has already shutdown!", false)));
    } else {
      return Option.none();
    }
  }

  private <T> CompletableFuture<T> runAsync(String methodName, Function<CancelChecker,T> request) {
    return CancellableFuture.from(CompletableFutures.computeAsync(request))
        .thenApply(Either::<Throwable, T>forRight)
        .exceptionally(Either::forLeft)
        .thenCompose(
            either ->
                either.isLeft()
                    ? failure(methodName, either.getLeft())
                    : success(methodName, either.getRight()));
  }

  private <T> CompletableFuture<T> success(String methodName, T response) {
    LOGGER.info("{} call finishing successfully", methodName);
    return CompletableFuture.completedFuture(response);
  }

  private <T> CompletableFuture<T> failure(String methodName, ResponseError error) {
    LOGGER.error("{} call finishing with error: {}", methodName, error.getMessage());
    return CompletableFuture.failedFuture(new ResponseErrorException(error));
  }

  private <T> CompletableFuture<T> failure(String methodName, Throwable throwable) {
    LOGGER.error("{} call finishing with error: {}", methodName, throwable.getMessage());
    if (throwable instanceof ResponseErrorException) {
      return CompletableFuture.failedFuture(throwable);
    }
    var sw = new StringWriter();
    var pw = new PrintWriter(sw);
    throwable.printStackTrace(pw);

    var message = throwable.getMessage() + "\n" + sw.toString();
    return CompletableFuture.failedFuture(
        new ResponseErrorException(
            new ResponseError(ResponseErrorCode.InternalError, message, null)));
  }
}
