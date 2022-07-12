package org.jetbrains.bsp.bazel.server.sync

import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.jetbrains.bsp.bazel.info.BspTargetInfo.FileLocation
import org.jetbrains.bsp.bazel.info.BspTargetInfo.TargetInfo
import org.jetbrains.bsp.bazel.server.sync.dependencytree.DependencyTree
import org.jetbrains.bsp.bazel.server.sync.languages.LanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.languages.LanguagePluginsService
import org.jetbrains.bsp.bazel.server.sync.model.Label
import org.jetbrains.bsp.bazel.server.sync.model.Language
import org.jetbrains.bsp.bazel.server.sync.model.Module
import org.jetbrains.bsp.bazel.server.sync.model.Project
import org.jetbrains.bsp.bazel.server.sync.model.SourceSet
import org.jetbrains.bsp.bazel.server.sync.model.Tag
import org.jetbrains.bsp.bazel.workspacecontext.WorkspaceContext
import java.net.URI

class BazelProjectMapper(
    private val languagePluginsService: LanguagePluginsService,
    private val bazelPathsResolver: BazelPathsResolver,
    private val targetKindResolver: TargetKindResolver
) {
    fun createProject(
        targets: Map<String, TargetInfo>,
        rootTargets: Set<String>,
        workspaceContext: WorkspaceContext
    ): Project {
        languagePluginsService.prepareSync(targets.values.asSequence())
        val dependencyTree = DependencyTree(rootTargets, targets)
        val targetsToImport = selectTargetsToImport(workspaceContext, rootTargets, dependencyTree)
        val modulesFromBazel = createModules(targetsToImport, dependencyTree)
        val workspaceRoot = bazelPathsResolver.workspaceRoot()
        val syntheticModules =
            createSyntheticModules(modulesFromBazel, workspaceRoot, workspaceContext)
        val allModules = modulesFromBazel + syntheticModules
        val sourceToTarget = buildReverseSourceMapping(modulesFromBazel)
        return Project(workspaceRoot, allModules.toList(), sourceToTarget)
    }

    private fun selectTargetsToImport(
        workspaceContext: WorkspaceContext, rootTargets: Set<String>, tree: DependencyTree
    ): Sequence<TargetInfo> = tree.allTargetsAtDepth(
        workspaceContext.importDepth.value, rootTargets
    ).asSequence().filter(::isWorkspaceTarget)


    private fun isWorkspaceTarget(target: TargetInfo): Boolean = target.id.startsWith("//")

    private fun createModules(
        targetsToImport: Sequence<TargetInfo>, dependencyTree: DependencyTree
    ): Sequence<Module> = runBlocking {
        targetsToImport.asFlow()
            .map { createModule(it, dependencyTree) }
            .filterNot { it.tags.contains(Tag.NO_IDE) }
            .toList()
            .asSequence()
    }


    private fun createModule(
        target: TargetInfo, dependencyTree: DependencyTree
    ): Module {
        val label = Label(target.id)
        val directDependencies = resolveDirectDependencies(target)
        val languages = inferLanguages(target)
        val tags = targetKindResolver.resolveTags(target)
        val baseDirectory = bazelPathsResolver.labelToDirectoryUri(label)
        val languagePlugin = languagePluginsService.getPlugin(languages)
        val sourceSet = resolveSourceSet(target, languagePlugin)
        val resources = resolveResources(target)
        val languageData = languagePlugin.resolveModule(target)
        val sourceDependencies = languagePlugin.dependencySources(target, dependencyTree)
        return Module(
            label = label,
            isSynthetic = false,
            directDependencies = directDependencies,
            languages = languages,
            tags = tags,
            baseDirectory = baseDirectory,
            sourceSet = sourceSet,
            resources = resources,
            sourceDependencies = sourceDependencies,
            languageData = languageData
        )
    }

    private fun resolveDirectDependencies(target: TargetInfo): List<Label> =
        target.dependenciesList.map { Label(it.id) }

    private fun inferLanguages(target: TargetInfo): Set<Language> =
        target.sourcesList.flatMap { source: FileLocation ->
            Language.all().filter { isLanguageFile(source, it) }
        }.toHashSet()

    private fun isLanguageFile(file: FileLocation, language: Language): Boolean =
        language.extensions.any {
            file.relativePath.endsWith(it)
        }

    private fun resolveSourceSet(target: TargetInfo, languagePlugin: LanguagePlugin<*>): SourceSet {
        val sources = target.sourcesList.asSequence().map(bazelPathsResolver::resolve)
        val sourceRoots = sources.mapNotNull(languagePlugin::calculateSourceRoot)
        return SourceSet(
            sources.map(bazelPathsResolver::resolveUri).toSet(),
            sourceRoots.map(bazelPathsResolver::resolveUri).toSet()
        )
    }

    private fun resolveResources(target: TargetInfo): Set<URI> =
        bazelPathsResolver.resolveUris(target.resourcesList).toSet()

    // TODO make this feature configurable with flag in project view file
    private fun createSyntheticModules(
        modulesFromBazel: Sequence<Module>, workspaceRoot: URI, workspaceContext: WorkspaceContext
    ): Sequence<Module> = IntelliJProjectTreeViewFix(bazelPathsResolver).createModules(
        workspaceRoot,
        modulesFromBazel,
        workspaceContext,
    )

    private fun buildReverseSourceMapping(modules: Sequence<Module>): Map<URI, Label> =
        modules.flatMap(::buildReverseSourceMappingForModule).toMap()

    private fun buildReverseSourceMappingForModule(module: Module): List<Pair<URI, Label>> =
        with(module) {
            (sourceSet.sources + resources).map { Pair(it, label) }
        }
}
