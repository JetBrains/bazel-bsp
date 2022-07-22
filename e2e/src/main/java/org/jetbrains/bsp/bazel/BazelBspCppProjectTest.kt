package org.jetbrains.bsp.bazel

import ch.epfl.scala.bsp4j.BuildTarget
import ch.epfl.scala.bsp4j.BuildTargetCapabilities
import ch.epfl.scala.bsp4j.BuildTargetDataKind
import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import ch.epfl.scala.bsp4j.CppBuildTarget
import ch.epfl.scala.bsp4j.CppOptionsItem
import ch.epfl.scala.bsp4j.CppOptionsParams
import ch.epfl.scala.bsp4j.CppOptionsResult
import ch.epfl.scala.bsp4j.WorkspaceBuildTargetsResult
import com.google.common.collect.ImmutableList
import org.jetbrains.bsp.bazel.base.BazelBspTestBaseScenario
import org.jetbrains.bsp.bazel.base.BazelBspTestScenarioStep
import org.jetbrains.bsp.bazel.commons.Constants
import java.time.Duration

object BazelBspCppProjectTest : BazelBspTestBaseScenario() {

    @JvmStatic
    fun main(args: Array<String>) = executeScenario()

    override fun repoName(): String = "cpp-project"

    override fun scenarioSteps(): List<BazelBspTestScenarioStep> =
        listOf(compareWorkspaceTargetsResults(), cppOptions())

    private fun compareWorkspaceTargetsResults(): BazelBspTestScenarioStep {
        val exampleExampleCppBuildTarget = CppBuildTarget(null, "compiler", "/bin/gcc", "/bin/gcc")

        val exampleExampleBuildTarget1 =
            BuildTarget(
                BuildTargetIdentifier("//example:example"),
                ImmutableList.of("application"),
                ImmutableList.of(Constants.CPP),
                ImmutableList.of(BuildTargetIdentifier("@com_google_googletest//:gtest_main")),
                BuildTargetCapabilities(true, false, true)
            )
        exampleExampleBuildTarget1.displayName = "//example:example"
        exampleExampleBuildTarget1.baseDirectory = "file://\$WORKSPACE/example/"
        exampleExampleBuildTarget1.data = exampleExampleCppBuildTarget
        exampleExampleBuildTarget1.dataKind = BuildTargetDataKind.CPP

        val exampleExampleBuildTarget2 =
            BuildTarget(
                BuildTargetIdentifier("bsp-workspace-root"),
                ImmutableList.of(),
                ImmutableList.of(),
                ImmutableList.of(),
                BuildTargetCapabilities(false, false, false)
            )
        exampleExampleBuildTarget2.baseDirectory = "file://\$WORKSPACE/"
        exampleExampleBuildTarget2.displayName = "bsp-workspace-root"
        val expectedWorkspaceBuildTargetsResult =
            WorkspaceBuildTargetsResult(ImmutableList.of(exampleExampleBuildTarget1, exampleExampleBuildTarget2))

        return BazelBspTestScenarioStep("cpp project") {
            testClient.testWorkspaceTargets(Duration.ofSeconds(20), expectedWorkspaceBuildTargetsResult)
        }
    }

    private fun cppOptions(): BazelBspTestScenarioStep {
        val cppOptionsParams =
            CppOptionsParams(ImmutableList.of(BuildTargetIdentifier("//example:example")))

        val exampleExampleCppOptionsItem =
            CppOptionsItem(
                BuildTargetIdentifier("//example:example"),
                ImmutableList.of("-Iexternal/gtest/include"),
                ImmutableList.of("BOOST_FALLTHROUGH"),
                ImmutableList.of("-pthread")
            )

        val expectedCppOptionsResult =
            CppOptionsResult(ImmutableList.of(exampleExampleCppOptionsItem))

        return BazelBspTestScenarioStep("cpp options") {
            testClient.testCppOptions(Duration.ofSeconds(20), cppOptionsParams, expectedCppOptionsResult)
        }
    }
}
