package org.jetbrains.bsp.bazel

import ch.epfl.scala.bsp4j.BuildTarget
import ch.epfl.scala.bsp4j.BuildTargetCapabilities
import ch.epfl.scala.bsp4j.BuildTargetDataKind
import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import ch.epfl.scala.bsp4j.JvmBuildTarget
import ch.epfl.scala.bsp4j.WorkspaceBuildTargetsResult
import org.jetbrains.bsp.bazel.base.BazelBspTestBaseScenario
import org.jetbrains.bsp.bazel.base.BazelBspTestScenarioStep
import kotlin.time.Duration.Companion.seconds

object BazelBspLocalJdkTest : BazelBspTestBaseScenario() {
  private val testClient = createTestkitClient()

  // TODO: https://youtrack.jetbrains.com/issue/BAZEL-95
  @JvmStatic
  fun main(args: Array<String>) = executeScenario()

  override fun scenarioSteps(): List<BazelBspTestScenarioStep> = listOf(workspaceBuildTargets())


  private fun workspaceBuildTargets(): BazelBspTestScenarioStep {
    val workspaceBuildTargetsResult = expectedWorkspaceBuildTargetsResult()

    return BazelBspTestScenarioStep("workspace build targets") {
      testClient.testWorkspaceTargets(
        60.seconds, workspaceBuildTargetsResult
      )
    }
  }

  override fun expectedWorkspaceBuildTargetsResult(): WorkspaceBuildTargetsResult {
    val exampleExampleJvmBuildTarget = JvmBuildTarget().also {
      it.javaHome = "file://\$BAZEL_OUTPUT_BASE_PATH/external/local_jdk/"
      it.javaVersion = "17"
    }

    val exampleExampleBuildTarget = BuildTarget(BuildTargetIdentifier("$targetPrefix//example:example"),
      listOf("application"),
      listOf("java"),
      emptyList(),
      BuildTargetCapabilities().also {
        it.canCompile = true
        it.canTest = false
        it.canRun = true
        it.canDebug = true
      })
    exampleExampleBuildTarget.displayName = "$targetPrefix//example:example"
    exampleExampleBuildTarget.baseDirectory = "file://\$WORKSPACE/example/"
    exampleExampleBuildTarget.data = exampleExampleJvmBuildTarget
    exampleExampleBuildTarget.dataKind = BuildTargetDataKind.JVM

    return WorkspaceBuildTargetsResult(
      listOf(exampleExampleBuildTarget)
    )
  }
}
