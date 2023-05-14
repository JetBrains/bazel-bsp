package org.jetbrains.bsp.bazel.server.sync.languages.rust

import ch.epfl.scala.bsp4j.RustDepKindInfo
import ch.epfl.scala.bsp4j.RustDependency
import ch.epfl.scala.bsp4j.RustEnvData
import ch.epfl.scala.bsp4j.RustFeature
import ch.epfl.scala.bsp4j.RustPackage
import ch.epfl.scala.bsp4j.RustProcMacroArtifact
import ch.epfl.scala.bsp4j.RustRawDependency
import ch.epfl.scala.bsp4j.RustTarget
import org.jetbrains.bsp.bazel.server.sync.BazelPathsResolver
import org.jetbrains.bsp.bazel.server.sync.model.Label
import org.jetbrains.bsp.bazel.server.sync.model.Module

private data class BazelPackageTargetInfo(
    val packageName: String,
    val targetName: String
)

private data class RustTargetModule(
    val module: Module,
    val rustModule: RustModule
)

class RustWorkspaceResolver(val bazelPathsResolver: BazelPathsResolver) {
    fun rustPackages(rustBspTargets: List<Module>): List<RustPackage> = rustBspTargets
        .groupBy { resolvePackage(it).packageName }
        .mapNotNull(::resolveSinglePackage)

    // We need to resolve all dependencies are provide a list of new bazel targets (deps) to be transformed into packages.
    fun rustDependencies(
        rustPackages: List<RustPackage>,
        rustBspTargets: List<Module>
    ): Pair<List<RustDependency>, List<RustRawDependency>> {

        val associatedBspTargets = groupBspTargetsByPackage(rustBspTargets, rustPackages)
        val associatedRawBspTargets = groupBspRawTargetsByPackage(rustBspTargets, rustPackages)

        val rustDependencies = resolveDependencies(associatedBspTargets)
        val rustRawDependencies = resolveRawDependencies(associatedRawBspTargets)

        return Pair(rustDependencies, rustRawDependencies)
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

    private fun serveTargetWithRustData(rustTargets: List<Module>): List<RustTargetModule> =
        rustTargets.mapNotNull {
            RustTargetModule(it, it.languageData as? RustModule ?: return@mapNotNull null)
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

    private fun groupBspTargetsByPackage(
        rustBspTargets: List<Module>,
        rustPackages: List<RustPackage>
    ): Map<RustPackage, List<Module>> {
        val rustBspTargetsMappedToLabel = rustBspTargets.associateBy { it.label.value }
        return rustPackages.associateWith {
            it.targets.mapNotNull { pkgTarget ->
                rustBspTargetsMappedToLabel["${it.id}:${pkgTarget.name}"]
            }
        }
    }

    private fun groupBspRawTargetsByPackage(
        rustBspTargets: List<Module>,
        rustPackages: List<RustPackage>
    ): Map<RustPackage, List<Module>> = rustPackages.associateWith { pkg ->
        rustBspTargets.filter { resolvePackage(it).packageName == pkg.id }
    }

    private fun resolveDependencies(associatedBspTargets: Map<RustPackage, List<Module>>): List<RustDependency> =
        associatedBspTargets
            .flatMap { (rustPackage, bspTargets) ->
                bspTargets.flatMap { bspTarget ->
                    resolveBspDependencies(rustPackage, bspTarget.directDependencies)
                }
            }
            .filter { it.source != it.target }

    private fun resolveBspDependencies(
        rustPackage: RustPackage,
        directDependencies: List<Label>
    ): List<RustDependency> =
        directDependencies
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

    private fun resolveRawDependencies(associatedRawBspTargets: Map<RustPackage, List<Module>>): List<RustRawDependency> =
        associatedRawBspTargets.flatMap { (rustPackage, bspTargets) ->
            bspTargets.flatMap { bspTarget ->
                resolveRawBspDependencies(rustPackage, bspTarget.directDependencies)
            }
        }

    private fun resolveRawBspDependencies(
        rustPackage: RustPackage,
        directDependencies: List<Label>
    ): List<RustRawDependency> =
        directDependencies.map {
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
