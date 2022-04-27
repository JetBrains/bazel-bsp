package org.jetbrains.bsp.bazel.server.sync

import io.vavr.collection.HashSet
import io.vavr.collection.List
import io.vavr.collection.Map
import io.vavr.collection.Set
import io.vavr.control.Option
import org.jetbrains.bsp.bazel.info.BspTargetInfo
import org.jetbrains.bsp.bazel.info.BspTargetInfo.FileLocation
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import org.jetbrains.bsp.bazel.server.bsp.utils.SourceRootGuesser
import org.jetbrains.bsp.bazel.server.sync.dependencytree.DependencyTree
import org.jetbrains.bsp.bazel.server.sync.languages.LanguageData
import org.jetbrains.bsp.bazel.server.sync.languages.LanguagePluginsService
import org.jetbrains.bsp.bazel.server.sync.model.*
import java.net.URI
import java.nio.file.Path
import java.util.function.Consumer

class BazelProjectMapper(
        private val languagePluginsService: LanguagePluginsService,
        private val bazelPathsResolver: BazelPathsResolver,
        private val targetKindResolver: TargetKindResolver) {
    fun createProject(
            targets: Map<String?, BspTargetInfo.TargetInfo>, rootTargets: Set<String>, projectView: ProjectView): Project {
        languagePluginsService.prepareSync(targets.values())
        val dependencyTree = DependencyTree(targets, rootTargets)
        val targetsToImport = selectTargetsToImport(rootTargets, targets)
        val modulesFromBazel = createModules(targetsToImport, dependencyTree)
        val workspaceRoot = bazelPathsResolver.workspaceRoot()
        val syntheticModules = createSyntheticModules(modulesFromBazel, workspaceRoot, projectView)
        val allModules = modulesFromBazel.appendAll(syntheticModules)
        val sourceToTarget = buildReverseSourceMapping(modulesFromBazel)
        return Project(workspaceRoot, allModules, sourceToTarget)
    }

    // When we will be implementing transitive import (configurable through project view),
    // here we will implement the logic to include more targets than the root ones.
    private fun selectTargetsToImport(
            rootTargets: Set<String>, targets: Map<String?, BspTargetInfo.TargetInfo>): List<BspTargetInfo.TargetInfo> =
            List.ofAll(rootTargets).flatMap { targets[it] }

    private fun createModules(
            targetsToImport: List<BspTargetInfo.TargetInfo>, dependencyTree: DependencyTree): List<Module> =
            targetsToImport
                    .map { target: BspTargetInfo.TargetInfo -> createModule(target, dependencyTree) }
                    .filter { !it.tags().contains(Tag.NO_IDE) }

    private fun createModule(target: BspTargetInfo.TargetInfo, dependencyTree: DependencyTree): Module {
        val label = Label.from(target.id)
        val directDependencies = resolveDirectDependencies(target)
        val languages = inferLanguages(target)
        val tags = targetKindResolver.resolveTags(target)
        val baseDirectory = bazelPathsResolver.labelToDirectory(label).toUri()
        val sourceSet = resolveSourceSet(target)
        val resources = resolveResources(target)
        val languagePlugin = languagePluginsService.getPlugin(languages)
        val languageData = languagePlugin.resolveModule(target) as Option<LanguageData>
        val sourceDependencies = languagePlugin.dependencySources(target, dependencyTree)
        return Module(
                label,
                false,
                directDependencies,
                languages,
                tags,
                baseDirectory,
                sourceSet,
                resources,
                sourceDependencies,
                languageData)
    }

    private fun resolveDirectDependencies(target: BspTargetInfo.TargetInfo): List<Label> =
            List.ofAll(target.dependenciesList).map { dep: BspTargetInfo.Dependency -> Label.from(dep.id) }

    private fun inferLanguages(target: BspTargetInfo.TargetInfo): Set<Language> =
            HashSet.ofAll(target.sourcesList)
                    .flatMap { Language.all().filter { language: Language -> isLanguageFile(it, language) } }

    private fun isLanguageFile(file: FileLocation, language: Language): Boolean =
            language.extensions.exists { file.relativePath.endsWith(it!!) }

    private fun resolveSourceSet(target: BspTargetInfo.TargetInfo): SourceSet {
        val sources = HashSet.ofAll(target.sourcesList).map { fileLocation: FileLocation? -> bazelPathsResolver.resolve(fileLocation) }
        val sourceRoots = sources.map { sourcePath: Path? -> SourceRootGuesser.getSourcesRoot(sourcePath) }
        return SourceSet(sources.map { it.toUri() }, sourceRoots.map { it.toUri() })
    }

    private fun resolveResources(target: BspTargetInfo.TargetInfo): Set<URI> =
            bazelPathsResolver.resolveUris(target.resourcesList).toSet()

    // TODO make this feature configurable with flag in project view file
    private fun createSyntheticModules(
            modulesFromBazel: List<Module>, workspaceRoot: URI, projectView: ProjectView): List<Module> =
            IntelliJProjectTreeViewFix()
                    .createModules(workspaceRoot, modulesFromBazel, projectView)

    private fun buildReverseSourceMapping(modules: List<Module>): Map<URI, Label> {
        val output = HashMap<URI, Label>()
        modules.forEach(
                Consumer {
                    it.sourceSet().sources().forEach(Consumer { source: URI -> output[source] = it.label() })
                    it.resources().forEach(Consumer { resource: URI -> output[resource] = it.label() })
                })
        return io.vavr.collection.HashMap.ofAll(output)
    }
}