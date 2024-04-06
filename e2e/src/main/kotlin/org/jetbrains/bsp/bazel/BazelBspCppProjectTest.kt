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
import kotlin.time.Duration.Companion.seconds

object BazelBspCppProjectTest : BazelBspTestBaseScenario() {
  private val testClient = createTestkitClient()

  @JvmStatic
  fun main(args: Array<String>) = executeScenario()

  override fun scenarioSteps(): List<BazelBspTestScenarioStep> = listOf(compareWorkspaceTargetsResults(), cppOptions())

  private fun compareWorkspaceTargetsResults(): BazelBspTestScenarioStep {
    val expectedWorkspaceBuildTargetsResult = expectedWorkspaceBuildTargetsResult()

    return BazelBspTestScenarioStep("cpp project") {
      testClient.testWorkspaceTargets(20.seconds, expectedWorkspaceBuildTargetsResult)
    }
  }

  override fun expectedWorkspaceBuildTargetsResult(): WorkspaceBuildTargetsResult {
    val exampleExampleCppBuildTarget = CppBuildTarget().also {
      it.version = null; it.compiler = "compiler"; it.cCompiler = "/bin/gcc"; it.cppCompiler = "/bin/gcc"
    }

    val exampleExampleBuildTarget = BuildTarget(BuildTargetIdentifier("$targetPrefix//example:example"),
      ImmutableList.of("application"),
      ImmutableList.of(Constants.CPP),
      ImmutableList.of(BuildTargetIdentifier("@com_google_googletest//:gtest_main")),
      BuildTargetCapabilities().also {
        it.canCompile = true
        it.canTest = false
        it.canRun = true
        it.canDebug = false
      })
    exampleExampleBuildTarget.displayName = "$targetPrefix//example:example"
    exampleExampleBuildTarget.baseDirectory = "file://\$WORKSPACE/example/"
    exampleExampleBuildTarget.data = exampleExampleCppBuildTarget
    exampleExampleBuildTarget.dataKind = BuildTargetDataKind.CPP

    val bspWorkspaceRootExampleBuildTarget = BuildTarget(BuildTargetIdentifier("bsp-workspace-root"),
      ImmutableList.of(),
      ImmutableList.of(),
      ImmutableList.of(),
      BuildTargetCapabilities().also {
        it.canCompile = false; it.canTest = false; it.canRun = false; it.canDebug = false
      })
    bspWorkspaceRootExampleBuildTarget.baseDirectory = "file://\$WORKSPACE/"
    bspWorkspaceRootExampleBuildTarget.displayName = "bsp-workspace-root"

    return WorkspaceBuildTargetsResult(ImmutableList.of(exampleExampleBuildTarget, bspWorkspaceRootExampleBuildTarget))
  }

  private fun cppOptions(): BazelBspTestScenarioStep {
    val cppOptionsParams = CppOptionsParams(ImmutableList.of(BuildTargetIdentifier("$targetPrefix//example:example")))

    val exampleExampleCppOptionsItem = CppOptionsItem(
      BuildTargetIdentifier("$targetPrefix//example:example"),
      ImmutableList.of("-Iexternal/gtest/include"),
      ImmutableList.of("BOOST_FALLTHROUGH"),
      ImmutableList.of("-pthread")
    )

    val expectedCppOptionsResult = CppOptionsResult(ImmutableList.of(exampleExampleCppOptionsItem))

    return BazelBspTestScenarioStep("cpp options") {
      testClient.testCppOptions(20.seconds, cppOptionsParams, expectedCppOptionsResult)
    }
  }
}
