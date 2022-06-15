package org.jetbrains.bsp.bazel.base

import ch.epfl.scala.bsp4j.BuildClientCapabilities
import ch.epfl.scala.bsp4j.InitializeBuildParams
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.jetbrains.bsp.testkit.client.bazel.BazelTestClient
import java.nio.file.Path
import java.util.stream.Collectors
import kotlin.system.exitProcess

abstract class BazelBspTestBaseScenario(repoName: String?) {
    @JvmField
    protected val testClient: BazelTestClient

    private fun getTestingRepoPath(repoName: String?): Path = Path.of(WORKSPACE_DIR, TEST_RESOURCES_DIR, repoName)

    private fun createClient(workspacePath: Path): BazelTestClient {
        LOGGER.info("Workspace path: {}", workspacePath)
        LOGGER.info("Creating TestClient...")
        // TODO: capabilities should be configurable
        val capabilities = BuildClientCapabilities(java.util.List.of("java", "scala", "kotlin", "cpp"))
        val initializeBuildParams = InitializeBuildParams(
            "BspTestClient", "1.0.0", BSP_VERSION, workspacePath.toString(), capabilities
        )
        val client = BazelTestClient(workspacePath, initializeBuildParams)
        LOGGER.info("Created TestClient.")
        return client
    }

    fun executeScenario() {
        LOGGER.info("Running scenario...")
        val scenarioStepsExecutionResult = executeScenarioSteps()
        LOGGER.info("Running scenario done.")
        if (scenarioStepsExecutionResult) {
            LOGGER.info("Test passed!")
            exitProcess(SUCCESS_EXIT_CODE)
        }
        LOGGER.fatal("Test failed :(")
        exitProcess(FAIL_EXIT_CODE)
    }

    private fun executeScenarioSteps(): Boolean = scenarioSteps.stream()
            .map { obj: BazelBspTestScenarioStep -> obj.executeAndReturnResult() }
            .collect(Collectors.toList())
            .stream()
            .allMatch { it }

    protected abstract val scenarioSteps: List<BazelBspTestScenarioStep>

    companion object {
        private const val BSP_VERSION = "2.0.0"
        private val LOGGER: Logger = LogManager.getLogger(
            BazelBspTestBaseScenario::class.java
        )
        private const val SUCCESS_EXIT_CODE = 0
        private const val FAIL_EXIT_CODE = 1
        private const val TEST_RESOURCES_DIR = "e2e/test-resources"
        private val WORKSPACE_DIR = System.getenv("BUILD_WORKSPACE_DIRECTORY")
    }

    init {
        val workspacePath = getTestingRepoPath(repoName)
        testClient = createClient(workspacePath)
    }
}