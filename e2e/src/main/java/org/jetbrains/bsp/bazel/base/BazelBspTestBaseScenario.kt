package org.jetbrains.bsp.bazel.base

import ch.epfl.scala.bsp4j.BuildClientCapabilities
import ch.epfl.scala.bsp4j.InitializeBuildParams
import org.apache.logging.log4j.LogManager
import org.jetbrains.bsp.bazel.install.Install
import org.jetbrains.bsp.testkit.client.bazel.BazelTestClient
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.name
import kotlin.system.exitProcess

abstract class BazelBspTestBaseScenario {

    private val binary = System.getenv("BIT_BAZEL_BINARY")
    private val workspaceDir = System.getenv("BIT_WORKSPACE_DIR")

    val targetPrefix = calculateTargetPrefix()
    protected val testClient: BazelTestClient

    init {
        installServer()
        testClient = createClient()
    }

    // check: https://github.com/bazelbuild/intellij/blob/adb358670a7fc6ad51808486dc03f4605f83dcd3/aspect/testing/tests/src/com/google/idea/blaze/aspect/integration/BazelInvokingIntegrationTestRunner.java#L132
    private fun calculateTargetPrefix(): String {
        val dirName = Path(binary).parent.name
        val majorVersion = dirName.split("_")[3].toInt()
        return if (majorVersion < 6) "" else "@"
    }

    private fun installServer() {
        Install.main(
            arrayOf(
                "-d", workspaceDir,
                "-b", binary,
                "-t", "//...",
            )
        )
    }
    private fun createClient(): BazelTestClient {
        log.info("Testing repo workspace path: $workspaceDir")
        log.info("Creating TestClient...")

        // TODO: capabilities should be configurable
        val capabilities = BuildClientCapabilities(listOf("java", "scala", "kotlin", "cpp"))
        val initializeBuildParams = InitializeBuildParams(
            "BspTestClient",
            "1.0.0",
            "2.0.0",
            workspaceDir,
            capabilities
        )
        return BazelTestClient(Path.of(workspaceDir), initializeBuildParams)
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

    protected abstract fun scenarioSteps(): List<BazelBspTestScenarioStep>

    companion object {
        private val log = LogManager.getLogger(BazelBspTestBaseScenario::class.java)

        private const val SUCCESS_EXIT_CODE = 0
        private const val FAIL_EXIT_CODE = 1
    }
}
