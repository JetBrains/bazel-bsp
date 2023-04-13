package org.jetbrains.bsp.bazel.server.sync

import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import org.jetbrains.bsp.bazel.bazelrunner.BazelInfo
import org.jetbrains.bsp.bazel.server.sync.model.Label
import org.jetbrains.bsp.bazel.server.sync.model.Module
import org.jetbrains.bsp.bazel.server.sync.model.SourceSet
import org.jetbrains.bsp.bazel.server.sync.model.Tag
import org.jetbrains.bsp.bazel.workspacecontext.WorkspaceContext
import java.net.URI
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.Path
import kotlin.io.path.name
import kotlin.io.path.toPath
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.isSymbolicLink
import kotlin.io.path.readSymbolicLink

class IntelliJProjectTreeViewFix(
    private val bazelPathsResolver: BazelPathsResolver,
    private val bazelInfo: BazelInfo
) {

    fun createModules(
        workspaceRoot: URI,
        modules: Sequence<Module>,
        workspaceContext: WorkspaceContext
    ): Sequence<Module> {
        return if (isFullWorkspaceImport(workspaceContext, workspaceRoot)) {
            createWorkspaceRootModule(workspaceRoot, modules)
        } else {
            createMultipleModules(workspaceContext, workspaceRoot, modules)
        }
    }

    private fun isFullWorkspaceImport(workspaceContext: WorkspaceContext, workspaceRoot: URI): Boolean {
        return importTargetSpecs(workspaceContext).any { it.startsWith("//...") || it.startsWith("@//...") } ||
                workspaceContext.dotBazelBspDirPath.value.parent.toUri() == workspaceRoot
    }

    private fun createWorkspaceRootModule(
        workspaceRoot: URI, modules: Sequence<Module>
    ): Sequence<Module> {
        val excludeDirs = computeSymlinksToExclude(workspaceRoot)

        val existingRootDirectories = resolveExistingRootDirectories(modules)
        if (existingRootDirectories.contains(workspaceRoot)) {
            return modules.map {
                if (it.sourceSet.sourceRoots.contains(workspaceRoot)) {
                    it.copy(outputs = it.outputs + excludeDirs)
                } else it
            }
        }
        val rootModule = syntheticModule("bsp-workspace-root", workspaceRoot, excludeDirs)
        return modules + sequenceOf(rootModule)
    }

    private fun computeSymlinksToExclude(workspaceRoot: URI): Set<URI> {
        val stableSymlinkNames = setOf("bazel-out", "bazel-testlogs", "bazel-bin")
        val execRoot = Path(bazelInfo.execRoot)
        val execRootSymlinkNames = execRoot.name.let { name ->
            // newer bazel versions put workspace name as last component of exec root
            // still, the symlink to this directory is in form of bazel-<sanitized-workspace-name>
            // it seems to not accept '_' as a character, and it is replaced with '-'
            //
            // In older bazel versions, the exec root directory is named __main__ and I am using
            // less efficient heuristic: finding all bazel-* files that are symlinks that point to
            // exec root
            if (name != "__main__") {
                setOf("bazel-" + name.replace("[^A-Za-z0-9]".toRegex(), "-"))
            } else {
                workspaceRoot.toPath()
                    .listDirectoryEntries("bazel-*")
                    .filterNot { it.name in stableSymlinkNames }
                    .filter { it.isSymbolicLink() && it.readSymbolicLink() == execRoot }
                    .map { it.name }
            }
        }

        return (stableSymlinkNames + execRootSymlinkNames).map {
            workspaceRoot.toPath().resolve(it).toUri()
        }.toSet()
    }

    private fun createMultipleModules(
        workspaceContext: WorkspaceContext, workspaceRoot: URI, modules: Sequence<Module>
    ): Sequence<Module> {
        val existingRootDirectories = resolveExistingRootDirectories(modules)
        val expectedRootDirs = resolveExpectedRootDirs(workspaceContext, workspaceRoot)
        val workspaceRootPath = Paths.get(workspaceRoot)
        return modules + expectedRootDirs.mapNotNull { root: URI ->
            if (existingRootDirectories.contains(root)) {
                return@mapNotNull null
            }
            val relative = workspaceRootPath.relativize(Paths.get(root)).toString()
            val moduleName = "$relative-modules-root"
            syntheticModule(moduleName, root)
        }
    }

    private fun resolveExistingRootDirectories(modules: Sequence<Module>): Sequence<URI> =
        modules.map(Module::sourceSet).flatMap { it.sourceRoots }.distinct()

    private fun resolveExpectedRootDirs(
        workspaceContext: WorkspaceContext, workspaceRoot: URI
    ): Sequence<URI> {
        val dirs = rootDirsFromTargetSpecs(workspaceContext, workspaceRoot).sorted()
        return if (dirs.none()) emptySequence() else removeAlreadyIncludedSubdirs(dirs)
    }

    private fun removeAlreadyIncludedSubdirs(dirs: Sequence<String>): Sequence<URI> {
        var current = dirs.first() + "\n"
        return dirs.filter { path ->
            if (!path.startsWith(current)) {
                current = path
                true
            } else false
        }.map(URI::create)
    }

    private fun rootDirsFromTargetSpecs(
        workspaceContext: WorkspaceContext, workspaceRoot: URI
    ): Sequence<String> {
        val root = Paths.get(workspaceRoot)
        return importTargetSpecs(workspaceContext).map { stripSuffixes(it, ":all", "...", "/") }
            .map { stripPrefixes(it, "//") }.map(root::resolve).filter(Files::exists)
            .map(bazelPathsResolver::resolveUri).map(URI::toString)
    }

    private fun stripSuffixes(s: String, vararg suffixes: String): String =
        suffixes.fold(s) { acc, suffix ->
            if (acc.endsWith(suffix)) acc.substring(0, acc.length - suffix.length)
            else acc
        }

    private fun stripPrefixes(s: String, vararg prefixes: String): String =
        prefixes.fold(s) { acc, prefix ->
            if (acc.startsWith(prefix)) acc.substring(prefix.length)
            else acc
        }

    private fun importTargetSpecs(workspaceContext: WorkspaceContext): Sequence<String> =
        workspaceContext.targets.values.map(BuildTargetIdentifier::getUri).asSequence()

    private fun syntheticModule(moduleName: String, baseDirectory: URI, outputs: Set<URI> = emptySet()): Module {
        val resources = hashSetOf(baseDirectory)
        return Module(
            label = Label(moduleName),
            isSynthetic = true,
            directDependencies = emptyList(),
            languages = emptySet(),
            tags = hashSetOf(Tag.NO_BUILD),
            baseDirectory = baseDirectory,
            sourceSet = SourceSet(emptySet(), emptySet()),
            resources = resources,
            outputs = outputs,
            sourceDependencies = emptySet(),
            languageData = null,
            environmentVariables = hashMapOf()
        )
    }
}
