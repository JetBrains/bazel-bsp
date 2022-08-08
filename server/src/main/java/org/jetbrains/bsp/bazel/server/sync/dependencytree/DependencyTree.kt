package org.jetbrains.bsp.bazel.server.sync.dependencytree

import org.jetbrains.bsp.bazel.info.BspTargetInfo.Dependency
import org.jetbrains.bsp.bazel.info.BspTargetInfo.TargetInfo

class DependencyTree(
        private val rootTargets: Set<String> = emptySet(),
        private val idToTargetInfo: Map<String, TargetInfo> = emptyMap(),
) {
    private val idToDirectDependenciesIds: Map<String, Set<String>>
    private val idToLazyTransitiveDependencies: Map<String, Lazy<Set<TargetInfo>>>

    init {
        idToDirectDependenciesIds = idToTargetInfo.entries.associate { (id, target) ->
            Pair(
                    id,
                    getDependencies(target)
            )
        }
        idToLazyTransitiveDependencies = createIdToLazyTransitiveDependenciesMap(idToTargetInfo)
    }

    private fun createIdToLazyTransitiveDependenciesMap(
            idToTargetInfo: Map<String, TargetInfo>
    ): Map<String, Lazy<Set<TargetInfo>>> =
            idToTargetInfo.mapValues { (_, targetInfo) ->
                calculateLazyTransitiveDependenciesForTarget(targetInfo)
            }

    private fun calculateLazyTransitiveDependenciesForTarget(
            targetInfo: TargetInfo
    ): Lazy<Set<TargetInfo>> =
            lazy { calculateTransitiveDependenciesForTarget(targetInfo) }

    private fun calculateTransitiveDependenciesForTarget(
            targetInfo: TargetInfo
    ): Set<TargetInfo> {
        val dependencies = getDependencies(targetInfo)
        val strictlyTransitiveDependencies = calculateStrictlyTransitiveDependencies(dependencies)
        val directDependencies = idsToTargetInfo(dependencies)
        return strictlyTransitiveDependencies + directDependencies
    }

    private fun calculateStrictlyTransitiveDependencies(
            dependencies: Set<String>
    ): Set<TargetInfo> =
            dependencies.flatMap {
                idToLazyTransitiveDependencies[it]?.value.orEmpty()
            }.toSet()

    private fun idsToTargetInfo(dependencies: Set<String>): Set<TargetInfo> =
            dependencies.mapNotNull(idToTargetInfo::get).toSet()

    private fun directDependenciesIds(targetIds: Set<String>) =
            targetIds.flatMap {
                idToDirectDependenciesIds[it].orEmpty()
            }.toSet()

    fun allTargetsAtDepth(depth: Int, targets: Set<String>): Set<TargetInfo> =
            if (depth < 0)
                idsToTargetInfo(targets) + calculateStrictlyTransitiveDependencies(targets)
            else
                allTargetsAtDepth(depth, setOf(), targets)

    private tailrec fun allTargetsAtDepth(depth: Int, searched: Set<TargetInfo>, targets: Set<String>): Set<TargetInfo> =
            if (depth == 0)
                searched + idsToTargetInfo(targets)
            else
                allTargetsAtDepth(depth - 1, searched + idsToTargetInfo(targets), directDependenciesIds(targets))

    fun transitiveDependenciesWithoutRootTargets(targetId: String): Set<TargetInfo> =
            idToTargetInfo[targetId]?.let(::getDependencies).orEmpty()
                    .filter(::isNotARootTarget)
                    .flatMap(::collectTransitiveDependenciesAndAddTarget).toSet()

    private fun getDependencies(target: TargetInfo): Set<String> =
            target.dependenciesList.map(Dependency::getId).toSet()

    private fun isNotARootTarget(targetId: String): Boolean =
            !rootTargets.contains(targetId)

    private fun collectTransitiveDependenciesAndAddTarget(targetId: String): Set<TargetInfo> {
        val target = idToTargetInfo[targetId]?.let(::setOf).orEmpty()
        val dependencies = idToLazyTransitiveDependencies[targetId]?.let(::setOf).orEmpty()
                .map(Lazy<Set<TargetInfo>>::value).flatten().toSet()
        return dependencies + target
    }
}
