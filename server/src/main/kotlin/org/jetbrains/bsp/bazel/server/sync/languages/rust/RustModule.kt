package org.jetbrains.bsp.bazel.server.sync.languages.rust

import org.jetbrains.bsp.bazel.server.model.LanguageData

enum class RustCrateLocation {
    WORKSPACE_DIR, EXEC_ROOT
}

data class RustModule(
    val crateId: String,
    val location: RustCrateLocation,
    val fromWorkspace: Boolean,
    val name: String,
    val kind: String,
    val edition: String,
    val crateFeatures: List<String>,
    val dependenciesCrateIds: List<String>,
    val crateRoot: String,
    val version: String,
    val procMacroArtifacts: List<String>,
    var isExternalModule: Boolean = false
) : LanguageData
