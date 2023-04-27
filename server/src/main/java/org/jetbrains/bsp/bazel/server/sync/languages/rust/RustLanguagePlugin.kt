package org.jetbrains.bsp.bazel.server.sync.languages.rust

import ch.epfl.scala.bsp4j.*
import ch.epfl.scala.bsp4j.RustDependency
import org.jetbrains.bsp.bazel.info.BspTargetInfo
import org.jetbrains.bsp.bazel.info.BspTargetInfo.TargetInfo
import org.jetbrains.bsp.bazel.server.sync.BazelPathsResolver
import org.jetbrains.bsp.bazel.server.sync.model.Module
import org.jetbrains.bsp.bazel.server.sync.languages.LanguagePlugin
import org.jetbrains.bsp.bazel.info.BspTargetInfo.RustCrateLocation as ProtoRustCrateLocation
import org.apache.logging.log4j.LogManager // TODO: remove
import org.jetbrains.bsp.bazel.server.sync.model.Label
import org.jetbrains.bsp.bazel.server.sync.model.Language

class RustLanguagePlugin(private val bazelPathsResolver: BazelPathsResolver) : LanguagePlugin<RustModule>() {
    override fun resolveModule(targetInfo: TargetInfo): RustModule? {
        return targetInfo.getRustCrateInfoOrNull()?.run {
            val location = if (targetInfo.rustCrateInfo.location == ProtoRustCrateLocation.WORKSPACE_DIR) {
                RustCrateLocation.WORKSPACE_DIR
            } else {
                RustCrateLocation.EXEC_ROOT
            }

            val crateRoot = {
                val path = if (location == RustCrateLocation.WORKSPACE_DIR) {
                    bazelPathsResolver.relativePathToWorkspaceAbsolute(targetInfo.rustCrateInfo.crateRoot)
                } else {
                    bazelPathsResolver.relativePathToExecRootAbsolute(targetInfo.rustCrateInfo.crateRoot)
                }

                bazelPathsResolver.resolveUri(path).toString()
            }

            RustModule(
                crateId = targetInfo.rustCrateInfo.crateId,
                location = location,
                fromWorkspace = targetInfo.rustCrateInfo.fromWorkspace,
                name = targetInfo.rustCrateInfo.name,
                kind = targetInfo.rustCrateInfo.kind,
                edition = targetInfo.rustCrateInfo.edition,
                crateFeatures = targetInfo.rustCrateInfo.crateFeaturesList,
                dependencies = targetInfo.rustCrateInfo.dependenciesList.mapNotNull {
                    RustDependency(
                        crateId = it.crateId,
                        rename = it.rename,
                    )
                },
                crateRoot = crateRoot(),
                version = targetInfo.rustCrateInfo.version,
                procMacroArtifacts = targetInfo.rustCrateInfo.procMacroArtifactsList,
                procMacroSrv = targetInfo.rustCrateInfo.procMacroSrv,
                rustcSysroot = targetInfo.rustCrateInfo.rustcSysroot,
                rustcSrcSysroot = targetInfo.rustCrateInfo.rustcSrcSysroot,
                cargoBinPath = targetInfo.rustCrateInfo.cargoBinPath,
                rustcVersion = targetInfo.rustCrateInfo.rustcVersion,
            )
        }
    }

    private fun TargetInfo.getRustCrateInfoOrNull(): BspTargetInfo.RustCrateInfo? =
        this.takeIf(TargetInfo::hasRustCrateInfo)?.rustCrateInfo

    override fun applyModuleData(moduleData: RustModule, buildTarget: BuildTarget) {
        // TODO
    }

    data class BazelPackageTargetInfo(val packageName: String, val targetName: String)

    private fun resolvePackage(rustTarget: Module): BazelPackageTargetInfo {
        return resolvePackage(rustTarget.label)
    }

    private fun resolvePackage(label: Label): BazelPackageTargetInfo {
        val labelVal = label.value
        val packageName = labelVal.substringBefore(":")
        val targetName = labelVal.substringAfter(":")
        return BazelPackageTargetInfo(packageName, targetName)
    }

    // We need to merge targets with the same crate root.
    private fun mergeTargets(rustTargets: List<Pair<Module, RustModule>>): Pair<Module, RustModule> {
        if (rustTargets.size == 1) {
            return rustTargets[0]
        }

        // TODO:
        // We need some heuristic here. As we need the proper label, lets take the target with
        //  the most features. It can be changes as a union of features set, but we need
        //  to give it a proper label.

        return rustTargets.maxByOrNull { (_, rustData) -> rustData.crateFeatures.size }!!
    }

    private fun prependWorkspacePath(path: String): String =
        bazelPathsResolver.relativePathToExecRootAbsolute(path).toString()

    private fun rustPackages(rustBspTargets: List<Module>): List<RustPackage> {
        for (target in rustBspTargets) {
            require(Language.RUST in target.languages) { "The target is not a Rust target" }
        }
        val packages = rustBspTargets
            .groupBy { resolvePackage(it).packageName }
            .mapNotNull { (rustPackage, rustTargets) ->
                val allRustTargetsWithData: List<Pair<Module, RustModule>> = rustTargets.mapNotNull {
                    if (it.languageData is RustModule) {
                        Pair(it, it.languageData)
                    } else {
                        null
                    }
                }
                // With removed `duplicates` for the same crate root
                val rustTargetsWithData: List<Pair<Module, RustModule>> =
                    allRustTargetsWithData.groupBy { (_, rustData) ->
                        rustData.crateRoot
                    }.map { (_, targets) ->
                        mergeTargets(targets)
                    }

                // target have versions, but package no, as there is no such thing as package in Rust in Bazel
                val version = rustTargetsWithData.map { (_, rustData) -> rustData.version }.maxOf { it }
                val (major, minor, patch) = version.split(".")
                val isFromWorkspace = rustTargetsWithData.any { (_, rustData) -> rustData.fromWorkspace }
                val origin = if (isFromWorkspace) {
                    "WORKSPACE"
                } else {
                    "DEP"
                }
                val edition = rustTargetsWithData.map { (_, rustData) -> rustData.edition }.maxOf { it }
                val source = if (isFromWorkspace) {
                    null
                } else {
                    "bazel+https://github.com/rust-lang/crates.io-index"
                }     // let's hope it is
                val pkgBaseDir = rustTargetsWithData.first().first.baseDirectory.toString()

                val mapTarget = { (genericData, rustData): Pair<Module, RustModule> ->
                    RustTarget(
                        resolvePackage(genericData).targetName,
                        rustData.crateRoot,
                        genericData.baseDirectory.toString(),
                        rustData.kind,
                        rustData.edition,
                        false,                                  // TODO: check it somehow. I even know where to look for it :/  http://bazelbuild.github.io/rules_rust/rust_doc.html
                        rustData.crateFeatures
                    )
                }

                val procMacroArtifacts = rustTargetsWithData
                    .flatMap { (_, rustData) -> rustData.procMacroArtifacts }
                    .map { bazelPathsResolver.pathToDirectoryUri(it) }
                    .map { RustProcMacroArtifact(it.path, "") }

                val procMacroArtifact = procMacroArtifacts.firstOrNull() // TODO: oh yeah, we need toolchain here...

                val targets = rustTargetsWithData.map(mapTarget)
                val allTargets = allRustTargetsWithData.map(mapTarget)
                val allFeatures = rustTargetsWithData.flatMap { (_, rustData) ->
                    rustData.crateFeatures
                }
                RustPackage(
                    rustPackage,
                    version,
                    origin,
                    edition,
                    source,
                    targets,
                    allTargets,
                    allFeatures.map { RustFeature(it, listOf()) },
                    allFeatures,
                    null,
                    listOf(
                        RustEnvData("CARGO_MANIFEST_DIF", "$pkgBaseDir${rustPackage.drop(1)}"),
                        RustEnvData("CARGO", "cargo"),
                        RustEnvData("CARGO_PKG_VERSION", version),
                        RustEnvData("CARGO_PKG_VERSION_MAJOR", major),
                        RustEnvData("CARGO_PKG_VERSION_MINOR", minor),
                        RustEnvData("CARGO_PKG_VERSION_PATCH", patch),
                        RustEnvData("CARGO_PKG_VERSION_PRE", ""),
                        RustEnvData("CARGO_PKG_AUTHORS", ""),
                        RustEnvData("CARGO_PKG_NAME", rustPackage),
                        RustEnvData("CARGO_PKG_DESCRIPTION", ""),
                        RustEnvData("CARGO_PKG_REPOSITORY", ""),
                        RustEnvData("CARGO_PKG_LICENSE", ""),
                        RustEnvData("CARGO_PKG_LICENSE_FILE", ""),
                        RustEnvData("CARGO_CRATE_NAME", rustPackage),
                    ),
                    null,
                    procMacroArtifact
                )
            }

        return packages
    }

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

    companion object {
        private val LOGGER = LogManager.getLogger(RustLanguagePlugin::class.java)
    }
}
