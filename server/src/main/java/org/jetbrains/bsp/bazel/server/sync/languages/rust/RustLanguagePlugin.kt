package org.jetbrains.bsp.bazel.server.sync.languages.rust

import ch.epfl.scala.bsp4j.BuildTarget
import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import ch.epfl.scala.bsp4j.RustWorkspaceResult
import ch.epfl.scala.bsp4j.RustPackage
import ch.epfl.scala.bsp4j.RustRawDependency
import ch.epfl.scala.bsp4j.RustDependency
import ch.epfl.scala.bsp4j.RustDepKindInfo
import ch.epfl.scala.bsp4j.RustTarget
import ch.epfl.scala.bsp4j.RustFeature
import ch.epfl.scala.bsp4j.RustEnvData
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
                crateRoot = targetInfo.rustCrateInfo.crateRoot,
                version = targetInfo.rustCrateInfo.version
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

    private fun rustPackages(rustBspTargets: List<Module>): List<RustPackage> {
        for (target in rustBspTargets) {
            LOGGER.info("rust target:")
            LOGGER.info(target)

            require(Language.RUST in target.languages) { "The target is not a Rust target" }

            val uri = bazelPathsResolver.labelToDirectoryUri(target.label)
            LOGGER.info("uri: $uri")

            val relPath = bazelPathsResolver.workspaceRoot()
            LOGGER.info("relPath: $relPath")

            val packageTargetInfo = resolvePackage(target)
            LOGGER.info("packageTargetInfo: $packageTargetInfo")
        }
        val workspaceRoot = bazelPathsResolver.workspaceRoot()
        val packages = rustBspTargets
            .groupBy { resolvePackage(it).packageName }
            .mapNotNull { (rustPackage, rustTargets) ->
                val rustTargetsWithData: List<Pair<Module, RustModule>> = rustTargets.mapNotNull {
                    if (it.languageData is RustModule) {
                        Pair(it, it.languageData)
                    } else {
                        null
                    }
                }
                    .groupBy { (_, rustData) -> rustData.crateRoot }
                    .map { (_, targets) ->
                        mergeTargets(targets)
                    }

                // target have versions, but package no, as there is no such thing as package in Rust in Bazel
                val version = rustTargetsWithData.map { (_, rustData) -> rustData.version }.maxOf { it }
                val isFromWorkspace = rustTargetsWithData.any { (_, rustData) -> rustData.fromWorkspace }
                val origin = if (isFromWorkspace) {
                    "WORKSPACE"
                } else {
                    "DEPENDENCY"
                }
                val edition = rustTargetsWithData.map { (_, rustData) -> rustData.edition }.maxOf { it }
                val source = if (isFromWorkspace) {
                    null
                } else {
                    "registry+https://github.com/rust-lang/crates.io-index"
                }     // let's hope it is
                val targets = rustTargetsWithData.map { (genericData, rustData) ->
                    RustTarget(
                        resolvePackage(genericData).targetName,
                        "$workspaceRoot${rustData.crateRoot}",
                        genericData.tags.first().toString(),    // TODO: not so sure about that
                        rustData.edition,
                        false,                                  // TODO: check it somehow. I even know where to look for it :/  http://bazelbuild.github.io/rules_rust/rust_doc.html
                        rustData.crateFeatures
                    )
                }
                val allFeatures = rustTargetsWithData.flatMap { (_, rustData) ->
                    rustData.crateFeatures
                }
                RustPackage(
                    BuildTargetIdentifier(rustPackage),         // id. TODO: it shouldn't be a BuildTargetIdentifier!!
                    version,
                    origin,
                    edition,
                    source,
                    targets,
                    allFeatures.map { RustFeature(it, listOf()) },
                    allFeatures,
                    null,
                    listOf(
                        RustEnvData("CARGO_MANIFEST_DIF", "$workspaceRoot${rustPackage.drop(1)}"),
                        RustEnvData("CARGO", "cargo"),
                        RustEnvData("CARGO_PKG_VERSION", "0.0.0"),
                        RustEnvData("CARGO_PKG_VERSION_MAJOR", "0"),
                        RustEnvData("CARGO_PKG_VERSION_MINOR", "0"),
                        RustEnvData("CARGO_PKG_VERSION_PATCH", "0"),
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
                    null
                )
            }
        LOGGER.info("packages:")
        for (rustPackage in packages) {
            LOGGER.info(rustPackage)
        }

        return packages
    }

    // We need to resolve all dependencies are provide a list of new bazel targets (deps) to be transformed into packages.
    private fun rustDependencies(rustPackages: List<RustPackage>, rustBspTargets: List<Module>): Pair<List<BuildTargetIdentifier>, Pair<List<RustDependency>, List<RustRawDependency>>> {

        // TODO: I guess it can be made faster #PoC
        val associatedBspTargets = rustPackages.associateWith {
            it.targets.mapNotNull { pkgTarget ->
                rustBspTargets.find {
                    bspTarget -> "${it.id.uri}:${pkgTarget.name}" == bspTarget.label.value
                }
            }
        }

        val associatedRawBspTargets = rustPackages.associateWith { pkg ->
            rustBspTargets.filter { resolvePackage(it).packageName == pkg.id.uri }
        }

        val rustDependenciesLabels = associatedBspTargets
            .values
            .toList()
            .flatten()
            .flatMap { it.directDependencies.map { label -> label.value } }
            .distinct()
            .map { BuildTargetIdentifier(it) }

        val rustDependencies = associatedBspTargets
            .flatMap { (rustPackage, bspTargets) ->
                bspTargets.flatMap { bspTarget ->
                    bspTarget.directDependencies
                        .map { label -> resolvePackage(label) }
                        .map { RustDependency(
                            rustPackage.id.uri,
                            it.packageName,
                            it.targetName,
                            listOf(
                                RustDepKindInfo("normal", null)
                            )
                        ) }
                }
            }
            .filter { it.source != it.target }

        val rustRawDependencies = associatedRawBspTargets
            .flatMap { (rustPackage, bspTargets) ->
                bspTargets.flatMap { bspTarget ->
                    bspTarget.directDependencies
                        .map {
                            RustRawDependency(
                                rustPackage.id.uri,
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

        LOGGER.info("rustDependencies")
        LOGGER.info(rustDependencies)

        return Pair(rustDependenciesLabels, Pair(rustDependencies, rustRawDependencies))
    }

    fun toRustWorkspaceResult(modules: List<Module>): RustWorkspaceResult {

        val packages = rustPackages(modules)
        val (transitiveBuildTargetsToResolve, dependenciesPair) = rustDependencies(packages, modules)
        val (dependencies, rawDependencies) = dependenciesPair

        // TODO: we need to package targets in `transitiveBuildTargetsToResolve`
        //       even local dependencies are not resolved currently. Only one
        //       level of BFS is passed. e.g.
        //       Let's say we have a tree:
        //                foo
        //                 |
        //                bar
        //                 |
        //                zonk
        //      If `modules` is a singleton of `foo` only foo will be resolved.
        //      `bar` will be a `transitiveBuildTargetsToResolve` and `zonk` will be ignored.

        LOGGER.info("=================================================================================")

        for (module in modules) {
            LOGGER.info("module info: ")
            LOGGER.info(module)
        }

        return RustWorkspaceResult(
                packages,
                rawDependencies,
                dependencies
        )
    }

    companion object {
        private val LOGGER = LogManager.getLogger(RustLanguagePlugin::class.java)
    }
}
