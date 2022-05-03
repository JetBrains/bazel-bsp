package org.jetbrains.bsp.bazel.bazelrunner

import org.apache.logging.log4j.LogManager
import org.jetbrains.bsp.bazel.logger.BspClientLogger

class BazelRunner private constructor(
    private val bazelPathProvider: BazelPathProvider,
    private val bspClientLogger: BspClientLogger,
    private val bazelInfo: BazelInfo?,
    private val defaultFlags: List<String>
) {

  companion object {
    private val LOGGER = LogManager.getLogger(BazelRunner::class.java)

    // This is runner without workspace path. It is used to determine workspace
    // path and create a fully functional runner.
    @JvmStatic
    fun inCwd(bazelPath: BazelPathProvider, bspClientLogger: BspClientLogger): BazelRunner {
      return BazelRunner(bazelPath, bspClientLogger, bazelInfo = null, emptyList())
    }

    @JvmStatic
    fun of(
        bazelPath: BazelPathProvider,
        bspClientLogger: BspClientLogger,
        bazelInfo: BazelInfo?,
        defaultFlags: List<String>): BazelRunner {
      return BazelRunner(bazelPath, bspClientLogger, bazelInfo, defaultFlags)
    }
  }

  private var besBackendPort: Int? = null

  fun commandBuilder(): BazelRunnerCommandBuilder = BazelRunnerCommandBuilder(this)

  fun runBazelCommandBes(command: String, flags: List<String>, arguments: List<String>): BazelProcess {
    fun besFlags() = listOf(
        "--bes_backend=grpc://localhost:${besBackendPort!!}",
        "--build_event_publish_all_actions")

    return runBazelCommand(command, flags = besFlags() + flags, arguments)
  }

  fun runBazelCommand(command: String, flags: List<String>, arguments: List<String>): BazelProcess {
    val processArgs = listOf(bazel(), command) + defaultFlags + flags + arguments
    logInvocation(processArgs)
    val processBuilder = ProcessBuilder(processArgs)
    bazelInfo?.let { processBuilder.directory(it.workspaceRoot.toFile()) }
    val process = processBuilder.start()
    return BazelProcess(process, bspClientLogger)
  }

  private fun logInvocation(processArgs: List<String>) {
    "Invoking: ${processArgs.joinToString(" ")}"
        .also { LOGGER.info(it) }
        .also { bspClientLogger.message(it) }
  }

  private fun bazel() = bazelPathProvider.currentBazelPath()

  fun setBesBackendPort(port: Int) {
    besBackendPort = port
  }
}
