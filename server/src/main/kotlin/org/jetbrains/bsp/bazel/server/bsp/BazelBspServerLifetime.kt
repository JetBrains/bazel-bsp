package org.jetbrains.bsp.bazel.server.bsp

import org.jetbrains.bsp.bazel.workspacecontext.WorkspaceContextProvider
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.system.exitProcess

class BazelBspServerLifetime(private val workspaceContextProvider: WorkspaceContextProvider) {
  private val initializedStatus = CompletableFuture<Any?>()
  private val finishedStatus = CompletableFuture<Any?>()

  val isInitialized: Boolean
    get() {
      try {
        initializedStatus.get(1, TimeUnit.SECONDS)
      } catch (e: InterruptedException) {
        return false
      } catch (e: ExecutionException) {
        return false
      } catch (e: TimeoutException) {
        return false
      }
      return true
    }

  val isFinished: Boolean
    get() = finishedStatus.isDone

  fun initialize() {
    // Run it here to force the workspace context to be initialized
    // It should download bazelisk if bazel is missing
    workspaceContextProvider.currentWorkspaceContext()

    initializedStatus.complete(null)
  }

  fun finish() {
    finishedStatus.complete(null)
  }

  fun forceFinish() {
    try {
      finishedStatus.get(1, TimeUnit.SECONDS)
    } catch (e: InterruptedException) {
      exitProcess(1)
    } catch (e: ExecutionException) {
      exitProcess(1)
    } catch (e: TimeoutException) {
      exitProcess(1)
    }
    exitProcess(0)
  }
}
