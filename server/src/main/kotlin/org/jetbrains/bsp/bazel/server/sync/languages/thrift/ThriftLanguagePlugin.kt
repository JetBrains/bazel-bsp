package org.jetbrains.bsp.bazel.server.sync.languages.thrift

import ch.epfl.scala.bsp4j.BuildTarget
import org.jetbrains.bsp.bazel.info.BspTargetInfo
import org.jetbrains.bsp.bazel.server.paths.BazelPathsResolver
import org.jetbrains.bsp.bazel.server.dependencygraph.DependencyGraph
import org.jetbrains.bsp.bazel.server.model.Label
import org.jetbrains.bsp.bazel.server.sync.languages.LanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.languages.jvm.SourceRootGuesser
import java.net.URI
import java.nio.file.Path

class ThriftLanguagePlugin(private val bazelPathsResolver: BazelPathsResolver) :
    LanguagePlugin<ThriftModule>() {

    override fun dependencySources(
        targetInfo: BspTargetInfo.TargetInfo,
        dependencyGraph: DependencyGraph
    ): Set<URI> {
        val transitiveSourceDeps = dependencyGraph.transitiveDependenciesWithoutRootTargets(Label.parse(targetInfo.id))
            .filter(::isThriftLibrary)
            .flatMap(BspTargetInfo.TargetInfo::getSourcesList)
            .map(bazelPathsResolver::resolveUri)
            .toHashSet()

        val directSourceDeps = sourcesFromJvmTargetInfo(targetInfo)

        return transitiveSourceDeps + directSourceDeps
    }

    private fun sourcesFromJvmTargetInfo(targetInfo: BspTargetInfo.TargetInfo): HashSet<URI> =
        if (targetInfo.hasJvmTargetInfo()) {
            targetInfo
                .jvmTargetInfo
                .jarsList
                .flatMap { it.sourceJarsList }
                .map(bazelPathsResolver::resolveUri)
                .toHashSet()
        } else {
            HashSet()
        }


    private fun isThriftLibrary(target: BspTargetInfo.TargetInfo): Boolean =
        target.kind == THRIFT_LIBRARY_RULE_NAME

    protected override fun applyModuleData(moduleData: ThriftModule, buildTarget: BuildTarget) {}

    override fun calculateSourceRoot(source: Path): Path =
        SourceRootGuesser.getSourcesRoot(source)

    companion object {
        private const val THRIFT_LIBRARY_RULE_NAME = "thrift_library"
    }
}
