package org.jetbrains.bsp.bazel.base

import ch.epfl.scala.bsp4j.BuildClientCapabilities
import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import ch.epfl.scala.bsp4j.InitializeBuildParams
import ch.epfl.scala.bsp4j.WorkspaceBuildTargetsResult
import org.apache.logging.log4j.LogManager
import org.jetbrains.bsp.JoinedBuildServer
import org.jetbrains.bsp.bazel.install.Install
import org.jetbrains.bsp.testkit.client.BasicTestClient
import org.jetbrains.bsp.testkit.client.MockClient
import org.jetbrains.bsp.testkit.client.TestClient
import org.jetbrains.bsp.testkit.client.bazel.BazelJsonTransformer
import kotlin.io.path.Path
import kotlin.io.path.name
import kotlin.system.exitProcess

abstract class BazelBspTestBaseScenario {

  protected val bazelBinary = System.getenv("BIT_BAZEL_BINARY")
  protected val workspaceDir = System.getenv("BIT_WORKSPACE_DIR")

  val targetPrefix = calculateTargetPrefix()

  init {
    installServer()
  }

  // check: https://github.com/bazelbuild/intellij/blob/adb358670a7fc6ad51808486dc03f4605f83dcd3/aspect/testing/tests/src/com/google/idea/blaze/aspect/integration/BazelInvokingIntegrationTestRunner.java#L132
  private fun calculateTargetPrefix(): String {
    val dirName = Path(bazelBinary).parent.name
    // With bzlmod enabled the directory name is something like:
    // rules_bazel_integration_test~0.18.0~bazel_binaries~build_bazel_bazel_6_3_2
    val bazelPart = if (dirName.contains("~")) dirName.split("~")[3] else dirName
    val majorVersion = bazelPart.split("_")[3].toIntOrNull()
    // null for .bazelversion, we can assume that it's > 6, so we can return "@" anyway
    return if (majorVersion != null && majorVersion < 6) "" else "@"
  }

  protected open fun installServer() {
    Install.main(
      arrayOf(
        "-d", workspaceDir,
        "-b", bazelBinary,
        "-t", "//...",
      )
    )
  }

  protected fun processBazelOutput(vararg args: String): String {
    val command = arrayOf<String>(bazelBinary, *args)
    val process = ProcessBuilder(*command).directory(Path(workspaceDir).toFile()).start()
    val output = process.inputStream.bufferedReader().readText().trim()
    val exitCode = process.waitFor()
    if (exitCode != 0) {
      val error = process.errorStream.bufferedReader().readText().trim()
      throw RuntimeException("Command '${command.joinToString(" ")}' failed with exit code $exitCode.\n$error")
    }
    return output
  }

  fun executeScenario() {
    log.info("Running scenario...")
    val scenarioStepsExecutionResult = executeScenarioSteps()
    log.info("Running scenario done.")

    when (scenarioStepsExecutionResult) {
      true -> {
        log.info("Test passed")
        exitProcess(SUCCESS_EXIT_CODE)
      }

      false -> {
        log.fatal("Test failed")
        exitProcess(FAIL_EXIT_CODE)
      }
    }
  }

  protected fun createTestkitClient(): TestClient {
    log.info("Testing repo workspace path: $workspaceDir")
    log.info("Creating TestClient...")

    val capabilities = BuildClientCapabilities(listOf("java", "scala", "kotlin", "cpp"))
    val initializeBuildParams = InitializeBuildParams(
      "BspTestClient", "1.0.0", "2.0.0", workspaceDir, capabilities
    )

    val bazelCache = Path(processBazelOutput("info", "execution_root"))
    val bazelOutputBase = Path(processBazelOutput("info", "output_base"))

    val bazelJsonTransformer = BazelJsonTransformer(
      java.nio.file.Path.of(workspaceDir), bazelCache, bazelOutputBase
    )

    return TestClient(
      java.nio.file.Path.of(workspaceDir),
      initializeBuildParams,
      { s: String -> bazelJsonTransformer.transformJson(s) }
    ).also { log.info("Created TestClient done.") }
  }

  protected fun createBazelClient(): BazelTestClient {
    log.info("Testing repo workspace path: $workspaceDir")
    log.info("Creating TestClient...")

    val capabilities = BuildClientCapabilities(listOf("java", "scala", "kotlin", "cpp"))
    val initializeBuildParams = InitializeBuildParams(
      "BspTestClient", "1.0.0", "2.0.0", workspaceDir, capabilities
    )

    val bazelCache = Path(processBazelOutput("info", "execution_root"))
    val bazelOutputBase = Path(processBazelOutput("info", "output_base"))

    val bazelJsonTransformer = BazelJsonTransformer(
      java.nio.file.Path.of(workspaceDir), bazelCache, bazelOutputBase
    )

    return BazelTestClient(
      java.nio.file.Path.of(workspaceDir),
      initializeBuildParams,
      { s: String -> bazelJsonTransformer.transformJson(s) },
      MockClient(),
      JoinedBuildServer::class.java
    ).also { log.info("Created TestClient done.") }
  }

  private fun executeScenarioSteps(): Boolean = scenarioSteps().map { it.executeAndReturnResult() }.all { it }

  protected fun expectedTargetIdentifiers(): List<BuildTargetIdentifier> =
    expectedWorkspaceBuildTargetsResult().targets.map { it.id }

  protected abstract fun expectedWorkspaceBuildTargetsResult(): WorkspaceBuildTargetsResult

  protected abstract fun scenarioSteps(): List<BazelBspTestScenarioStep>

  companion object {
    @JvmStatic
    private val log = LogManager.getLogger(BazelBspTestBaseScenario::class.java)

    private const val SUCCESS_EXIT_CODE = 0
    private const val FAIL_EXIT_CODE = 1


  }
}
