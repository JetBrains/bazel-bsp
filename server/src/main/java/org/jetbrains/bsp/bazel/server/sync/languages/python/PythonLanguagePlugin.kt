package org.jetbrains.bsp.bazel.server.sync.languages.python

import com.jetbrains.bsp.bsp4kt.BuildTarget
import com.jetbrains.bsp.bsp4kt.BuildTargetDataKind
import com.jetbrains.bsp.bsp4kt.PythonBuildTarget
import com.jetbrains.bsp.bsp4kt.PythonOptionsItem
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.jetbrains.bsp.bazel.info.BspTargetInfo.FileLocation
import org.jetbrains.bsp.bazel.info.BspTargetInfo.PythonTargetInfo
import org.jetbrains.bsp.bazel.info.BspTargetInfo.TargetInfo
import org.jetbrains.bsp.bazel.server.sync.BazelPathsResolver
import org.jetbrains.bsp.bazel.server.sync.BspMappings
import org.jetbrains.bsp.bazel.server.sync.dependencytree.DependencyTree
import org.jetbrains.bsp.bazel.server.sync.languages.LanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.model.Module
import java.net.URI
import java.nio.file.Path
import kotlin.io.path.toPath

class PythonLanguagePlugin(
    private val bazelPathsResolver: BazelPathsResolver
) : LanguagePlugin<PythonModule>() {
    private var defaultInterpreter: URI? = null
    private var defaultVersion: String? = null

    override fun prepareSync(targets: Sequence<TargetInfo>) {
        val defaultTargetInfo = calculateDefaultTargetInfo(targets)
        defaultInterpreter = defaultTargetInfo?.interpreter
            ?.takeUnless { it.relativePath.isNullOrEmpty() }
            ?.let { bazelPathsResolver.resolveUri(it) }
        defaultVersion = defaultTargetInfo?.version
    }

    private fun calculateDefaultTargetInfo(targets: Sequence<TargetInfo>): PythonTargetInfo? =
        targets
            .filter(::hasPythonInterpreter)
            .firstOrNull()
            ?.pythonTargetInfo


    private fun hasPythonInterpreter(targetInfo: TargetInfo): Boolean =
        targetInfo.hasPythonTargetInfo() && targetInfo.pythonTargetInfo.hasInterpreter()

    override fun resolveModule(targetInfo: TargetInfo): PythonModule? =
        targetInfo.pythonTargetInfo?.let { pythonTargetInfo ->
            PythonModule(
                calculateInterpreterURI(interpreter = pythonTargetInfo.interpreter) ?: defaultInterpreter,
                pythonTargetInfo.version.takeUnless(String::isNullOrEmpty) ?: defaultVersion
            )
        }

    private fun calculateInterpreterURI(interpreter: FileLocation?): URI? =
        interpreter?.takeUnless { it.relativePath.isNullOrEmpty() }
            ?.let { bazelPathsResolver.resolveUri(it) }

    override fun applyModuleData(buildTarget: BuildTarget, moduleData: PythonModule): BuildTarget {
        val interpreter = moduleData.interpreter?.toString()
        val pythonBuildTarget = PythonBuildTarget(moduleData.version, interpreter)
        val data = Json.encodeToJsonElement(pythonBuildTarget)
        return buildTarget.copy(dataKind = BuildTargetDataKind.Python, data = data)
    }

    fun toPythonOptionsItem(module: Module, pythonModule: PythonModule): PythonOptionsItem =
        PythonOptionsItem(
            BspMappings.toBspId(module),
            emptyList(),
        )

    override fun dependencySources(targetInfo: TargetInfo, dependencyTree: DependencyTree): Set<URI> =
        if (targetInfo.hasPythonTargetInfo())
            dependencyTree.transitiveDependenciesWithoutRootTargets(targetInfo.id)
                .flatMap(::getExternalSources)
                .map(::calculateExternalSourcePath)
                .toSet()
        else
            emptySet()


    private fun getExternalSources(targetInfo: TargetInfo): List<FileLocation> =
        targetInfo.sourcesList.mapNotNull { it.takeIf { it.isExternal } }

    private fun calculateExternalSourcePath(externalSource: FileLocation): URI {
        val path = bazelPathsResolver.resolveUri(externalSource).toPath()
        return bazelPathsResolver.resolveUri(findSitePackagesSubdirectory(path) ?: path)
    }

    private tailrec fun findSitePackagesSubdirectory(path: Path?): Path? =
        when {
            path == null -> null
            path.endsWith("site-packages") -> path
            else -> findSitePackagesSubdirectory(path.parent)
        }
}
