package org.jetbrains.bsp.bazel.server.sync.languages.python

import ch.epfl.scala.bsp4j.BuildTarget
import ch.epfl.scala.bsp4j.BuildTargetDataKind
import ch.epfl.scala.bsp4j.PythonBuildTarget
import ch.epfl.scala.bsp4j.PythonOptionsItem
import org.jetbrains.bsp.bazel.info.BspTargetInfo.TargetInfo
import org.jetbrains.bsp.bazel.server.sync.BazelPathsResolver
import org.jetbrains.bsp.bazel.server.sync.BspMappings
import org.jetbrains.bsp.bazel.server.sync.languages.LanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.model.Module

class PythonLanguagePlugin(
    private val bazelPathsResolver: BazelPathsResolver
) : LanguagePlugin<PythonModule>() {

    override fun resolveModule(targetInfo: TargetInfo): PythonModule? =
        targetInfo?.pythonTargetInfo?.run {

            PythonModule(
                interpreter.takeUnless(String::isNullOrEmpty),
                version.takeUnless(String::isNullOrEmpty)
            )

        }


    override fun applyModuleData(moduleData: PythonModule, buildTarget: BuildTarget) {
        buildTarget.dataKind = BuildTargetDataKind.PYTHON
        buildTarget.data = PythonBuildTarget(moduleData.version, moduleData.interpreter)
    }

    fun toPythonOptionsItem(module: Module, pythonModule: PythonModule): PythonOptionsItem =
        PythonOptionsItem(
            BspMappings.toBspId(module),
            emptyList(),
        )
}