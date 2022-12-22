package org.jetbrains.bsp.bazel.base

import ch.epfl.scala.bsp4j.BuildClientCapabilities
import ch.epfl.scala.bsp4j.InitializeBuildParams
import org.apache.logging.log4j.LogManager
import org.jetbrains.bsp.testkit.client.bazel.BazelTestClient
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.system.exitProcess

abstract class BazelBspTestBaseScenario {
    val targetPrefix = Runtime.getRuntime().exec("bazel version").inputStream.bufferedReader().readText()
            .let {"""(?<=Build label: )\d+(?=[0-9.]+)""".toRegex().find(it)!!.value.toInt() }
            .let { if(it < 6) "" else "@" }

    protected val testClient = createClient()

    private fun createClient(): BazelTestClient {
        val workspaceDir = System.getenv("BUILD_WORKSPACE_DIRECTORY")
        val bazelWorkspace = Path.of(workspaceDir, TEST_RESOURCES_DIR, repoName())
        val testRepoWorkspaceDir = System.getenv("BSP_WORKSPACE").let { Paths.get(it) }

        log.info("Testing repo workspace path: $testRepoWorkspaceDir")
        log.info("Creating TestClient...")

        // TODO: capabilities should be configurable
        val capabilities = BuildClientCapabilities(listOf("java", "scala", "kotlin", "cpp"))
        val initializeBuildParams = InitializeBuildParams(
            "BspTestClient",
            "1.0.0",
            "2.0.0",
            testRepoWorkspaceDir.toString(),
            capabilities
        )
        return BazelTestClient(bazelWorkspace, initializeBuildParams)
            .also { log.info("Created TestClient done.") }
    }

    protected abstract fun repoName(): String

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
        private const val TEST_RESOURCES_DIR = "e2e/test-resources"
    }
}
