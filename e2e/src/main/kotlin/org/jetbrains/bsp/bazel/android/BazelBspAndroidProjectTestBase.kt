package org.jetbrains.bsp.bazel.android

import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import ch.epfl.scala.bsp4j.ResourcesItem
import ch.epfl.scala.bsp4j.ResourcesParams
import ch.epfl.scala.bsp4j.ResourcesResult
import ch.epfl.scala.bsp4j.WorkspaceBuildTargetsResult
import kotlinx.coroutines.future.await
import org.jetbrains.bsp.bazel.base.BazelBspTestBaseScenario
import org.jetbrains.bsp.bazel.base.BazelBspTestScenarioStep
import org.jetbrains.bsp.bazel.install.Install
import java.net.URI
import kotlin.io.path.exists
import kotlin.io.path.name
import kotlin.io.path.toPath
import kotlin.time.Duration.Companion.minutes

abstract class BazelBspAndroidProjectTestBase : BazelBspTestBaseScenario() {
  private val testClient = createBazelClient()

  protected abstract val enabledRules: List<String>

  override fun installServer() {
    Install.main(
      arrayOf(
        "-d", workspaceDir,
        "-b", bazelBinary,
        "-t", "//...",
        "--enabled-rules", *enabledRules.toTypedArray(),
        "-f", "--action_env=ANDROID_HOME=${AndroidSdkDownloader.androidSdkPath}",
      )
    )
  }

  override fun scenarioSteps(): List<BazelBspTestScenarioStep> = listOf(
    downloadAndroidSdk(),
    compareWorkspaceBuildTargets(),
    compareBuildTargetResources(),
    compareWorkspaceLibraries(),
  )

  private fun downloadAndroidSdk() = BazelBspTestScenarioStep("Download Android SDK") {
    AndroidSdkDownloader.downloadAndroidSdkIfNeeded()
  }

  private fun expectedBuildTargetResourcesResult(): ResourcesResult {
    val appResources = ResourcesItem(
      BuildTargetIdentifier("@@//src/main:app"),
      listOf("file://\$WORKSPACE/src/main/AndroidManifest.xml"),
    )

    val libResources = ResourcesItem(
      BuildTargetIdentifier("@@//src/main/java/com/example/myapplication:lib"),
      listOf(
        "file://\$WORKSPACE/src/main/java/com/example/myapplication/AndroidManifest.xml",
        "file://\$WORKSPACE/src/main/java/com/example/myapplication/res/",
      ),
    )

    val libTestResources = ResourcesItem(
      BuildTargetIdentifier("@@//src/test/java/com/example/myapplication:lib_test"),
      listOf("file://\$WORKSPACE/src/test/java/com/example/myapplication/AndroidManifest.xml"),
    )

    return ResourcesResult(listOf(appResources, libResources, libTestResources))
  }

  private fun compareWorkspaceBuildTargets(): BazelBspTestScenarioStep = BazelBspTestScenarioStep(
    "Compare workspace/buildTargets"
  ) {
    testClient.test(timeout = 5.minutes) { session, _ ->
      val result = session.server.workspaceBuildTargets().await()
      testClient.assertJsonEquals<WorkspaceBuildTargetsResult>(expectedWorkspaceBuildTargetsResult(), result)
    }
  }

  private fun compareBuildTargetResources(): BazelBspTestScenarioStep = BazelBspTestScenarioStep(
    "Compare buildTarget/resources"
  ) {
    testClient.test(timeout = 1.minutes) { session, _ ->
      val resourcesParams = ResourcesParams(expectedTargetIdentifiers())
      val result = session.server.buildTargetResources(resourcesParams).await()
      testClient.assertJsonEquals<ResourcesResult>(expectedBuildTargetResourcesResult(), result)
    }
  }

  private fun compareWorkspaceLibraries(): BazelBspTestScenarioStep = BazelBspTestScenarioStep(
    "Compare workspace/libraries"
  ) {
    testClient.test(timeout = 5.minutes) { session, _ ->
      // Make sure Bazel unpacks all the dependent AARs
      session.server.workspaceBuildAndGetBuildTargets().await()
      val result = session.server.workspaceLibraries().await()
      val appCompatLibrary = result.libraries.first { "androidx_appcompat_appcompat" in it.id.uri }

      val jars = appCompatLibrary.jars.toList().map { URI.create(it).toPath() }
      for (jar in jars) { require(jar.exists()) { "Jar $jar should exist" } }

      val expectedJarNames = setOf("classes_and_libs_merged.jar", "AndroidManifest.xml", "res", "R.txt")
      val jarNames = jars.map { it.name }.toSet()
      require(jarNames == expectedJarNames) { "$jarNames should be equal to $expectedJarNames" }
    }
  }
}
