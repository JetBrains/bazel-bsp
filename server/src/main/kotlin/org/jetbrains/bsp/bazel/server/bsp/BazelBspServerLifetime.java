package org.jetbrains.bsp.bazel.server.bsp;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class BazelBspServerLifetime {

  private final CompletableFuture<Void> initializedStatus = new CompletableFuture<>();
  private final CompletableFuture<Void> finishedStatus = new CompletableFuture<>();

  public boolean isInitialized() {
    try {
      initializedStatus.get(1, TimeUnit.SECONDS);
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      return false;
    }
    return true;
  }

  public boolean isFinished() {
    return finishedStatus.isDone();
  }

  public void setInitializedComplete() {
    initializedStatus.complete(null);
  }

  public void setFinishedComplete() {
    finishedStatus.complete(null);
  }

  public void forceFinish() {
    try {
      finishedStatus.get(1, TimeUnit.SECONDS);
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      System.exit(1);
    }

    System.exit(0);
  }
}
