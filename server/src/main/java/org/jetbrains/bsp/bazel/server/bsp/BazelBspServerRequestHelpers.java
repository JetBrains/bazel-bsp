package org.jetbrains.bsp.bazel.server.bsp;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.lsp4j.jsonrpc.ResponseErrorException;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseError;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseErrorCode;

public class BazelBspServerRequestHelpers {

  private static final Logger LOGGER = LogManager.getLogger(BazelBspServerRequestHelpers.class);

  private final BazelBspServerLifetime serverLifetime;

  public BazelBspServerRequestHelpers(BazelBspServerLifetime serverLifetime) {
    this.serverLifetime = serverLifetime;
  }

  public <T> CompletableFuture<T> executeCommand(
      String methodName, Supplier<Either<ResponseError, T>> request) {
    if (!serverLifetime.isInitialized()) {
      return completeExceptionally(
          methodName,
          new ResponseError(
              ResponseErrorCode.serverNotInitialized,
              "Server has not been initialized yet!",
              false));
    }
    if (serverLifetime.isFinished()) {
      return completeExceptionally(
          methodName,
          new ResponseError(
              ResponseErrorCode.serverErrorEnd, "Server has already shutdown!", false));
    }

    return getValue(methodName, request);
  }

  public <T> CompletableFuture<T> getValue(
      String methodName, Supplier<Either<ResponseError, T>> request) {
    return CompletableFuture.supplyAsync(request)
        .exceptionally( // TODO remove eithers in next PR
            exception -> {
              LOGGER.error(
                  "{} call finishing with exception: {}",
                  methodName,
                  exception.getCause().getStackTrace());

              return Either.forLeft(
                  new ResponseError(ResponseErrorCode.InternalError, exception.getMessage(), null));
            })
        .thenComposeAsync(
            either ->
                either.isLeft()
                    ? completeExceptionally(methodName, either.getLeft())
                    : completeWithSuccess(methodName, either.getRight()));
  }

  public <T> CompletableFuture<T> completeExceptionally(String methodName, ResponseError error) {
    LOGGER.error("{} call finishing with error: {}", methodName, error.getMessage());

    CompletableFuture<T> future = new CompletableFuture<>();
    future.completeExceptionally(new ResponseErrorException(error));
    return future;
  }

  private <T> CompletableFuture<T> completeWithSuccess(String methodName, T response) {
    LOGGER.info("{} call finishing with response: {}", methodName, response);

    return CompletableFuture.completedFuture(response);
  }
}
