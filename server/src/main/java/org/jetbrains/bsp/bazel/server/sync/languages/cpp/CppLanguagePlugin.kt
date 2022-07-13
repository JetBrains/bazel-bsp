package org.jetbrains.bsp.bazel.server.sync.languages.cpp

import ch.epfl.scala.bsp4j.BuildTarget
import org.jetbrains.bsp.bazel.server.sync.languages.LanguagePlugin

// TODO implement
class CppLanguagePlugin : LanguagePlugin<CppModule>() {
    override fun applyModuleData(moduleData: CppModule, buildTarget: BuildTarget) {}
}
