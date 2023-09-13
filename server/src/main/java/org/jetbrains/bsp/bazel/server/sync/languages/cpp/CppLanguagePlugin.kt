package org.jetbrains.bsp.bazel.server.sync.languages.cpp

import ch.epfl.scala.bsp4j.BuildTarget
import ch.epfl.scala.bsp4j.BuildTargetDataKind
import ch.epfl.scala.bsp4j.CppBuildTarget
import ch.epfl.scala.bsp4j.CppOptionsItem
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

    override fun applyModuleData(moduleData: CppModule, buildTarget: BuildTarget) {
        // TODO https://youtrack.jetbrains.com/issue/BAZEL-612
        val cppBuildTarget = CppBuildTarget().also { it.version = null; it.compiler = "compiler"; it.cCompiler = "/bin/gcc"; it.cppCompiler= "/bin/gcc" }
        buildTarget.data = cppBuildTarget
        buildTarget.dataKind = BuildTargetDataKind.CPP
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
