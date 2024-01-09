package org.jetbrains.bsp.bazel.server.sync.languages.go

import ch.epfl.scala.bsp4j.BuildTarget
import org.jetbrains.bsp.GoBuildTarget
import org.jetbrains.bsp.bazel.info.BspTargetInfo
import org.jetbrains.bsp.bazel.server.sync.BazelPathsResolver
import org.jetbrains.bsp.bazel.server.sync.languages.LanguagePlugin

class GoLanguagePlugin(
  private val bazelPathsResolver: BazelPathsResolver
) : LanguagePlugin<GoModule>() {
  override fun applyModuleData(moduleData: GoModule, buildTarget: BuildTarget) {
    val goBuildTarget = with(moduleData) {
      GoBuildTarget(
        sdkHomePath = sdkHomePath,
        importPath = importPath,
      )
    }

    buildTarget.dataKind = "go"
    buildTarget.data = goBuildTarget
  }

  override fun resolveModule(targetInfo: BspTargetInfo.TargetInfo): GoModule? {
    if (!targetInfo.hasGoTargetInfo()) return null

    val goTargetInfo = targetInfo.goTargetInfo
    return GoModule(
      sdkHomePath = goTargetInfo.sdkHomePath,
      importPath = goTargetInfo.importpath,
    )
  }
}