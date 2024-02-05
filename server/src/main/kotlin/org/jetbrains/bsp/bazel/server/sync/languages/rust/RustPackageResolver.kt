package org.jetbrains.bsp.bazel.server.sync.languages.rust

import ch.epfl.scala.bsp4j.RustTarget
import ch.epfl.scala.bsp4j.RustCrateType
import ch.epfl.scala.bsp4j.RustPackage
import ch.epfl.scala.bsp4j.RustTargetKind
import org.jetbrains.bsp.bazel.server.paths.BazelPathsResolver
import org.jetbrains.bsp.bazel.server.sync.model.Label
import org.jetbrains.bsp.bazel.server.sync.model.Module

data class BazelPackageTargetInfo(
    val packageName: String,
    val targetName: String
)

class RustPackageResolver(val bazelPathsResolver: BazelPathsResolver) {
    private data class RustTargetModule(
        val module: Module,
        val rustModule: RustModule
    )

    private data class Version(
        val major: String,
        val minor: String = "",
        val patch: String = "",
        val preRelease: String? = ""
    )

    fun rustPackages(rustBspTargets: List<Module>): List<RustPackage> = rustBspTargets
        .groupBy { resolvePackage(it).packageName }
        .mapNotNull(::resolveSinglePackage)

    fun resolvePackage(rustTarget: Module): BazelPackageTargetInfo =
        resolvePackage(rustTarget.label)

    fun resolvePackage(label: Label): BazelPackageTargetInfo {
        val labelVal = label.value
        val (packageName, targetName) = labelVal.split(":", limit = 2)
        return BazelPackageTargetInfo(packageName, targetName)
    }

    private fun resolveSinglePackage(
        packageData: Map.Entry<String, List<Module>>
    ): RustPackage {
        val (rustPackageId, rustTargets) = packageData
        val allRustTargetsWithData = serveTargetWithRustData(rustTargets)
        // With removed `duplicates` for the same crate root.
        val rustTargetsWithData = removeConflictingRustTargets(allRustTargetsWithData)

        // Targets have already resolved versions, but packages don't,
        // as there is no such thing as package in Rust in Bazel.
        val version = resolvePackageVersion(rustTargetsWithData)
        val isFromWorkspace = isPackageFromWorkspace(rustTargetsWithData)
        val origin = resolvePackageOrigin(isFromWorkspace)
        val edition = resolvePackageEdition(rustTargetsWithData)
        val source = resolvePackageSource(isFromWorkspace)
        val pkgBaseDir = resolvePackageBaseDir(rustTargetsWithData)
        val procMacroArtifact = resolveProcMacroArtifact(rustTargetsWithData)
        val rootUrl = resolvePackageRootUrl(rustTargetsWithData)

        val targets = rustTargetsWithData.map(::parseSingleTarget)
        val allTargets = allRustTargetsWithData.map(::parseSingleTarget)
        val allFeatures = resolvePackageFeatures(rustTargetsWithData)
        val env = resolvePackageEnv(rustPackageId, pkgBaseDir, version)

        val rustPackage = RustPackage(
            rustPackageId,
            rootUrl,
            rustPackageId,
            version,
            origin,
            edition,
            targets,
            allTargets,
            allFeatures,
            allFeatures.keys.toSet(),
        )
        rustPackage.source = source
        rustPackage.env = env
        rustPackage.procMacroArtifact = procMacroArtifact

        return rustPackage
    }

    private fun serveTargetWithRustData(rustTargets: List<Module>): List<RustTargetModule> =
        rustTargets.mapNotNull {
            RustTargetModule(it, it.languageData as? RustModule ?: return@mapNotNull null)
        }

    private fun removeConflictingRustTargets(
        allRustTargets: List<RustTargetModule>
    ): List<RustTargetModule> =
        allRustTargets.groupBy { (_, rustData) ->
            rustData.crateRoot
        }.map { (_, targets) ->
            mergeTargetsWithSameCrateRoot(targets)
        }

    private fun mergeTargetsWithSameCrateRoot(rustTargets: List<RustTargetModule>): RustTargetModule {
        require(rustTargets.isNotEmpty()) { "Cannot merge an empty list of targets." }

        //  We need some heuristic here. As we need the proper label, lets take the target with
        //  the most features. It can be changes as a union of features set, but we need
        //  to give it a proper label.
        return rustTargets.maxByOrNull { (_, rustData) -> rustData.crateFeatures.size }!!
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
        "DEPENDENCY"
    }

    private fun resolvePackageEdition(rustTargets: List<RustTargetModule>): String =
        rustTargets.map { (_, rustData) -> rustData.edition }.maxOf { it }

    private fun resolvePackageSource(isFromWorkspace: Boolean): String? = if (isFromWorkspace) {
        null
    } else {
        // We assume that the external packages are hosted on the crates.io package registry.
        "bazel+https://github.com/rust-lang/crates.io-index"
    }

    private fun resolvePackageBaseDir(rustTargets: List<RustTargetModule>): String {
        val baseDirs = rustTargets.map { (pkg, _) -> pkg.baseDirectory.toString() }.distinct()
        require(baseDirs.size == 1) {
            "All targets within a single package must have the same base directory."
        }
        return baseDirs.first()
    }

    private fun resolveProcMacroArtifact(rustTargets: List<RustTargetModule>): String? =
        // We take only the first procedural macro as Cargo does not allow
        // more than one library in a single package.
        rustTargets
            .flatMap { (_, rustData) -> rustData.procMacroArtifacts }
            .map { bazelPathsResolver.pathToDirectoryUri(it) }
            .map { it.path }
            .firstOrNull()

    private fun resolvePackageRootUrl(rustTargets: List<RustTargetModule>): String =
        rustTargets
            .map { (genericData, _) -> genericData.baseDirectory.toString() }
            .firstOrNull() ?: "Missing package root path"

    private fun parseSingleTarget(module: RustTargetModule): RustTarget {
        val (genericData, rustData) = module
        val buildTarget = RustTarget(
            resolvePackage(genericData).targetName,
            rustData.crateRoot,
            parseTargetKind(rustData.kind),
            rustData.edition,
            // We set `doctest` always to false, as in Bazel doctests
            // are separate targets (`rust_doc_test`) that don't have CrateInfo
            // and therefore are not send in RustWorkspaceResult.
            false,
        )
        buildTarget.requiredFeatures = rustData.crateFeatures.toSet()
        buildTarget.crateTypes = parseCrateTypes(rustData.kind)
        return buildTarget
    }

    private fun parseTargetKind(kind: String): RustTargetKind {
        return when (kind) {
            "bin" -> RustTargetKind.BIN
            "test" -> RustTargetKind.TEST
            "rlib" -> RustTargetKind.LIB
            "proc-macro" -> RustTargetKind.LIB
            else -> RustTargetKind.UNKNOWN
        }
    }

    private fun parseCrateTypes(kind: String): List<RustCrateType> {
        return when (kind) {
            "rlib" -> listOf(RustCrateType.RLIB)
            "proc-macro" -> listOf(RustCrateType.PROC_MACRO)
            else -> emptyList()
        }
    }

    private fun resolvePackageFeatures(
        rustTargets: List<RustTargetModule>
    ): Map<String, Set<String>> {
        val allFeaturesAsStrings = rustTargets.flatMap { (_, rustData) ->
            rustData.crateFeatures
        }
        return allFeaturesAsStrings.associate { Pair(it, setOf()) }
    }

    private fun resolvePackageEnv(
        rustPackage: String,
        pkgBaseDir: String,
        version: String
    ): Map<String, String> {
        val (major, minor, patch, preRelease) = splitVersion(version)
        return mapOf(
            "CARGO_MANIFEST_DIR" to "$pkgBaseDir${rustPackage.drop(1)}",
            "CARGO" to "cargo",
            "CARGO_PKG_VERSION" to version,
            "CARGO_PKG_VERSION_MAJOR" to major,
            "CARGO_PKG_VERSION_MINOR" to minor,
            "CARGO_PKG_VERSION_PATCH" to patch,
            "CARGO_PKG_VERSION_PRE" to (preRelease ?: ""),
            "CARGO_PKG_AUTHORS" to "",
            "CARGO_PKG_NAME" to rustPackage,
            "CARGO_PKG_DESCRIPTION" to "",
            "CARGO_PKG_REPOSITORY" to "",
            "CARGO_PKG_LICENSE" to "",
            "CARGO_PKG_LICENSE_FILE" to "",
            "CARGO_CRATE_NAME" to rustPackage,
        )
    }

    private fun splitVersion(version: String): Version = try {
        val (major, minor, rest) = version.split(".")
        val splitRest = rest.split("-")
        Version(major, minor, splitRest[0], splitRest.getOrElse(1) { null })
    } catch (_: Exception) {
        Version(version)
    }
}
