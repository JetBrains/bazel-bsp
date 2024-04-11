package org.jetbrains.bsp.bazel.server.sync.languages.kotlin

import ch.epfl.scala.bsp4j.BuildTarget
import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import org.jetbrains.bsp.KotlinBuildTarget
import org.jetbrains.bsp.bazel.info.BspTargetInfo
import org.jetbrains.bsp.bazel.server.sync.dependencygraph.DependencyGraph
import org.jetbrains.bsp.bazel.server.sync.languages.LanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.languages.java.JavaLanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.model.Label
import java.net.URI
import java.nio.file.Path

class KotlinLanguagePlugin(
    private val javaLanguagePlugin: JavaLanguagePlugin
) : LanguagePlugin<KotlinModule>() {

  override fun applyModuleData(moduleData: KotlinModule, buildTarget: BuildTarget) {
    val kotlinBuildTarget = toKotlinBuildTarget(moduleData)
    buildTarget.dataKind = "kotlin"
    buildTarget.data = kotlinBuildTarget
  }

  fun toKotlinBuildTarget(kotlinModule: KotlinModule): KotlinBuildTarget {
    val kotlinBuildTarget = with(kotlinModule) {
      KotlinBuildTarget(
        languageVersion = languageVersion,
        apiVersion = apiVersion,
        associates = associates.map { BuildTargetIdentifier(it.value) },
        kotlincOptions = kotlincOptions
      )
    }
    kotlinModule.javaModule?.let { javaLanguagePlugin.toJvmBuildTarget(it) }?.let {
      kotlinBuildTarget.jvmBuildTarget = it
    }
    return kotlinBuildTarget
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

  override fun dependencySources(targetInfo: BspTargetInfo.TargetInfo, dependencyGraph: DependencyGraph): Set<URI> =
      javaLanguagePlugin.dependencySources(targetInfo, dependencyGraph)

  override fun calculateSourceRoot(source: Path): Path? =
      javaLanguagePlugin.calculateSourceRoot(source)
}