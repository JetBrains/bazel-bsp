package org.jetbrains.bsp.bazel.server.sync.languages.rust

import ch.epfl.scala.bsp4j.BuildTarget
import ch.epfl.scala.bsp4j.RustDepKindInfo
import ch.epfl.scala.bsp4j.RustDependency
import ch.epfl.scala.bsp4j.RustEnvData
import ch.epfl.scala.bsp4j.RustFeature
import ch.epfl.scala.bsp4j.RustPackage
import ch.epfl.scala.bsp4j.RustProcMacroArtifact
import ch.epfl.scala.bsp4j.RustRawDependency
import ch.epfl.scala.bsp4j.RustStdLib
import ch.epfl.scala.bsp4j.RustTarget
import ch.epfl.scala.bsp4j.RustToolchain
import ch.epfl.scala.bsp4j.RustToolchainResult
import ch.epfl.scala.bsp4j.RustWorkspaceResult
import org.jetbrains.bsp.bazel.info.BspTargetInfo
import org.jetbrains.bsp.bazel.info.BspTargetInfo.RustCrateInfo
import org.jetbrains.bsp.bazel.info.BspTargetInfo.TargetInfo
import org.jetbrains.bsp.bazel.server.sync.BazelPathsResolver
import org.jetbrains.bsp.bazel.server.sync.model.Module
import org.jetbrains.bsp.bazel.server.sync.languages.LanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.model.Label
import org.jetbrains.bsp.bazel.server.sync.model.Language

class RustLanguagePlugin(private val bazelPathsResolver: BazelPathsResolver) : LanguagePlugin<RustModule>() {

    private fun resolveTargetLocation(rustCrateInfo: RustCrateInfo): RustCrateLocation =
        if (rustCrateInfo.location == BspTargetInfo.RustCrateLocation.WORKSPACE_DIR) {
            RustCrateLocation.WORKSPACE_DIR
        } else {
            RustCrateLocation.EXEC_ROOT
        }

    private fun resolveTargetCrateRoot(rustCrateInfo: RustCrateInfo, location: RustCrateLocation): String {
        val path = if (location == RustCrateLocation.WORKSPACE_DIR) {
            bazelPathsResolver.relativePathToWorkspaceAbsolute(rustCrateInfo.crateRoot)
        } else {
            bazelPathsResolver.relativePathToExecRootAbsolute(rustCrateInfo.crateRoot)
        }

        return bazelPathsResolver.resolveUri(path).toString()
    }

    override fun resolveModule(targetInfo: TargetInfo): RustModule? {
        return targetInfo.getRustCrateInfoOrNull()?.let { rustCrateInfo ->
            val location = resolveTargetLocation(rustCrateInfo)
            val crateRoot = resolveTargetCrateRoot(rustCrateInfo, location)

            RustModule(
                crateId = rustCrateInfo.crateId,
                location = location,
                fromWorkspace = rustCrateInfo.fromWorkspace,
                name = rustCrateInfo.name,
                kind = rustCrateInfo.kind,
                edition = rustCrateInfo.edition,
                crateFeatures = rustCrateInfo.crateFeaturesList,
                dependencies = rustCrateInfo.dependenciesList.mapNotNull { depInfo ->
                    RustDependency(
                        crateId = depInfo.crateId,
                        rename = depInfo.rename,
                    )
                },
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
    }

    private fun TargetInfo.getRustCrateInfoOrNull(): RustCrateInfo? =
        this.takeIf(TargetInfo::hasRustCrateInfo)?.rustCrateInfo

    override fun applyModuleData(moduleData: RustModule, buildTarget: BuildTarget) {
        // TODO
    }

    private fun resolvePackage(rustTarget: Module): BazelPackageTargetInfo =
        resolvePackage(rustTarget.label)

    private fun resolvePackage(label: Label): BazelPackageTargetInfo {
        val labelVal = label.value
        val packageName = labelVal.substringBefore(":")
        val targetName = labelVal.substringAfter(":")
        return BazelPackageTargetInfo(packageName, targetName)
    }

    // We need to merge targets with the same crate root.
    private fun mergeTargets(rustTargets: List<RustTargetModule>): RustTargetModule {
        require(rustTargets.isNotEmpty()) { "Cannot merge an empty list of targets." }
        if (rustTargets.size == 1) {
            return rustTargets.first()
        }

        // TODO:
        //  We need some heuristic here. As we need the proper label, lets take the target with
        //   the most features. It can be changes as a union of features set, but we need
        //   to give it a proper label.

        return rustTargets.maxByOrNull { (_, rustData) -> rustData.crateFeatures.size }!!
    }

    private fun prependWorkspacePath(path: String): String =
        bazelPathsResolver.relativePathToExecRootAbsolute(path).toString()

    private fun serveTargetWithRustData(rustTargets: List<Module>): List<RustTargetModule> =
        rustTargets.mapNotNull {
            if (it.languageData is RustModule) {
                Pair(it, it.languageData)
            } else {
                null
            }
        }

    private fun removeConflictingRustTargets(allRustTargets: List<RustTargetModule>): List<RustTargetModule> =
        allRustTargets.groupBy { (_, rustData) ->
            rustData.crateRoot
        }.map { (_, targets) ->
            mergeTargets(targets)
        }

    private fun splitVersion(version: String): Triple<String, String, String> = try {
        val (major, minor, patch) = version.split(".")
        Triple(major, minor, patch)
    } catch (_: Exception) {
        Triple(version, "", "")
    }

    private fun resolvePackageVersion(rustTargets: List<RustTargetModule>): String =
        rustTargets.map { (_, rustData) -> rustData.version }.maxOf { it }

    private fun isPackageFromWorkspace(rustTargets: List<RustTargetModule>): Boolean {
        val isFromWorkspace = rustTargets.any { (_, rustData) -> rustData.fromWorkspace }
        require(rustTargets.all { (_, rustData) -> rustData.fromWorkspace == isFromWorkspace }) {
            "All targets within a single package must have the same origin."
        }
        return isFromWorkspace
    }

    private fun resolvePackageOrigin(isFromWorkspace: Boolean): String = if (isFromWorkspace) {
        "WORKSPACE"
    } else {
        "DEP"
    }

    private fun resolvePackageEdition(rustTargets: List<RustTargetModule>): String =
        rustTargets.map { (_, rustData) -> rustData.edition }.maxOf { it }

    private fun resolvePackageSource(isFromWorkspace: Boolean): String? = if (isFromWorkspace) {
        null
    } else {
        // let's hope it is
        "bazel+https://github.com/rust-lang/crates.io-index"
    }

    private fun resolvePackageBaseDir(rustTargets: List<RustTargetModule>): String {
        val baseDirs = rustTargets.map { (pkg, _) -> pkg.baseDirectory.toString() }.distinct()
        require(baseDirs.size == 1) { "All targets within a single package must have the same base directory." }
        return baseDirs.first()
    }

    private fun parseSingleTarget(module: RustTargetModule): RustTarget {
        val (genericData, rustData) = module
        return RustTarget(
            resolvePackage(genericData).targetName,
            rustData.crateRoot,
            genericData.baseDirectory.toString(),
            rustData.kind,
            rustData.edition,
            false,                  // TODO: check it somehow. I even know where to look for it :/  http://bazelbuild.github.io/rules_rust/rust_doc.html
            rustData.crateFeatures
        )
    }

    private fun resolveProcMacroArtifact(rustTargets: List<RustTargetModule>): RustProcMacroArtifact? =
        rustTargets
            .flatMap { (_, rustData) -> rustData.procMacroArtifacts }
            .map { bazelPathsResolver.pathToDirectoryUri(it) }
            .map { RustProcMacroArtifact(it.path, "") }
            .firstOrNull()
    // TODO: ^^^^^ Cargo does not allow for more then one library in a single package so no more than one proc macro.

    private fun resolvePackageFeatures(
        rustTargets: List<RustTargetModule>
    ): Pair<List<String>, List<RustFeature>> {
        val allFeaturesAsStrings = rustTargets.flatMap { (_, rustData) ->
            rustData.crateFeatures
        }
        val allFeatures = allFeaturesAsStrings.map { RustFeature(it, listOf()) }
        return Pair(allFeaturesAsStrings, allFeatures)
    }

    private fun resolvePackageEnv(rustPackage: String, pkgBaseDir: String, version: String): List<RustEnvData> {
        val (major, minor, patch) = splitVersion(version)
        val envMap = mapOf(
            "CARGO_MANIFEST_DIR" to "$pkgBaseDir${rustPackage.drop(1)}",
            "CARGO" to "cargo",
            "CARGO_PKG_VERSION" to version,
            "CARGO_PKG_VERSION_MAJOR" to major,
            "CARGO_PKG_VERSION_MINOR" to minor,
            "CARGO_PKG_VERSION_PATCH" to patch,
            "CARGO_PKG_VERSION_PRE" to "",
            "CARGO_PKG_AUTHORS" to "",
            "CARGO_PKG_NAME" to rustPackage,
            "CARGO_PKG_DESCRIPTION" to "",
            "CARGO_PKG_REPOSITORY" to "",
            "CARGO_PKG_LICENSE" to "",
            "CARGO_PKG_LICENSE_FILE" to "",
            "CARGO_CRATE_NAME" to rustPackage,
        )
        return envMap.map { RustEnvData(it.key, it.value) }
    }

    private fun resolveSinglePackage(packageData: Map.Entry<String, List<Module>>): RustPackage {
        val (rustPackage, rustTargets) = packageData
        val allRustTargetsWithData = serveTargetWithRustData(rustTargets)
        // With removed `duplicates` for the same crate root
        val rustTargetsWithData = removeConflictingRustTargets(allRustTargetsWithData)

        // target have versions, but package no, as there is no such thing as package in Rust in Bazel
        val version = resolvePackageVersion(rustTargetsWithData)
        val isFromWorkspace = isPackageFromWorkspace(rustTargetsWithData)
        val origin = resolvePackageOrigin(isFromWorkspace)
        val edition = resolvePackageEdition(rustTargetsWithData)
        val source = resolvePackageSource(isFromWorkspace)
        val pkgBaseDir = resolvePackageBaseDir(rustTargetsWithData)
        val procMacroArtifact = resolveProcMacroArtifact(rustTargetsWithData)

        val targets = rustTargetsWithData.map(::parseSingleTarget)
        val allTargets = allRustTargetsWithData.map(::parseSingleTarget)
        val (allFeaturesAsString, allFeatures) = resolvePackageFeatures(rustTargetsWithData)
        val env = resolvePackageEnv(rustPackage, pkgBaseDir, version)

        return RustPackage(
            rustPackage,
            version,
            origin,
            edition,
            source,
            targets,
            allTargets,
            allFeatures,
            allFeaturesAsString,
            null,
            env,
            null,
            procMacroArtifact
        )
    }

    private fun rustPackages(rustBspTargets: List<Module>): List<RustPackage> = rustBspTargets
        .groupBy { resolvePackage(it).packageName }
        .mapNotNull(::resolveSinglePackage)

    // We need to resolve all dependencies are provide a list of new bazel targets (deps) to be transformed into packages.
    private fun rustDependencies(
        rustPackages: List<RustPackage>,
        rustBspTargets: List<Module>
    ): Pair<List<RustDependency>, List<RustRawDependency>> {

        val rustBspTargetsMapped = rustBspTargets.associateBy { it.label.value }
        val associatedBspTargets = rustPackages.associateWith {
            it.targets.mapNotNull { pkgTarget ->
                rustBspTargetsMapped["${it.id}:${pkgTarget.name}"]
            }
        }

        val associatedRawBspTargets = rustPackages.associateWith { pkg ->
            rustBspTargets.filter { resolvePackage(it).packageName == pkg.id }
        }

        val rustDependencies = associatedBspTargets
            .flatMap { (rustPackage, bspTargets) ->
                bspTargets.flatMap { bspTarget ->
                    bspTarget.directDependencies
                        .map { label -> resolvePackage(label) }
                        .map {
                            RustDependency(
                                rustPackage.id,
                                it.packageName,
                                it.targetName,
                                listOf(
                                    RustDepKindInfo("normal", null)
                                )
                            )
                        }
                }
            }
            .filter { it.source != it.target }

        val rustRawDependencies = associatedRawBspTargets
            .flatMap { (rustPackage, bspTargets) ->
                bspTargets.flatMap { bspTarget ->
                    bspTarget.directDependencies
                        .map {
                            RustRawDependency(
                                rustPackage.id,
                                it.value,
                                null,
                                null,
                                null,
                                false,
                                true,
                                listOf<String>()
                            )
                        }
                }
            }

        return Pair(rustDependencies, rustRawDependencies)
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
            listOf(module) +
                    findAllRelatedRustTargets(
                        module.directDependencies
                            .mapNotNull { allModules[it] }
                            .filter { it.label !in visited }
                            .filter { Language.RUST in it.languages },
                        allModules,
                        visited
                    )
        }
    }

    fun toRustWorkspaceResult(requestTargets: List<Module>, allTargets: List<Module>): RustWorkspaceResult {
        val modules = findAllRelatedRustTargets(requestTargets, allTargets.associateBy { it.label })
        val packages = rustPackages(modules)
        val (dependencies, rawDependencies) = rustDependencies(packages, modules)

        return RustWorkspaceResult(
            packages,
            rawDependencies,
            dependencies
        )
    }

    private fun resolveStdLib(rustModule: Module): RustToolchain? {
        if (rustModule.languageData !is RustModule) {
            return null;
        }

        val rustData = rustModule.languageData
        val stdLib = if (rustData.rustcSysroot.isEmpty() || rustData.rustcSrcSysroot.isEmpty()) {
            null
        } else {
            RustStdLib(
                prependWorkspacePath(rustData.rustcSysroot),
                prependWorkspacePath(rustData.rustcSrcSysroot),
                rustData.rustcVersion
            )
        }

        return RustToolchain(
            stdLib,
            prependWorkspacePath(rustData.cargoBinPath),
            prependWorkspacePath(rustData.procMacroSrv)
        )
    }

    fun toRustToolchains(requestTargets: List<Module>, allTargets: List<Module>): RustToolchainResult {
        val allRelatedTargets = findAllRelatedRustTargets(requestTargets, allTargets.associateBy { it.label })
        val toolchains = allRelatedTargets.mapNotNull { resolveStdLib(it) }

        return RustToolchainResult(toolchains)
    }
}

data class BazelPackageTargetInfo(val packageName: String, val targetName: String)
// TODO: add private
typealias RustTargetModule = Pair<Module, RustModule>
