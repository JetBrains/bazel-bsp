package org.jetbrains.bsp.bazel.server.sync.languages

import ch.epfl.scala.bsp4j.BuildTarget
import org.jetbrains.bsp.bazel.info.BspTargetInfo
import org.jetbrains.bsp.bazel.server.dependencygraph.DependencyGraph
import org.jetbrains.bsp.bazel.server.model.LanguageData
import java.net.URI
import java.nio.file.Path

abstract class LanguagePlugin<T : LanguageData> {

    open fun calculateSourceRoot(source: Path): Path? = null
    open fun resolveAdditionalResources(targetInfo: BspTargetInfo.TargetInfo): Set<URI> = emptySet()
    open fun prepareSync(targets: Sequence<BspTargetInfo.TargetInfo>) {}
    open fun resolveModule(targetInfo: BspTargetInfo.TargetInfo): T? = null

    open fun dependencySources(
        targetInfo: BspTargetInfo.TargetInfo, dependencyGraph: DependencyGraph
    ): Set<URI> = emptySet()

    @Suppress("UNCHECKED_CAST")
    fun setModuleData(moduleData: LanguageData, buildTarget: BuildTarget) =
        applyModuleData(moduleData as T, buildTarget)

    protected abstract fun applyModuleData(moduleData: T, buildTarget: BuildTarget)
}
