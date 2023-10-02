package org.jetbrains.bsp.bazel

import ch.epfl.scala.bsp4j.BuildTarget
import ch.epfl.scala.bsp4j.BuildTargetCapabilities
import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import ch.epfl.scala.bsp4j.WorkspaceBuildTargetsResult
import com.google.common.collect.ImmutableList
import org.jetbrains.bsp.bazel.base.BazelBspTestBaseScenario
import org.jetbrains.bsp.bazel.base.BazelBspTestScenarioStep
import java.time.Duration

object BazelBspRustProjectTest : BazelBspTestBaseScenario() {

    @JvmStatic
    fun main(args: Array<String>) = executeScenario()

    override fun scenarioSteps(): List<BazelBspTestScenarioStep> = listOf(
        workspaceBuildTargets(),
        // TODO: rustWorkspacePackaging(),
    )

    private fun workspaceBuildTargets(): BazelBspTestScenarioStep {
        val workspaceBuildTargetsResult = WorkspaceBuildTargetsResult(
            listOf(
                makeRoot(),
                makeExampleExampleLib(),
                makeExampleExampleFeature(),
                makeExampleExample()
            )
        )

        return BazelBspTestScenarioStep("workspace build targets") {
            testClient.testWorkspaceTargets(
                Duration.ofSeconds(60),
                workspaceBuildTargetsResult
            )
        }
    }

    private fun makeRoot(): BuildTarget {
        val bspWorkspaceRootExampleBuildTarget =
            BuildTarget(
                BuildTargetIdentifier("bsp-workspace-root"),
                ImmutableList.of(),
                ImmutableList.of(),
                ImmutableList.of(),
                BuildTargetCapabilities().also { it.canCompile = false; it.canTest = false; it.canRun = false; it.canDebug = false }
            )
        bspWorkspaceRootExampleBuildTarget.baseDirectory = "file://\$WORKSPACE/"
        bspWorkspaceRootExampleBuildTarget.displayName = "bsp-workspace-root"
        return bspWorkspaceRootExampleBuildTarget
    }

    private fun makeExampleExampleLib(): BuildTarget {
        val exampleExampleLibRustBuildTargetDependencies = listOf(
            BuildTargetIdentifier("@crate_index__serde-1.0.160//:serde"),
            BuildTargetIdentifier("@crate_index__serde_json-1.0.96//:serde_json")
        )
        val exampleExampleLibRustBuildTarget = BuildTarget(
            BuildTargetIdentifier("$targetPrefix//example-lib:example_lib"),
            listOf("library"),
            listOf("rust"),
            exampleExampleLibRustBuildTargetDependencies,
            BuildTargetCapabilities().also { it.canCompile = true; it.canTest = false; it.canRun = false; it.canDebug = false }
        )
        exampleExampleLibRustBuildTarget.displayName = "$targetPrefix//example-lib:example_lib"
        exampleExampleLibRustBuildTarget.baseDirectory = "file://\$WORKSPACE/example-lib/"
        return exampleExampleLibRustBuildTarget
    }

    private fun makeExampleExample(): BuildTarget {
        val exampleExampleRustBuildTargetDependencies = listOf(
            BuildTargetIdentifier("$targetPrefix//example-lib:example_lib"),
            BuildTargetIdentifier("@crate_index__itertools-0.10.5//:itertools"),
        )
        val exampleExampleRustBuildTarget = BuildTarget(
            BuildTargetIdentifier("$targetPrefix//example:example"),
            listOf("application"),
            listOf("rust"),
            exampleExampleRustBuildTargetDependencies,
            BuildTargetCapabilities().also { it.canCompile = true; it.canTest = false; it.canRun = true; it.canDebug = false }
        )
        exampleExampleRustBuildTarget.displayName = "$targetPrefix//example:example"
        exampleExampleRustBuildTarget.baseDirectory = "file://\$WORKSPACE/example/"
        return exampleExampleRustBuildTarget
    }

    private fun makeExampleExampleFeature(): BuildTarget {
        val exampleExampleFeatureRustBuildTargetDependencies = listOf(
            BuildTargetIdentifier("$targetPrefix//example-lib:example_lib"),
            BuildTargetIdentifier("@crate_index__itertools-0.10.5//:itertools"),
            BuildTargetIdentifier("@crate_index__itoa-1.0.6//:itoa")
        )
        val exampleExampleFeatureRustBuildTarget = BuildTarget(
            BuildTargetIdentifier("$targetPrefix//example:example_foo"),
            listOf("application"),
            listOf("rust"),
            exampleExampleFeatureRustBuildTargetDependencies,
            BuildTargetCapabilities().also { it.canCompile = true; it.canTest = false; it.canRun = true; it.canDebug = false }
        )
        exampleExampleFeatureRustBuildTarget.displayName = "$targetPrefix//example:example_foo"
        exampleExampleFeatureRustBuildTarget.baseDirectory = "file://\$WORKSPACE/example/"
        return exampleExampleFeatureRustBuildTarget
    }
}
