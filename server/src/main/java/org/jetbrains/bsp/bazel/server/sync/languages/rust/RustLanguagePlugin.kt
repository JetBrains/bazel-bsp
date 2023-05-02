package org.jetbrains.bsp.bazel.server.sync.languages.rust

import ch.epfl.scala.bsp4j.BuildTarget
import ch.epfl.scala.bsp4j.RustStdLib
import ch.epfl.scala.bsp4j.RustToolchain
import ch.epfl.scala.bsp4j.RustToolchainResult
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

    private val rustWorkspaceResolver = RustWorkspaceResolver(bazelPathsResolver)

    private fun TargetInfo.getRustCrateInfoOrNull(): RustCrateInfo? =
        this.takeIf(TargetInfo::hasRustCrateInfo)?.rustCrateInfo

    override fun applyModuleData(moduleData: RustModule, buildTarget: BuildTarget) {
        // TODO
    }

    override fun resolveModule(targetInfo: TargetInfo): RustModule? =
        targetInfo.getRustCrateInfoOrNull()?.let { rustCrateInfo ->
            val location = resolveTargetLocation(rustCrateInfo)
            val crateRoot = resolveTargetCrateRoot(rustCrateInfo, location)
            val dependencies = resolveTargetDependencies(rustCrateInfo)

            RustModule(
                crateId = rustCrateInfo.crateId,
                location = location,
                fromWorkspace = rustCrateInfo.fromWorkspace,
                name = rustCrateInfo.name,
                kind = rustCrateInfo.kind,
                edition = rustCrateInfo.edition,
                crateFeatures = rustCrateInfo.crateFeaturesList,
                dependencies = dependencies,
                crateRoot = crateRoot,
                version = rustCrateInfo.version,
                procMacroArtifacts = rustCrateInfo.procMacroArtifactsList,
                procMacroSrv = rustCrateInfo.procMacroSrv,
                rustcSysroot = rustCrateInfo.rustcSysroot,
                rustcSrcSysroot = rustCrateInfo.rustcSrcSysroot,
                cargoBinPath = rustCrateInfo.cargoBinPath,
                rustcVersion = rustCrateInfo.rustcVersion,
            )
        }

    private fun resolveTargetDependencies(rustCrateInfo: RustCrateInfo): List<RustDependency> =
        rustCrateInfo.dependenciesList.mapNotNull { depInfo ->
            RustDependency(
                crateId = depInfo.crateId,
                rename = depInfo.rename,
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
    private fun findAllRelatedRustTargets(
        targets: List<Module>,
        allModules: Map<Label, Module>,
        visited: MutableSet<Label> = mutableSetOf()
    ): List<Module> {
        visited.addAll(targets.map { it.label })
        return targets.flatMap { module ->
            listOf(module) + findAllRustTargetsRelatedToModule(module, allModules, visited)
        }
    }

    private fun findAllRustTargetsRelatedToModule(
        module: Module,
        allModules: Map<Label, Module>,
        visited: MutableSet<Label>,
    ): List<Module> =
        findAllRelatedRustTargets(
            filterVisitedRustDependencies(module.directDependencies, allModules, visited),
            allModules,
            visited
        )

    private fun filterVisitedRustDependencies(
        dependencies: List<Label>,
        allModules: Map<Label, Module>,
        visited: MutableSet<Label>
    ): List<Module> = 
        dependencies
            .mapNotNull { allModules[it] }
            .filter { it.label !in visited }
            .filter { Language.RUST in it.languages }

    fun toRustWorkspaceResult(requestTargets: List<Module>, allTargets: List<Module>): RustWorkspaceResult {
        val modules = findAllRelatedRustTargets(requestTargets, allTargets.associateBy { it.label })
        val packages = rustWorkspaceResolver.rustPackages(modules)
        val (dependencies, rawDependencies) = rustWorkspaceResolver.rustDependencies(packages, modules)

        return RustWorkspaceResult(
            packages,
            rawDependencies,
            dependencies
        )
    }

    fun toRustToolchains(requestTargets: List<Module>, allTargets: List<Module>): RustToolchainResult {
        val allRelatedTargets = findAllRelatedRustTargets(requestTargets, allTargets.associateBy { it.label })
        val toolchains = allRelatedTargets.mapNotNull { resolveRustToolchain(it) }

        return RustToolchainResult(toolchains)
    }

    private fun resolveRustToolchain(rustModule: Module): RustToolchain? {
        val rustData = rustModule.languageData as? RustModule ?: return null
        val stdLib = resolveRustStdLib(rustData)

        return RustToolchain(
            stdLib,
            prependWorkspacePath(rustData.cargoBinPath),
            prependWorkspacePath(rustData.procMacroSrv)
        )
    }

    private fun resolveRustStdLib(rustData: RustModule): RustStdLib? =
        if (rustData.rustcSysroot.isEmpty() || rustData.rustcSrcSysroot.isEmpty()) {
            null
        } else {
            RustStdLib(
                prependWorkspacePath(rustData.rustcSysroot),
                prependWorkspacePath(rustData.rustcSrcSysroot),
                rustData.rustcVersion
            )
        }

    private fun prependWorkspacePath(path: String): String =
        bazelPathsResolver.relativePathToExecRootAbsolute(path).toString()
}
