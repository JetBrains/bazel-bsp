package org.jetbrains.bsp.bazel

import ch.epfl.scala.bsp4j.BuildTarget
import ch.epfl.scala.bsp4j.BuildTargetCapabilities
import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import ch.epfl.scala.bsp4j.WorkspaceBuildTargetsResult
import org.jetbrains.bsp.GoBuildTarget
import org.jetbrains.bsp.bazel.base.BazelBspTestBaseScenario
import org.jetbrains.bsp.bazel.base.BazelBspTestScenarioStep
import kotlin.time.Duration.Companion.minutes
import java.net.URI

object BazelBspGoProjectTest : BazelBspTestBaseScenario() {
  private val testClient = createTestkitClient()

  val defaultSdkHomePath = URI("file://\$BAZEL_OUTPUT_BASE_PATH/external/go_sdk/")

  @JvmStatic
  fun main(args: Array<String>) = executeScenario()

  override fun expectedWorkspaceBuildTargetsResult(): WorkspaceBuildTargetsResult =
    WorkspaceBuildTargetsResult(
      listOf(
        exampleBuildTarget(),
        libBuildTarget(),
        libTestBuildTarget(),
      )
    )

  override fun scenarioSteps(): List<BazelBspTestScenarioStep> = listOf(
    workspaceBuildTargets(),
  )

  private fun workspaceBuildTargets(): BazelBspTestScenarioStep =
    BazelBspTestScenarioStep("workspace build targets") {
      testClient.testWorkspaceTargets(
        1.minutes,
        expectedWorkspaceBuildTargetsResult()
      )
    }

  private fun exampleBuildTarget(): BuildTarget =
    createGoBuildTarget(
      targetDirectory = "example",
      targetName = "hello",
      tags = listOf("application"),
      capabilities = BuildTargetCapabilities().also {
        it.canCompile = true; it.canTest = false; it.canRun = true; it.canDebug = true
      },
      dependencies = listOf(
        BuildTargetIdentifier("$targetPrefix//lib:go_default_library")
      )
    )

  private fun libBuildTarget(): BuildTarget =
    createGoBuildTarget(
      targetDirectory = "lib",
      targetName = "go_default_library",
      tags = listOf("library"),
      capabilities = BuildTargetCapabilities().also {
        it.canCompile = true; it.canTest = false; it.canRun = false; it.canDebug = false
      },
      importPath = "example.com/lib",
    )

  private fun libTestBuildTarget(): BuildTarget =
    createGoBuildTarget(
      targetDirectory = "lib",
      targetName = "go_default_test",
      tags = listOf("test"),
      capabilities = BuildTargetCapabilities().also {
        it.canCompile = true; it.canTest = true; it.canRun = false; it.canDebug = true
      }
    )

  private fun createGoBuildTarget(
    targetDirectory: String,
    targetName: String,
    tags: List<String>,
    capabilities: BuildTargetCapabilities,
    importPath: String = "",
    sdkHomePath: URI = defaultSdkHomePath,
    dependencies: List<BuildTargetIdentifier> = listOf(),
  ): BuildTarget {
    val goBuildTarget = GoBuildTarget(
      sdkHomePath = sdkHomePath,
      importPath = importPath,
    )

    val buildTargetData = BuildTarget(
      BuildTargetIdentifier("$targetPrefix//$targetDirectory:$targetName"),
      tags,
      listOf("go"),
      dependencies,
      capabilities
    )

    buildTargetData.displayName = "$targetPrefix//$targetDirectory:$targetName"
    buildTargetData.baseDirectory = "file://\$WORKSPACE/$targetDirectory/"
    buildTargetData.data = goBuildTarget
    buildTargetData.dataKind = "go"

    return buildTargetData
  }
}
