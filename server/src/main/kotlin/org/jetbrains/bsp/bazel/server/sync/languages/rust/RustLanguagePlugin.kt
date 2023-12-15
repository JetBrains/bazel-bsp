package org.jetbrains.bsp.bazel.server.sync.languages.rust

import ch.epfl.scala.bsp4j.BuildTarget
import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import ch.epfl.scala.bsp4j.RustWorkspaceResult
import org.jetbrains.bsp.bazel.info.BspTargetInfo
import org.jetbrains.bsp.bazel.info.BspTargetInfo.RustCrateInfo
import org.jetbrains.bsp.bazel.info.BspTargetInfo.TargetInfo
import org.jetbrains.bsp.bazel.server.sync.BazelPathsResolver
import org.jetbrains.bsp.bazel.server.sync.languages.LanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.model.Label
import org.jetbrains.bsp.bazel.server.sync.model.Language
import org.jetbrains.bsp.bazel.server.sync.model.Module

class RustLanguagePlugin(private val bazelPathsResolver: BazelPathsResolver) : LanguagePlugin<RustModule>() {

    private val rustPackageResolver = RustPackageResolver(bazelPathsResolver)
    private val rustDependencyResolver = RustDependencyResolver(rustPackageResolver)

    private fun TargetInfo.getRustCrateInfoOrNull(): RustCrateInfo? =
        this.takeIf(TargetInfo::hasRustCrateInfo)?.rustCrateInfo

    override fun applyModuleData(moduleData: RustModule, buildTarget: BuildTarget) {}

    override fun resolveModule(targetInfo: TargetInfo): RustModule? =
        targetInfo.getRustCrateInfoOrNull()?.let { rustCrateInfo ->
            val location = resolveTargetLocation(rustCrateInfo)
            val kind = if (targetInfo.kind == "rust_test") {
                "test"
            } else {
                rustCrateInfo.kind
            }
            val crateRoot = resolveTargetCrateRoot(rustCrateInfo, location)

            RustModule(
                crateId = rustCrateInfo.crateId,
                location = location,
                fromWorkspace = rustCrateInfo.fromWorkspace,
                name = rustCrateInfo.name,
                kind = kind,
                edition = rustCrateInfo.edition,
                crateFeatures = rustCrateInfo.crateFeaturesList,
                dependenciesCrateIds = rustCrateInfo.dependenciesCrateIdsList,
                crateRoot = crateRoot,
                version = rustCrateInfo.version,
                procMacroArtifacts = rustCrateInfo.procMacroArtifactsList,
            )
        }

    private fun resolveTargetLocation(rustCrateInfo: RustCrateInfo): RustCrateLocation =
        when (rustCrateInfo.location) {
            BspTargetInfo.RustCrateLocation.WORKSPACE_DIR -> RustCrateLocation.WORKSPACE_DIR
            else -> RustCrateLocation.EXEC_ROOT
        }

    private fun resolveTargetCrateRoot(rustCrateInfo: RustCrateInfo, location: RustCrateLocation): String {
        val path = if (location == RustCrateLocation.WORKSPACE_DIR) {
            bazelPathsResolver.relativePathToWorkspaceAbsolute(rustCrateInfo.crateRoot)
        } else {
            bazelPathsResolver.relativePathToExecRootAbsolute(rustCrateInfo.crateRoot)
        }

        return bazelPathsResolver.resolveUri(path).toString()
    }

    fun toRustWorkspaceResult(requestTargets: List<Module>, allTargets: List<Module>): RustWorkspaceResult {
        val modules = findAllRelatedRustTargets(requestTargets, allTargets.associateBy { it.label })
        val packages = rustPackageResolver.rustPackages(modules)
        val (dependencies, rawDependencies) = rustDependencyResolver.rustDependencies(packages, modules)
        val resolvedTargets = packages.filter { it.origin == "WORKSPACE" }
                .flatMap { it.resolvedTargets.map { it2 -> it.id + ':' + it2.name }}
                .map { BuildTargetIdentifier(it) }

        return RustWorkspaceResult(
            packages,
            rawDependencies,
            dependencies,
            resolvedTargets
        )
    }

    /**
     * Find all targets that are needed to be resolved.
     * It includes all targets from `targets` and all their dependencies.
     *
     * Let's assume we have a following project:
     *           B    A
     *           |   / \
     *           C  D   E
     *           \ /    |
     *            F     G
     * We want to resolve all targets related to B. We only need B, C and F.
     *
     * There is no need to revisit already visited targets - we can use `visited` set for that.
     * */
    private tailrec fun findAllRelatedRustTargets(
        targets: List<Module>,
        allModules: Map<Label, Module>,
        visited: MutableSet<Label> = mutableSetOf(),
        result: MutableList<Module> = mutableListOf()
    ): List<Module> {
        if (targets.isEmpty()) return result

        visited.addAll(targets.map { it.label })
        result.addAll(targets)

        val newTargets = targets
            .asSequence()
            .flatMap { module ->
                filterVisitedRustDependencies(
                    module.directDependencies,
                    allModules,
                    visited
                ).asSequence()
            }
            .toList()

        return findAllRelatedRustTargets(newTargets, allModules, visited, result)
    }

    private fun filterVisitedRustDependencies(
        dependencies: List<Label>,
        allModules: Map<Label, Module>,
        visited: MutableSet<Label>
    ): List<Module> = 
        dependencies
            .mapNotNull { allModules[it] }
            .filter { it.label !in visited }
            .filter { Language.RUST in it.languages }
}
