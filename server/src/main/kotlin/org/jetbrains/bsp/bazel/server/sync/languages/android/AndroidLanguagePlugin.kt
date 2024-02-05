package org.jetbrains.bsp.bazel.server.sync.languages.android

import ch.epfl.scala.bsp4j.BuildTarget
import org.jetbrains.bsp.AndroidBuildTarget
import org.jetbrains.bsp.AndroidTargetType
import org.jetbrains.bsp.bazel.info.BspTargetInfo
import org.jetbrains.bsp.bazel.server.paths.BazelPathsResolver
import org.jetbrains.bsp.bazel.server.sync.dependencytree.DependencyTree
import org.jetbrains.bsp.bazel.server.sync.languages.LanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.languages.java.JavaLanguagePlugin
import java.net.URI
import java.nio.file.Path

class AndroidLanguagePlugin(
  private val javaLanguagePlugin: JavaLanguagePlugin,
  private val bazelPathsResolver: BazelPathsResolver,
) : LanguagePlugin<AndroidModule>() {
  override fun applyModuleData(moduleData: AndroidModule, buildTarget: BuildTarget) {
    val androidBuildTarget = with(moduleData) {
      AndroidBuildTarget(
        androidJar = androidJar,
        androidTargetType = androidTargetType,
      )
    }
    moduleData.javaModule?.let { javaLanguagePlugin.toJvmBuildTarget(it) }?.let {
      androidBuildTarget.jvmBuildTarget = it
    }

    buildTarget.dataKind = "android"
    buildTarget.data = androidBuildTarget
  }

  override fun resolveModule(targetInfo: BspTargetInfo.TargetInfo): AndroidModule? {
    if (!targetInfo.hasAndroidTargetInfo()) return null

    val androidTargetInfo = targetInfo.androidTargetInfo
    val androidJar = bazelPathsResolver.resolveUri(androidTargetInfo.androidJar)

    return AndroidModule(
      androidJar = androidJar,
      androidTargetType = getAndroidTargetType(targetInfo),
      javaModule = javaLanguagePlugin.resolveModule(targetInfo),
    )
  }

  private fun getAndroidTargetType(targetInfo: BspTargetInfo.TargetInfo): AndroidTargetType = when (targetInfo.kind) {
    "android_binary" -> AndroidTargetType.APP
    "android_library" -> AndroidTargetType.LIBRARY
    "android_test", "android_robolectric_test", "android_local_test", "android_instrumentation_test" ->
      AndroidTargetType.TEST
    else -> AndroidTargetType.LIBRARY
  }

  override fun dependencySources(targetInfo: BspTargetInfo.TargetInfo, dependencyTree: DependencyTree): Set<URI> =
    javaLanguagePlugin.dependencySources(targetInfo, dependencyTree)

  override fun calculateSourceRoot(source: Path): Path =
    javaLanguagePlugin.calculateSourceRoot(source)
}
