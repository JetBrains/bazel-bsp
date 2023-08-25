package org.jetbrains.bsp.bazel.server.sync

import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest
import java.util.concurrent.CompletableFuture

data class LibraryItem(
        val id: BuildTargetIdentifier,
        val dependencies: List<BuildTargetIdentifier>,
        val jars: List<String>
)

data class WorkspaceLibrariesResult(
        val libraries: List<LibraryItem>
)

interface BazelBuildServer {
    @JsonRequest("workspace/libraries")
    fun workspaceLibraries(): CompletableFuture<WorkspaceLibrariesResult>
}
