package org.jetbrains.bsp.bazel.server.sync.languages.rust

import org.jetbrains.bsp.bazel.server.sync.languages.LanguageData

enum class RustCrateLocation {
    WORKSPACE_DIR, EXEC_ROOT
}

data class RustDependency(
    val crateId: String,
    val rename: String,
)

data class RustModule(
    val crateId: String,
    val location: RustCrateLocation,
    val fromWorkspace: Boolean,
    val name: String,
    val kind: String,
    val edition: String,
    val crateFeatures: List<String>,
    val dependencies: List<RustDependency>,
    val crateRoot: String,
    val version: String,
    val procMacroArtifacts: List<String>,
    val procMacroSrv: String,
    val rustcSysroot: String,
    val rustcSrcSysroot: String,
    val cargoBinPath: String,
    val rustcVersion: String,
) : LanguageData

