package org.jetbrains.bsp.bazel

import org.jetbrains.bsp.bazel.base.BazelBspTestBaseScenario
import org.jetbrains.bsp.bazel.base.BazelBspTestScenarioStep
import org.jetbrains.bsp.bazel.install.Install
import kotlin.time.Duration.Companion.minutes

object ServerDownloadsBazeliskTest : BazelBspTestBaseScenario() {
    @JvmStatic
    fun main(args: Array<String>) = executeScenario()

    override fun installServer() {
        // DO NOT supply the -b flag to test whether bazelisk is downloaded
        Install.main(
            arrayOf(
                "-d", workspaceDir,
                "-t", "//...",
                "--produce-trace-log"
            )
        )
    }

    override fun scenarioSteps(): List<BazelBspTestScenarioStep> = listOf(resolveProject())

    private fun resolveProject(): BazelBspTestScenarioStep = BazelBspTestScenarioStep(
        "resolve project"
    ) { testClient.testResolveProject(2.minutes) }
}