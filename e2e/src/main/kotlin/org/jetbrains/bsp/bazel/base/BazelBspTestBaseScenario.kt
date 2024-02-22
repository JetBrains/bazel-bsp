package org.jetbrains.bsp.bazel.base

import ch.epfl.scala.bsp4j.BuildClientCapabilities
import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import ch.epfl.scala.bsp4j.InitializeBuildParams
import ch.epfl.scala.bsp4j.WorkspaceBuildTargetsResult
import org.apache.logging.log4j.LogManager
import org.jetbrains.bsp.bazel.install.Install
import org.jetbrains.bsp.testkit.client.bazel.BazelTestClient
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.name
import kotlin.system.exitProcess

abstract class BazelBspTestBaseScenario {

    protected val binary = System.getenv("BIT_BAZEL_BINARY")
    protected val workspaceDir = System.getenv("BIT_WORKSPACE_DIR")

    val targetPrefix = calculateTargetPrefix()
    protected val testClient: BazelTestClient

    init {
        installServer()
        testClient = createClient()
    }

    // check: https://github.com/bazelbuild/intellij/blob/adb358670a7fc6ad51808486dc03f4605f83dcd3/aspect/testing/tests/src/com/google/idea/blaze/aspect/integration/BazelInvokingIntegrationTestRunner.java#L132
    private fun calculateTargetPrefix(): String {
        val dirName = Path(binary).parent.name
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
                "-b", binary,
                "-t", "//...",
            )
        )
    }

    private fun processBazelOutput(vararg args: String): String {
        val command = arrayOf<String>(binary, *args)
        val process = ProcessBuilder(*command)
            .directory(Path(workspaceDir).toFile())
            .start()
        val output = process.inputStream.bufferedReader().readText().trim()
        val exitCode = process.waitFor()
        if (exitCode != 0) {
            val error = process.errorStream.bufferedReader().readText().trim()
            throw RuntimeException("Command '${command.joinToString(" ")}' failed with exit code $exitCode.\n$error")
        }
        return output
    }

    private fun createClient(): BazelTestClient {
        log.info("Testing repo workspace path: $workspaceDir")
        log.info("Creating TestClient...")

        val capabilities = BuildClientCapabilities(listOf("java", "scala", "kotlin", "cpp"))
        val initializeBuildParams = InitializeBuildParams(
            "BspTestClient",
            "1.0.0",
            "2.0.0",
            workspaceDir,
            capabilities
        )

        val bazelCache = Path(processBazelOutput("info", "execution_root"))
        val bazelOutputBase = Path(processBazelOutput("info", "output_base"))

        return BazelTestClient(Path.of(workspaceDir), initializeBuildParams, bazelCache, bazelOutputBase)
            .also { log.info("Created TestClient done.") }
    }

    fun executeScenario() {
        log.info("Running scenario...")
        val scenarioStepsExecutionResult = executeScenarioSteps()
        log.info("Running scenario done.")

        when (scenarioStepsExecutionResult) {
            true -> {
                log.info("Test passed!")
                exitProcess(SUCCESS_EXIT_CODE)
            }

            false -> {
                log.fatal("Test failed :( ")
                exitProcess(FAIL_EXIT_CODE)
            }
        }
    }

    private fun executeScenarioSteps(): Boolean = scenarioSteps()
        .map { it.executeAndReturnResult() }
        .all { it }

    protected fun expectedTargetIdentifiers(): List<BuildTargetIdentifier> =
        expectedWorkspaceBuildTargetsResult()
            .targets
            .map { it.id }

    protected abstract fun expectedWorkspaceBuildTargetsResult(): WorkspaceBuildTargetsResult

    protected abstract fun scenarioSteps(): List<BazelBspTestScenarioStep>

    companion object {
        private val log = LogManager.getLogger(BazelBspTestBaseScenario::class.java)

        private const val SUCCESS_EXIT_CODE = 0
        private const val FAIL_EXIT_CODE = 1
    }
}
