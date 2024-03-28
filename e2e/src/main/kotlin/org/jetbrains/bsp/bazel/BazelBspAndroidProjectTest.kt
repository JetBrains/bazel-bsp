package org.jetbrains.bsp.bazel

import ch.epfl.scala.bsp4j.BuildTarget
import ch.epfl.scala.bsp4j.BuildTargetCapabilities
import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import ch.epfl.scala.bsp4j.JvmBuildTarget
import ch.epfl.scala.bsp4j.WorkspaceBuildTargetsResult
import org.jetbrains.bsp.AndroidBuildTarget
import org.jetbrains.bsp.AndroidTargetType
import org.jetbrains.bsp.bazel.base.AndroidSdkDownloader
import org.jetbrains.bsp.bazel.base.BazelBspTestBaseScenario
import org.jetbrains.bsp.bazel.base.BazelBspTestScenarioStep
import org.jetbrains.bsp.bazel.install.Install
import kotlin.time.Duration.Companion.seconds

object BazelBspAndroidProjectTest : BazelBspTestBaseScenario() {
  @JvmStatic
  fun main(args: Array<String>) = executeScenario()

  override fun installServer() {
    Install.main(
      arrayOf(
        "-d", workspaceDir,
        "-b", binary,
        "-t", "//...",
        "--enabled-rules", "rules_android",
        "-f", "--action_env=ANDROID_HOME=${AndroidSdkDownloader.androidSdkPath}",
      )
    )
  }

  override fun scenarioSteps(): List<BazelBspTestScenarioStep> = listOf(
    downloadAndroidSdk(),
    compareWorkspaceTargetsResults(),
  )

  private fun downloadAndroidSdk() = BazelBspTestScenarioStep("Download Android SDK") {
    AndroidSdkDownloader.downloadAndroidSdkIfNeeded()
  }

  override fun expectedWorkspaceBuildTargetsResult(): WorkspaceBuildTargetsResult {
    val architecturePart = if (System.getProperty("os.arch") == "aarch64") "_aarch64" else ""
    val javaHome =
      "file://\$BAZEL_OUTPUT_BASE_PATH/external/rules_java~~toolchains~remotejdk17_\$OS${architecturePart}/"
    val jvmBuildTargetData = JvmBuildTarget().also {
      it.javaHome = javaHome
      it.javaVersion = "17"
    }

    val androidJar = "file://\$BAZEL_OUTPUT_BASE_PATH/external/androidsdk/platforms/android-34/android.jar"

    val appAndroidBuildTargetData = AndroidBuildTarget(
      androidJar = androidJar,
      androidTargetType = AndroidTargetType.APP,
      manifest = "file://\$WORKSPACE/src/main/AndroidManifest.xml",
      resourceFolders = emptyList(),
      jvmBuildTarget = jvmBuildTargetData,
    )

    val libAndroidBuildTargetData = AndroidBuildTarget(
      androidJar = androidJar,
      androidTargetType = AndroidTargetType.LIBRARY,
      manifest = "file://\$WORKSPACE/src/main/java/com/example/myapplication/AndroidManifest.xml",
      resourceFolders = listOf("file://\$WORKSPACE/src/main/java/com/example/myapplication/res/"),
      jvmBuildTarget = jvmBuildTargetData,
    )

    val appBuildTarget = BuildTarget(
      BuildTargetIdentifier("@@//src/main:app"),
      listOf("application"),
      listOf("android", "java"),
      listOf(BuildTargetIdentifier("@@//src/main/java/com/example/myapplication:lib")),
      BuildTargetCapabilities().apply {
        canCompile = true
        canDebug = true
        canRun = true
        canTest = false
      }
    )
    appBuildTarget.displayName = "@@//src/main:app"
    appBuildTarget.baseDirectory = "file://\$WORKSPACE/src/main/"
    appBuildTarget.data = appAndroidBuildTargetData
    appBuildTarget.dataKind = "android"

    val libBuildTarget = BuildTarget(
      BuildTargetIdentifier("@@//src/main/java/com/example/myapplication:lib"),
      listOf("library"),
      listOf("android", "java"),
      emptyList(),
      BuildTargetCapabilities().apply {
        canCompile = true
        canDebug = false
        canRun = false
        canTest = false
      }
    )
    libBuildTarget.displayName = "@@//src/main/java/com/example/myapplication:lib"
    libBuildTarget.baseDirectory = "file://\$WORKSPACE/src/main/java/com/example/myapplication/"
    libBuildTarget.data = libAndroidBuildTargetData
    libBuildTarget.dataKind = "android"

    return WorkspaceBuildTargetsResult(listOf(appBuildTarget, libBuildTarget))
  }

  private fun compareWorkspaceTargetsResults(): BazelBspTestScenarioStep = BazelBspTestScenarioStep(
    "workspace build targets"
  ) { testClient.testWorkspaceTargets(240.seconds, expectedWorkspaceBuildTargetsResult()) }

  // TODO: also compare target resources
}
