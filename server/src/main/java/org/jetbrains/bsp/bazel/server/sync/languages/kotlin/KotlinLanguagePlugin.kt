package org.jetbrains.bsp.bazel.server.sync.languages.kotlin

import ch.epfl.scala.bsp4j.BuildTarget
import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import org.jetbrains.bsp.bazel.info.BspTargetInfo
import org.jetbrains.bsp.bazel.server.sync.BazelPathsResolver
import org.jetbrains.bsp.bazel.server.sync.dependencytree.DependencyTree
import org.jetbrains.bsp.bazel.server.sync.languages.LanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.languages.java.JavaLanguagePlugin
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
          associates = associates,
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
    var kotlincOptsRes: KotlincOpts? = null
    if (kotlinTargetInfo.hasKotlincOpts()) {
      val kotlincOpts = kotlinTargetInfo.kotlincOpts
      with(kotlincOpts) {
        kotlincOptsRes = KotlincOpts(
          includeStdlibs = if (hasIncludeStdlibs()) includeStdlibs else null,
          javaParameters = if (hasJavaParameters()) javaParameters else null,
          jvmTarget = if (hasJvmTarget()) jvmTarget else null,
          warn = if (hasWarn()) warn else null,
          xAllowResultReturnType = if (hasXAllowResultReturnType()) xAllowResultReturnType else null,
          xBackendThreads = if (hasXBackendThreads()) xBackendThreads else null,
          xEmitJvmTypeAnnotations = if (hasXEmitJvmTypeAnnotations()) xEmitJvmTypeAnnotations else null,
          xEnableIncrementalCompilation = if (hasXEnableIncrementalCompilation()) xEnableIncrementalCompilation else null,
          xExplicitApiMode = if (hasXExplicitApiMode()) xExplicitApiMode else null,
          xInlineClasses = if (hasXInlineClasses()) xInlineClasses else null,
          xJvmDefault = if (hasXJvmDefault()) xJvmDefault else null,
          xLambdas = if (hasXLambdas()) xLambdas else null,
          xMultiPlatform = if (hasXMultiPlatform()) xMultiPlatform else null,
          xNoCallAssertions = if (hasXNoCallAssertions()) xNoCallAssertions else null,
          xNoOptimize = if (hasXNoOptimize()) xNoOptimize else null,
          xNoOptimizedCallableReferences = if (hasXNoOptimizedCallableReferences()) xNoOptimizedCallableReferences else null,
          xNoParamAssertions = if (hasXNoParamAssertions()) xNoParamAssertions else null,
          xNoReceiverAssertions = if (hasXNoReceiverAssertions()) xNoReceiverAssertions else null,
          xOptinList = xOptinList.ifEmpty { null },
          xReportPerf = if (hasXReportPerf()) xReportPerf else null,
          xSamConversions = if (hasXSamConversions()) xSamConversions else null,
          xSkipPrereleaseCheck = if (hasXSkipPrereleaseCheck()) xSkipPrereleaseCheck else null,
          xUseFirLt = if (hasXUseFirLt()) xUseFirLt else null,
          xUseK2 = if (hasXUseK2()) xUseK2 else null
        )
      }
    }
    return KotlinModule(
        languageVersion = kotlinTargetInfo.languageVersion,
        apiVersion = kotlinTargetInfo.apiVersion,
        associates = kotlinTargetInfo.associatesList.map { BuildTargetIdentifier(it) },
        kotlincOptions = kotlincOptsRes,
        javaModule = javaLanguagePlugin.resolveModule(targetInfo)
    )
  }

  override fun dependencySources(targetInfo: BspTargetInfo.TargetInfo, dependencyTree: DependencyTree): Set<URI> =
      javaLanguagePlugin.dependencySources(targetInfo, dependencyTree)

  override fun calculateSourceRoot(source: Path): Path? =
      javaLanguagePlugin.calculateSourceRoot(source)
}