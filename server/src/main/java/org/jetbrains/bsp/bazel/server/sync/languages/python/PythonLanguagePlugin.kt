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
import java.net.URI

class PythonLanguagePlugin(
    private val bazelPathsResolver: BazelPathsResolver
) : LanguagePlugin<PythonModule>() {

    override fun resolveModule(targetInfo: TargetInfo): PythonModule? =
        targetInfo.pythonTargetInfo?.run {

            val interpreterURI = interpreter?.let {
                it.takeUnless { it.relativePath.isNullOrEmpty() }
                ?.let { bazelPathsResolver.resolveUri(it) }
            }
            PythonModule(
                interpreterURI,
                version.takeUnless(String::isNullOrEmpty)
            )

        }


    override fun applyModuleData(moduleData: PythonModule, buildTarget: BuildTarget) {
        buildTarget.dataKind = BuildTargetDataKind.PYTHON
        val interpreter = moduleData.interpreter?.let { it.toString() }
        buildTarget.data = PythonBuildTarget(moduleData.version, interpreter)
    }

    fun toPythonOptionsItem(module: Module, pythonModule: PythonModule): PythonOptionsItem =
        PythonOptionsItem(
            BspMappings.toBspId(module),
            emptyList(),
        )
}