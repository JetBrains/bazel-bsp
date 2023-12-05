package org.jetbrains.bsp.bazel.server.sync.languages.kotlin

import ch.epfl.scala.bsp4j.BuildTarget
import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import org.jetbrains.bsp.bazel.info.BspTargetInfo
import org.jetbrains.bsp.bazel.server.sync.BazelPathsResolver
import org.jetbrains.bsp.bazel.server.sync.dependencytree.DependencyTree
import org.jetbrains.bsp.bazel.server.sync.languages.LanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.languages.java.JavaLanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.model.Label
import java.net.URI
import java.nio.file.Path

class KotlinLanguagePlugin(
    private val javaLanguagePlugin: JavaLanguagePlugin
) : LanguagePlugin<KotlinModule>() {

  override fun applyModuleData(moduleData: KotlinModule, buildTarget: BuildTarget) {
    val kotlinBuildTarget = with(moduleData) {
      KotlinBuildTarget(
          languageVersion = languageVersion,
          apiVersion = apiVersion,
          associates = associates.map { BuildTargetIdentifier(it.value) },
          kotlincOptions = kotlincOptions
      )
    }
    moduleData.javaModule?.let { javaLanguagePlugin.toJvmBuildTarget(it) }?.let {
      kotlinBuildTarget.jvmBuildTarget = it
    }

    buildTarget.dataKind = "kotlin"
    buildTarget.data = kotlinBuildTarget
  }

  override fun resolveModule(targetInfo: BspTargetInfo.TargetInfo): KotlinModule? {
    if (!targetInfo.hasKotlinTargetInfo()) return null

    val kotlinTargetInfo = targetInfo.kotlinTargetInfo

    return KotlinModule(
        languageVersion = kotlinTargetInfo.languageVersion,
        apiVersion = kotlinTargetInfo.apiVersion,
        associates = kotlinTargetInfo.associatesList.map { Label(it) },
        kotlincOptions = kotlinTargetInfo.kotlincOptsList,
        javaModule = javaLanguagePlugin.resolveModule(targetInfo)
    )
  }

  override fun dependencySources(targetInfo: BspTargetInfo.TargetInfo, dependencyTree: DependencyTree): Set<URI> =
      javaLanguagePlugin.dependencySources(targetInfo, dependencyTree)

  override fun calculateSourceRoot(source: Path): Path? =
      javaLanguagePlugin.calculateSourceRoot(source)
}