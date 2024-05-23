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

    val kotlincTestBuildTargetData = KotlinBuildTarget(
      languageVersion = "1.9",
      apiVersion = "1.9",
      kotlincOptions = listOf(
        "-Xno-call-assertions",
        "-Xno-param-assertions",
        "-Xsam-conversions=class",
        "-Xlambdas=class",
        "-Xno-source-debug-extension",
        "-jvm-target=1.8"
      ),
      associates = listOf(),
      jvmBuildTarget = jvmBuildTargetData,
    )

    val kotlincTestBuildTarget = BuildTarget(BuildTargetIdentifier("$targetPrefix//kotlinc_test:Foo"),
      listOf("application"),
      listOf("java", "kotlin"),
      listOf(BuildTargetIdentifier("rules_kotlin_kotlin-stdlibs")),
      BuildTargetCapabilities().also {
        it.canCompile = true
        it.canTest = false
        it.canRun = true
        it.canDebug = true
      })
    kotlincTestBuildTarget.displayName = "@//kotlinc_test:Foo"
    kotlincTestBuildTarget.baseDirectory = "file://\$WORKSPACE/kotlinc_test/"
    kotlincTestBuildTarget.data = kotlincTestBuildTargetData
    kotlincTestBuildTarget.dataKind = "kotlin"


    val openForTestingBuildTargetData = KotlinBuildTarget(
      languageVersion = "1.9",
      apiVersion = "1.9",
      kotlincOptions = listOf(
        "-Xsam-conversions=class",
        "-Xlambdas=class",
        "-Xno-source-debug-extension",
        "-jvm-target=1.8"
      ),
      associates = listOf(),
      jvmBuildTarget = jvmBuildTargetData,
    )

    val openForTestingBuildTarget = BuildTarget(BuildTargetIdentifier("$targetPrefix//plugin_allopen_test:open_for_testing"),
      listOf("library"),
      listOf("java", "kotlin"),
      listOf(BuildTargetIdentifier("rules_kotlin_kotlin-stdlibs")),
      BuildTargetCapabilities().also {
        it.canCompile = true
        it.canTest = false
        it.canRun = false
        it.canDebug = false
      })
    openForTestingBuildTarget.displayName = "@//plugin_allopen_test:open_for_testing"
    openForTestingBuildTarget.baseDirectory = "file://\$WORKSPACE/plugin_allopen_test/"
    openForTestingBuildTarget.data = openForTestingBuildTargetData
    openForTestingBuildTarget.dataKind = "kotlin"

    val userBuildTargetData = KotlinBuildTarget(
      languageVersion = "1.9",
      apiVersion = "1.9",
      kotlincOptions = listOf(
        "-P",
        "-Xlambdas=class",
        "-Xno-source-debug-extension",
        "-Xplugin=\$BAZEL_OUTPUT_BASE_PATH/external/com_github_jetbrains_kotlin/lib/allopen-compiler-plugin.jar",
        "-Xsam-conversions=class",
        "-jvm-target=1.8",
        "plugin:org.jetbrains.kotlin.allopen:annotation=plugin.allopen.OpenForTesting"
      ),
      associates = listOf(),
      jvmBuildTarget = jvmBuildTargetData,
    )

    val userBuildTarget = BuildTarget(BuildTargetIdentifier("$targetPrefix//plugin_allopen_test:user"),
      listOf("library"),
      listOf("java", "kotlin"),
      listOf(
        BuildTargetIdentifier("rules_kotlin_kotlin-stdlibs"),
        BuildTargetIdentifier("@//plugin_allopen_test:open_for_testing"),
        BuildTargetIdentifier("allopen-compiler-plugin.jar"),
      ),
      BuildTargetCapabilities().also {
        it.canCompile = true
        it.canTest = false
        it.canRun = false
        it.canDebug = false
      })
    userBuildTarget.displayName = "@//plugin_allopen_test:user"
    userBuildTarget.baseDirectory = "file://\$WORKSPACE/plugin_allopen_test/"
    userBuildTarget.data = userBuildTargetData
    userBuildTarget.dataKind = "kotlin"

    return WorkspaceBuildTargetsResult(
      listOf(kotlincTestBuildTarget, openForTestingBuildTarget, userBuildTarget)
    )
  }

  private fun compareWorkspaceTargetsResults(): BazelBspTestScenarioStep = BazelBspTestScenarioStep(
    "compare workspace targets results"
  ) { testClient.testWorkspaceTargets(60.seconds, expectedWorkspaceBuildTargetsResult()) }
}
