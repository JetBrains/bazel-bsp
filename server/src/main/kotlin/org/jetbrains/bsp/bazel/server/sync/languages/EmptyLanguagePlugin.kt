package org.jetbrains.bsp.bazel.server.sync.languages

import ch.epfl.scala.bsp4j.BuildTarget
import org.jetbrains.bsp.bazel.server.model.LanguageData

class EmptyLanguagePlugin : LanguagePlugin<LanguageData>() {
    override fun applyModuleData(moduleData: LanguageData, buildTarget: BuildTarget) {}
}
