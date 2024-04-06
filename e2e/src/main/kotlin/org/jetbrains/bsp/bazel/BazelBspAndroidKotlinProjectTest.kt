package org.jetbrains.bsp.bazel

import ch.epfl.scala.bsp4j.BuildTarget
import ch.epfl.scala.bsp4j.BuildTargetCapabilities
import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import ch.epfl.scala.bsp4j.JvmBuildTarget
import ch.epfl.scala.bsp4j.WorkspaceBuildTargetsResult
import org.jetbrains.bsp.AndroidBuildTarget
import org.jetbrains.bsp.AndroidTargetType
import org.jetbrains.bsp.KotlinBuildTarget
import org.jetbrains.bsp.bazel.android.BazelBspAndroidProjectTestBase

object BazelBspAndroidKotlinProjectTest : BazelBspAndroidProjectTestBase() {
  @JvmStatic
  fun main(args: Array<String>) = executeScenario()

  override val enabledRules: List<String>
    get() = listOf("rules_android", "rules_kotlin")

  override fun expectedWorkspaceBuildTargetsResult(): WorkspaceBuildTargetsResult {
    val architecturePart = if (System.getProperty("os.arch") == "aarch64") "_aarch64" else ""
    val javaHome =
      "file://\$BAZEL_OUTPUT_BASE_PATH/external/rules_java~~toolchains~remotejdk17_\$OS${architecturePart}/"
    val jvmBuildTargetData = JvmBuildTarget().also {
      it.javaHome = javaHome
      it.javaVersion = "17"
    }

    val kotlinBuildTargetData = KotlinBuildTarget(
      languageVersion = "1.9",
      apiVersion = "1.9",
      kotlincOptions = listOf("-Xlambdas=class", "-Xsam-conversions=class", "-jvm-target=1.8"),
      associates = emptyList(),
      jvmBuildTarget = jvmBuildTargetData,
    )

    val androidJar = "file://\$BAZEL_OUTPUT_BASE_PATH/external/androidsdk/platforms/android-34/android.jar"

    val appAndroidBuildTargetData = AndroidBuildTarget(
      androidJar = androidJar,
      androidTargetType = AndroidTargetType.APP,
      manifest = "file://\$WORKSPACE/src/main/AndroidManifest.xml",
      resourceFolders = emptyList(),
      jvmBuildTarget = jvmBuildTargetData,
      kotlinBuildTarget = null,
    )

    val libAndroidBuildTargetData = AndroidBuildTarget(
      androidJar = androidJar,
      androidTargetType = AndroidTargetType.LIBRARY,
      manifest = "file://\$WORKSPACE/src/main/java/com/example/myapplication/AndroidManifest.xml",
      resourceFolders = listOf("file://\$WORKSPACE/src/main/java/com/example/myapplication/res/"),
      jvmBuildTarget = jvmBuildTargetData,
      kotlinBuildTarget = kotlinBuildTargetData,
    )

    val libTestAndroidBuildTargetData = AndroidBuildTarget(
      androidJar = androidJar,
      androidTargetType = AndroidTargetType.TEST,
      manifest = "file://\$WORKSPACE/src/test/java/com/example/myapplication/AndroidManifest.xml",
      resourceFolders = emptyList(),
      jvmBuildTarget = jvmBuildTargetData,
      kotlinBuildTarget = kotlinBuildTargetData,
    )

    val appBuildTarget = BuildTarget(
      BuildTargetIdentifier("@@//src/main:app"),
      listOf("application"),
      listOf("android", "java"),
      listOf(
        BuildTargetIdentifier("@@//src/main/java/com/example/myapplication:lib"),
        // TODO: ideally these non-existent dependencies should be filtered out somehow
        // See KotlinAndroidModulesMerger for more explanation
        BuildTargetIdentifier("@@//src/main/java/com/example/myapplication:lib_base"),
        BuildTargetIdentifier("@@//src/main/java/com/example/myapplication:lib_kt"),
      ),
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
      listOf("android", "java", "kotlin"),
      listOf(
        BuildTargetIdentifier("@@rules_jvm_external~~maven~maven//:androidx_appcompat_appcompat"),
        BuildTargetIdentifier("rules_kotlin_kotlin-stdlibs"),
      ),
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

    val libTestBuildTarget = BuildTarget(
      BuildTargetIdentifier("@@//src/test/java/com/example/myapplication:lib_test"),
      listOf("test"),
      listOf("android", "java", "kotlin"),
      listOf(
        BuildTargetIdentifier("@@//src/main/java/com/example/myapplication:lib"),
        BuildTargetIdentifier("@@//src/main/java/com/example/myapplication:lib_base"),
        BuildTargetIdentifier("@@//src/main/java/com/example/myapplication:lib_kt"),
        BuildTargetIdentifier("@@rules_jvm_external~~maven~maven//:junit_junit"),
        BuildTargetIdentifier("@@rules_jvm_external~~maven~maven//:org_robolectric_robolectric"),
        BuildTargetIdentifier("@@rules_robolectric~//bazel:android-all"),
        BuildTargetIdentifier("rules_kotlin_kotlin-stdlibs"),
      ),
      BuildTargetCapabilities().apply {
        canCompile = true
        canDebug = true
        canRun = false
        canTest = true
      }
    )
    libTestBuildTarget.displayName = "@@//src/test/java/com/example/myapplication:lib_test"
    libTestBuildTarget.baseDirectory = "file://\$WORKSPACE/src/test/java/com/example/myapplication/"
    libTestBuildTarget.data = libTestAndroidBuildTargetData
    libTestBuildTarget.dataKind = "android"

    return WorkspaceBuildTargetsResult(listOf(appBuildTarget, libBuildTarget, libTestBuildTarget))
  }
}
