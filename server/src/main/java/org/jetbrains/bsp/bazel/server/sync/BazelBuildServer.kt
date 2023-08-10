package org.jetbrains.bsp.bazel.server.sync

import com.jetbrains.bsp.bsp4kt.BuildTargetIdentifier
import com.jetbrains.jsonrpc4kt.services.JsonRequest
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
