package org.jetbrains.bsp.bazel.server.sync.languages.cpp

import org.jetbrains.bsp.bazel.info.BspTargetInfo
import ch.epfl.scala.bsp4j.BuildTarget
import org.jetbrains.bsp.bazel.server.sync.languages.LanguagePlugin

// TODO implement
class CppLanguagePlugin : LanguagePlugin<CppModule>() {
    override fun resolveModule(targetInfo: BspTargetInfo.TargetInfo): CppModule? = null

    override fun applyModuleData(moduleData: CppModule, buildTarget: BuildTarget) {}
}
