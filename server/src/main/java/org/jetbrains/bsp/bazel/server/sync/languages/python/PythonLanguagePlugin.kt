package org.jetbrains.bsp.bazel.server.sync.languages.python

import ch.epfl.scala.bsp4j.BuildTarget
import ch.epfl.scala.bsp4j.BuildTargetDataKind
import ch.epfl.scala.bsp4j.PythonBuildTarget
import ch.epfl.scala.bsp4j.PythonOptionsItem
import org.jetbrains.bsp.bazel.info.BspTargetInfo.FileLocation
import org.jetbrains.bsp.bazel.info.BspTargetInfo.TargetInfo
import org.jetbrains.bsp.bazel.server.sync.BazelPathsResolver
import org.jetbrains.bsp.bazel.server.sync.BspMappings
import org.jetbrains.bsp.bazel.server.sync.dependencytree.DependencyTree
import org.jetbrains.bsp.bazel.server.sync.languages.LanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.model.Module
import java.net.URI
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.toPath

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

    override fun dependencySources(targetInfo: TargetInfo, dependencyTree: DependencyTree): Set<URI> {
        return targetInfo.pythonTargetInfo?.run {
            dependencyTree.transitiveDependenciesWithoutRootTargets(targetInfo.id)
                .flatMap(::getExternalSources)
                .map(bazelPathsResolver::resolveUri)
                .map {
                    fun findExternal(path: Path?): Path? {
                        return if (path == null || path.parent.endsWith("external"))
                            path
                        else
                            findExternal(path.parent)
                    }

                    val path = it.toPath()
                    bazelPathsResolver.resolveUri(findExternal(path) ?: path)
                }
                .toSet()
        }.orEmpty()
    }

    private fun getExternalSources(targetInfo: TargetInfo): List<FileLocation> {
        return targetInfo.sourcesList.mapNotNull { it.takeIf { it.isExternal } }
    }
}