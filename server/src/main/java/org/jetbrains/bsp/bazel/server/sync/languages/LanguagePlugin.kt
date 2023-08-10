package org.jetbrains.bsp.bazel.server.sync.languages

import com.jetbrains.bsp.bsp4kt.BuildTarget
import org.jetbrains.bsp.bazel.info.BspTargetInfo
import org.jetbrains.bsp.bazel.server.sync.dependencytree.DependencyTree
import java.net.URI
import java.nio.file.Path

abstract class LanguagePlugin<T : LanguageData> {

    open fun calculateSourceRoot(source: Path): Path? = null
    open fun prepareSync(targets: Sequence<BspTargetInfo.TargetInfo>) {}
    open fun resolveModule(targetInfo: BspTargetInfo.TargetInfo): T? = null

    open fun dependencySources(
        targetInfo: BspTargetInfo.TargetInfo, dependencyTree: DependencyTree
    ): Set<URI> = emptySet()

    @Suppress("UNCHECKED_CAST")
    fun setModuleData(buildTarget: BuildTarget, moduleData: LanguageData?): BuildTarget =
        if (moduleData == null) buildTarget
        else applyModuleData(buildTarget, moduleData as T)

    protected abstract fun applyModuleData(buildTarget: BuildTarget, moduleData: T): BuildTarget
}
