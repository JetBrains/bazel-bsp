package org.jetbrains.bsp.bazel.server.bsp;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import org.eclipse.lsp4j.jsonrpc.ResponseErrorException;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseError;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseErrorCode;

public class BazelBspServerRequestHelpers {

  private final BazelBspServerLifetime serverLifetime;

  public BazelBspServerRequestHelpers(BazelBspServerLifetime serverLifetime) {
    this.serverLifetime = serverLifetime;
  }

  public <T> CompletableFuture<T> executeCommand(Supplier<Either<ResponseError, T>> request) {
    if (!serverLifetime.isInitialized()) {
      return completeExceptionally(
          new ResponseError(
              ResponseErrorCode.serverNotInitialized,
              "Server has not been initialized yet!",
              false));
    }
    if (serverLifetime.isFinished()) {
      return completeExceptionally(
          new ResponseError(
              ResponseErrorCode.serverErrorEnd, "Server has already shutdown!", false));
    }

    return getValue(request);
  }

  public <T> CompletableFuture<T> completeExceptionally(ResponseError error) {
    CompletableFuture<T> future = new CompletableFuture<>();
    future.completeExceptionally(new ResponseErrorException(error));
    return future;
  }

  public <T> CompletableFuture<T> getValue(Supplier<Either<ResponseError, T>> request) {
    return CompletableFuture.supplyAsync(request)
        .exceptionally( // TODO remove eithers in next PR
            exception -> {
              exception.printStackTrace(); // TODO better logging
              return Either.forLeft(
                  new ResponseError(ResponseErrorCode.InternalError, exception.getMessage(), null));
            })
        .thenComposeAsync(
            either ->
                either.isLeft()
                    ? completeExceptionally(either.getLeft())
                    : CompletableFuture.completedFuture(either.getRight()));
  }
}
