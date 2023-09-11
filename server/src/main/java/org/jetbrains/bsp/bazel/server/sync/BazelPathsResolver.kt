package org.jetbrains.bsp.bazel.server.sync

import org.jetbrains.bsp.bazel.bazelrunner.BazelInfo
import org.jetbrains.bsp.bazel.info.BspTargetInfo.FileLocation
import org.jetbrains.bsp.bazel.server.sync.model.Label
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.exists
import kotlin.io.path.toPath

class BazelPathsResolver(private val bazelInfo: BazelInfo) {
    private val uris = ConcurrentHashMap<Path, URI>()
    private val paths = ConcurrentHashMap<FileLocation, Path>()

    fun resolveUri(path: Path): URI = uris.computeIfAbsent(path, Path::toUri)

    fun workspaceRoot(): URI = resolveUri(bazelInfo.workspaceRoot.toAbsolutePath())

    fun resolveUris(fileLocations: List<FileLocation>, shouldFilterExisting: Boolean = false): List<URI> =
        fileLocations
            .map(::resolveUri)
            .filter { !shouldFilterExisting || it.toPath().exists() }

    fun resolvePaths(fileLocations: List<FileLocation>): List<Path> = fileLocations.map(::resolve)

    fun resolveUri(fileLocation: FileLocation): URI = resolveUri(resolve(fileLocation))

    fun resolve(fileLocation: FileLocation): Path = paths.computeIfAbsent(fileLocation, ::doResolve)

    private fun doResolve(fileLocation: FileLocation): Path = when {
        isAbsolute(fileLocation) -> resolveAbsolute(fileLocation)
        isMainWorkspaceSource(fileLocation) -> resolveSource(fileLocation)
        isInExternalWorkspace(fileLocation) -> resolveExternal(fileLocation)
        else -> resolveOutput(fileLocation)
    }

    private fun isAbsolute(fileLocation: FileLocation): Boolean {
        val relative = fileLocation.relativePath
        return relative.startsWith("/") && Files.exists(Paths.get(relative))
    }

    private fun resolveAbsolute(fileLocation: FileLocation): Path =
        Paths.get(fileLocation.relativePath)

    private fun resolveExternal(fileLocation: FileLocation): Path {
        val outputBaseRelativePath = Paths.get(fileLocation.rootExecutionPathFragment, fileLocation.relativePath)
        return resolveExternal(outputBaseRelativePath)
    }

    private fun resolveExternal(outputBaseRelativePath: Path): Path = bazelInfo
            .outputBase
            .resolve(outputBaseRelativePath)

    private fun resolveOutput(fileLocation: FileLocation): Path {
        val execRootRelativePath = Paths.get(fileLocation.rootExecutionPathFragment, fileLocation.relativePath)
        return resolveOutput(execRootRelativePath)
    }

    fun resolveOutput(execRootRelativePath: Path): Path = when {
        execRootRelativePath.startsWith("external") -> resolveExternal(execRootRelativePath)
        else -> Paths.get(bazelInfo.execRoot).resolve(execRootRelativePath)
    }

    private fun resolveSource(fileLocation: FileLocation): Path =
        bazelInfo.workspaceRoot.resolve(fileLocation.relativePath)

    private fun isMainWorkspaceSource(fileLocation: FileLocation): Boolean =
        fileLocation.isSource && !fileLocation.isExternal

    private fun isInExternalWorkspace(fileLocation: FileLocation): Boolean =
        fileLocation.rootExecutionPathFragment.startsWith("external/")

    fun labelToDirectoryUri(label: Label): URI {
        val relativePath = extractRelativePath(label.value)
        return resolveUri(bazelInfo.workspaceRoot.resolve(relativePath))
    }

    fun extractRelativePath(label: String): String {
        val prefix = bazelInfo.release.mainRepositoryReferencePrefix()
        require(label.startsWith(prefix)) {
            String.format(
                "%s didn't start with %s", label, prefix
            )
        }
        val labelWithoutPrefix = label.substring(prefix.length)
        val parts = labelWithoutPrefix.split(":".toRegex()).toTypedArray()
        require(parts.size == 2) { String.format("Label %s didn't contain exactly one ':'", label) }
        return parts[0]
    }
}
