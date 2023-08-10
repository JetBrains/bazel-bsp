package org.jetbrains.bsp.bazel.server.sync.languages

import com.jetbrains.bsp.bsp4kt.BuildTarget

class EmptyLanguagePlugin : LanguagePlugin<LanguageData>() {
    override fun applyModuleData(buildTarget: BuildTarget, moduleData: LanguageData): BuildTarget {
        return buildTarget
    }
}
