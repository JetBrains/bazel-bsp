package org.jetbrains.bsp.bazel.server.sync.languages.cpp

import com.jetbrains.bsp.bsp4kt.BuildTarget
import com.jetbrains.bsp.bsp4kt.BuildTargetDataKind
import com.jetbrains.bsp.bsp4kt.CppBuildTarget
import com.jetbrains.bsp.bsp4kt.CppOptionsItem
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.jetbrains.bsp.bazel.info.BspTargetInfo
import org.jetbrains.bsp.bazel.info.BspTargetInfo.TargetInfo
import org.jetbrains.bsp.bazel.server.sync.BazelPathsResolver
import org.jetbrains.bsp.bazel.server.sync.BspMappings
import org.jetbrains.bsp.bazel.server.sync.dependencytree.DependencyTree
import org.jetbrains.bsp.bazel.server.sync.languages.LanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.model.Module
import java.net.URI

class CppLanguagePlugin(private val bazelPathsResolver: BazelPathsResolver) : LanguagePlugin<CppModule>() {

    override fun resolveModule(targetInfo: TargetInfo): CppModule? {
        return targetInfo.getCppTargetInfoOrNull()?.run {
            CppModule(
                copts = targetInfo.cppTargetInfo.coptsList ,
                defines = targetInfo.cppTargetInfo.definesList,
                linkOpts = targetInfo.cppTargetInfo.linkOptsList,
                linkShared = targetInfo.cppTargetInfo.linkShared
            )
        }
    }

    override fun applyModuleData(buildTarget: BuildTarget, moduleData: CppModule): BuildTarget {
        // TODO retrieve real information about cpp compiler
        val cppBuildTarget = CppBuildTarget(null, "compiler", "/bin/gcc", "/bin/gcc")
        val data = Json.encodeToJsonElement(cppBuildTarget)
        return buildTarget.copy(
            data = data,
            dataKind = BuildTargetDataKind.Cpp
        )

    }

    private fun TargetInfo.getCppTargetInfoOrNull(): BspTargetInfo.CppTargetInfo? =
        this.takeIf(TargetInfo::hasCppTargetInfo)?.cppTargetInfo

    override fun dependencySources(
        targetInfo: TargetInfo, dependencyTree: DependencyTree
    ): Set<URI> =
        targetInfo.getCppTargetInfoOrNull()?.run {
            dependencyTree.transitiveDependenciesWithoutRootTargets(targetInfo.id)
                .flatMap(TargetInfo::getSourcesList)
                .map(bazelPathsResolver::resolveUri)
                .toSet()
        }.orEmpty()

    fun toCppOptionsItem(module: Module, cppModule: CppModule): CppOptionsItem =
        CppOptionsItem(
            BspMappings.toBspId(module),
            cppModule.copts,
            cppModule.defines,
            cppModule.linkOpts
        )

}
