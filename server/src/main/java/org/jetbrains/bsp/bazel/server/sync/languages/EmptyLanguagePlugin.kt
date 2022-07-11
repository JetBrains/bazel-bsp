package org.jetbrains.bsp.bazel.server.sync.languages

import ch.epfl.scala.bsp4j.BuildTarget
import org.jetbrains.bsp.bazel.info.BspTargetInfo

class EmptyLanguagePlugin : LanguagePlugin<LanguageData>() {
    override fun resolveModule(targetInfo: BspTargetInfo.TargetInfo): LanguageData? = null

    override fun applyModuleData(moduleData: LanguageData, buildTarget: BuildTarget) {}
}
