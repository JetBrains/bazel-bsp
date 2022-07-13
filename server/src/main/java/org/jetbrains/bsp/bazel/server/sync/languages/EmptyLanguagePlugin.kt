package org.jetbrains.bsp.bazel.server.sync.languages

import ch.epfl.scala.bsp4j.BuildTarget

class EmptyLanguagePlugin : LanguagePlugin<LanguageData>() {
    override fun applyModuleData(moduleData: LanguageData, buildTarget: BuildTarget) {}
}
