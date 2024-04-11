package org.jetbrains.bsp.bazel

import ch.epfl.scala.bsp4j.BuildTarget
import ch.epfl.scala.bsp4j.BuildTargetCapabilities
import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import ch.epfl.scala.bsp4j.JvmBuildTarget
import ch.epfl.scala.bsp4j.WorkspaceBuildTargetsResult
import org.jetbrains.bsp.KotlinBuildTarget
import org.jetbrains.bsp.bazel.base.BazelBspTestBaseScenario
import org.jetbrains.bsp.bazel.base.BazelBspTestScenarioStep
import org.jetbrains.bsp.bazel.install.Install
import kotlin.time.Duration.Companion.seconds

object BazelBspKotlinProjectTest : BazelBspTestBaseScenario() {
  private val testClient = createTestkitClient()

  @JvmStatic
  fun main(args: Array<String>) = executeScenario()

  override fun installServer() {
    Install.main(
      arrayOf(
        "-d", workspaceDir,
        "-b", bazelBinary,
        "-t", "//...",
        "--enabled-rules", "rules_kotlin",
      )
    )
  }

  override fun scenarioSteps(): List<BazelBspTestScenarioStep> = listOf(
    compareWorkspaceTargetsResults(),
  )

  override fun expectedWorkspaceBuildTargetsResult(): WorkspaceBuildTargetsResult {
    val jvmBuildTargetData = JvmBuildTarget().also {
      it.javaHome = "file://\$BAZEL_OUTPUT_BASE_PATH/external/local_jdk/"
      it.javaVersion = "17"
    }

    val kotlinBuildTargetData = KotlinBuildTarget(
      languageVersion = "1.9",
      apiVersion = "1.9",
      associates = listOf(),
      kotlincOptions = listOf(
        "-Xno-call-assertions",
        "-Xno-param-assertions",
        "-Xsam-conversions=class",
        "-Xlambdas=class",
        "-Xno-source-debug-extension",
        "-jvm-target=1.8"
      ),
      jvmBuildTarget = jvmBuildTargetData,
    )

    val buildTarget = BuildTarget(BuildTargetIdentifier("$targetPrefix//kotlinc_test:Foo"),
      listOf("application"),
      listOf("java", "kotlin"),
      listOf(BuildTargetIdentifier("rules_kotlin_kotlin-stdlibs")),
      BuildTargetCapabilities().also {
        it.canCompile = true
        it.canTest = false
        it.canRun = true
        it.canDebug = true
      })
    buildTarget.displayName = "@//kotlinc_test:Foo"
    buildTarget.baseDirectory = "file://\$WORKSPACE/kotlinc_test/"
    buildTarget.data = kotlinBuildTargetData
    buildTarget.dataKind = "kotlin"
    return WorkspaceBuildTargetsResult(
      listOf(buildTarget)
    )
  }

  private fun compareWorkspaceTargetsResults(): BazelBspTestScenarioStep = BazelBspTestScenarioStep(
    "compare workspace targets results"
  ) { testClient.testWorkspaceTargets(60.seconds, expectedWorkspaceBuildTargetsResult()) }
}
