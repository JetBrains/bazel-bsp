package org.jetbrains.bsp.bazel

import ch.epfl.scala.bsp4j.*
import org.jetbrains.bsp.bazel.base.BazelBspTestBaseScenario
import org.jetbrains.bsp.bazel.base.BazelBspTestScenarioStep
import java.time.Duration

object BazelBspRemoteJdkTest : BazelBspTestBaseScenario() {

    // TODO: https://youtrack.jetbrains.com/issue/BAZEL-95
    @JvmStatic
    fun main(args: Array<String>) = executeScenario()

    override fun repoName(): String = "remote-jdk-project"

    override fun scenarioSteps(): List<BazelBspTestScenarioStep> = listOf(workspaceBuildTargets())

    private fun workspaceBuildTargets(): BazelBspTestScenarioStep {
        val exampleExampleJvmBuildTarget = JvmBuildTarget(
            "file://\$BAZEL_CACHE/external/remotejdk11_linux/",
            "11"
        )

        val exampleExampleBuildTarget = BuildTarget(
            BuildTargetIdentifier("//example:example"),
            listOf("application"),
            listOf("java"),
            emptyList(),
            BuildTargetCapabilities(true, false, true, false)
        )
        exampleExampleBuildTarget.displayName = "//example:example"
        exampleExampleBuildTarget.baseDirectory = "file://\$WORKSPACE/example/"
        exampleExampleBuildTarget.data = exampleExampleJvmBuildTarget
        exampleExampleBuildTarget.dataKind = BuildTargetDataKind.JVM

        val workspaceBuildTargetsResult = WorkspaceBuildTargetsResult(
            listOf(exampleExampleBuildTarget)
        )
        return BazelBspTestScenarioStep("workspace build targets") {
            testClient.testWorkspaceTargets(
                Duration.ofSeconds(60),
                workspaceBuildTargetsResult
            )
        }
    }
}
