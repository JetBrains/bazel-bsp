package org.jetbrains.bsp.bazel.server.sync

import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest
import java.util.concurrent.CompletableFuture

data class LibraryItem(
        val id: BuildTargetIdentifier,
        val dependencies: List<BuildTargetIdentifier>,
        val jars: List<String>,
        val sourceJars: List<String>,
)

data class WorkspaceLibrariesResult(
        val libraries: List<LibraryItem>
)

data class DirectoryItem(
  val uri: String,
)

data class WorkspaceDirectoriesResult(
  val includedDirectories: List<DirectoryItem>,
  val excludedDirectories: List<DirectoryItem>,
)

interface BazelBuildServer {
    @JsonRequest("workspace/libraries")
    fun workspaceLibraries(): CompletableFuture<WorkspaceLibrariesResult>

    @JsonRequest("workspace/directories")
    fun workspaceDirectories(): CompletableFuture<WorkspaceDirectoriesResult>
}
