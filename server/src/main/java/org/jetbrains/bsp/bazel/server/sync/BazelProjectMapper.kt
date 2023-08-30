package org.jetbrains.bsp.bazel.server.sync

import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.jetbrains.bsp.bazel.bazelrunner.BazelInfo
import org.jetbrains.bsp.bazel.info.BspTargetInfo.FileLocation
import org.jetbrains.bsp.bazel.info.BspTargetInfo.TargetInfo
import org.jetbrains.bsp.bazel.server.sync.dependencytree.DependencyTree
import org.jetbrains.bsp.bazel.server.sync.languages.LanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.languages.LanguagePluginsService
import org.jetbrains.bsp.bazel.server.sync.model.Label
import org.jetbrains.bsp.bazel.server.sync.model.Language
import org.jetbrains.bsp.bazel.server.sync.model.Library
import org.jetbrains.bsp.bazel.server.sync.model.Module
import org.jetbrains.bsp.bazel.server.sync.model.Project
import org.jetbrains.bsp.bazel.server.sync.model.SourceSet
import org.jetbrains.bsp.bazel.server.sync.model.Tag
import org.jetbrains.bsp.bazel.workspacecontext.WorkspaceContext
import java.net.URI

const val KOTLIN_STDLIB_ROOT_EXECUTION = "external/com_github_jetbrains_kotlin"
const val KOTLIN_STDLIB_RELATIVE_PATH_PREFIX = "lib/"

class BazelProjectMapper(
    private val languagePluginsService: LanguagePluginsService,
    private val bazelPathsResolver: BazelPathsResolver,
    private val targetKindResolver: TargetKindResolver,
    private val bazelInfo: BazelInfo
) {
    fun createProject(
        targets: Map<String, TargetInfo>,
        rootTargets: Set<String>,
        workspaceContext: WorkspaceContext
    ): Project {
        languagePluginsService.prepareSync(targets.values.asSequence())
        val dependencyTree = DependencyTree(rootTargets, targets)
        val targetsToImport = selectTargetsToImport(workspaceContext, rootTargets, dependencyTree)
        val targetsAsLibraries = targets - targetsToImport.map { it.id }.toSet()
        val annotationProcessorLibraries = annotationProcessorLibraries(targetsToImport)
        val kotlinStdlibsMapper = calculateKotlinStdlibsMapper(targetsToImport)
        val allLibraries = concatenateMaps(annotationProcessorLibraries, kotlinStdlibsMapper)
        val modulesFromBazel = createModules(targetsToImport, dependencyTree, allLibraries)
        val librariesToImport = createLibraries(targetsAsLibraries) +
            allLibraries.values.flatten().associateBy { it.label }
        val workspaceRoot = bazelPathsResolver.workspaceRoot()
        val modifiedModules = modifyModules(modulesFromBazel, workspaceRoot, workspaceContext)
        val sourceToTarget = buildReverseSourceMapping(modifiedModules)
        return Project(workspaceRoot, modifiedModules.toList(), sourceToTarget, librariesToImport)
    }

    private fun <K, V> concatenateMaps(
        left: Map<K, List<V>>,
        right: Map<K, List<V>>
    ) = (left.keys + right.keys).associateWith { left[it].orEmpty() + right[it].orEmpty() }

    private fun annotationProcessorLibraries(targetsToImport: Sequence<TargetInfo>): Map<String, List<Library>> {
        return targetsToImport
            .filter { it.jvmTargetInfo.generatedJarsList.isNotEmpty() }
            .associate { targetInfo ->
                targetInfo.id to
                    Library(
                        label = targetInfo.id + "_generated",
                        outputs = targetInfo.jvmTargetInfo.generatedJarsList
                            .flatMap { it.binaryJarsList }
                            .map { bazelPathsResolver.resolveUri(it) }
                            .toSet(),
                        sources = emptySet(),
                        dependencies = emptyList(),
                    )
            }
            .map { it.key to listOf(it.value) }
            .toMap()
    }

    private fun calculateKotlinStdlibsMapper(targetsToImport: Sequence<TargetInfo>): Map<String, List<Library>> {
        val projectLevelKotlinStdlibs = calculateProjectLevelKotlinStdlibs(targetsToImport)
        val rulesKotlinTargets = targetsToImport
            .filter { targetInfo -> targetInfo.jvmTargetInfo.compileClasspathList.any { it.isKotlinStdlibPath() } }
            .map { it.id }
            .toSet()
        return rulesKotlinTargets.associateWith { listOf(projectLevelKotlinStdlibs) }
    }

    private fun calculateProjectLevelKotlinStdlibs(targets: Sequence<TargetInfo>) =
        Library(
            label = "rules_kotlin_kotlin-stdlibs",
            outputs = targets
                .flatMap { it.jvmTargetInfo.compileClasspathList }
                .filter { it.isKotlinStdlibPath() }
                .map { bazelPathsResolver.resolveUri(it) }
                .toSet(),
            sources = emptySet(),
            dependencies = emptyList(),
        )

    private fun FileLocation.isKotlinStdlibPath() =
        this.rootExecutionPathFragment == KOTLIN_STDLIB_ROOT_EXECUTION &&
            this.relativePath.startsWith(KOTLIN_STDLIB_RELATIVE_PATH_PREFIX)


    private fun createLibraries(targets: Map<String, TargetInfo>): Map<String, Library> {
        return targets.mapValues { entry ->
            val targetId = entry.key
            val targetInfo = entry.value
            Library(
                label = targetId,
                outputs = getTargetJarUris(targetInfo),
                sources = getSourceJarUris(targetInfo),
                dependencies = targetInfo.dependenciesList.map { it.id },
            )
        }
    }

    private fun List<FileLocation>.resolveUris() =
        map { bazelPathsResolver.resolve(it).toUri() }.toSet()

    private fun getTargetJarUris(targetInfo: TargetInfo) =
        targetInfo.jvmTargetInfo.jarsList
            .flatMap { it.binaryJarsList }
            .resolveUris()

    private fun getSourceJarUris(targetInfo: TargetInfo) =
        targetInfo.jvmTargetInfo.jarsList
            .flatMap { it.sourceJarsList }
            .resolveUris()

    private fun selectTargetsToImport(
        workspaceContext: WorkspaceContext, rootTargets: Set<String>, tree: DependencyTree
    ): Sequence<TargetInfo> = tree.allTargetsAtDepth(
        workspaceContext.importDepth.value, rootTargets
    ).asSequence().filter(::isWorkspaceTarget)

    private fun hasJavaSources(targetInfo: TargetInfo) =
        targetInfo.sourcesList.any {
            it.relativePath.endsWith(".java") ||
                it.relativePath.endsWith(".kt") ||
                it.relativePath.endsWith(".scala") ||
                it.relativePath.endsWith(".py") ||
                it.relativePath.endsWith(".sh")
        }

    private fun isWorkspaceTarget(target: TargetInfo): Boolean =
        target.id.startsWith(bazelInfo.release.mainRepositoryReferencePrefix()) &&
            (hasJavaSources(target) ||
                target.kind in setOf("java_library", "java_binary", "kt_jvm_library", "kt_jvm_binary"))

    private fun createModules(
        targetsToImport: Sequence<TargetInfo>,
        dependencyTree: DependencyTree,
        generatedLibraries: Map<String, Collection<Library>>,
    ): Sequence<Module> = runBlocking {
        targetsToImport.asFlow()
            .map { createModule(
                it,
                dependencyTree,
                generatedLibraries[it.id].orEmpty())
            }
            .filterNot { it.tags.contains(Tag.NO_IDE) }
            .toList()
            .asSequence()
    }


    private fun createModule(
        target: TargetInfo,
        dependencyTree: DependencyTree,
        extraLibraries: Collection<Library>,
    ): Module {
        val label = Label(target.id)
        val directDependencies = resolveDirectDependencies(target) + extraLibraries.map {  Label(it.label) }
        val languages = inferLanguages(target)
        val tags = targetKindResolver.resolveTags(target)
        val baseDirectory = bazelPathsResolver.labelToDirectoryUri(label)
        val languagePlugin = languagePluginsService.getPlugin(languages)
        val sourceSet = resolveSourceSet(target, languagePlugin)
        val resources = resolveResources(target)
        val languageData = languagePlugin.resolveModule(target)
        val sourceDependencies = languagePlugin.dependencySources(target, dependencyTree)
        val environment = environmentItem(target)
        return Module(
            label = label,
            isSynthetic = false,
            directDependencies = directDependencies,
            languages = languages,
            tags = tags,
            baseDirectory = baseDirectory,
            sourceSet = sourceSet,
            resources = resources,
            outputs = emptySet(),
            sourceDependencies = sourceDependencies,
            languageData = languageData,
            environmentVariables = environment,
        )
    }

    private fun resolveDirectDependencies(target: TargetInfo): List<Label> =
        target.dependenciesList.map { Label(it.id) }

    private fun inferLanguages(target: TargetInfo): Set<Language> =
        if (target.sourcesList.isEmpty()) {
            Language.all().filter { isBinaryTargetOfLanguage(target.kind, it) }.toHashSet()
        } else {
            target.sourcesList.flatMap { source: FileLocation ->
                Language.all().filter { isLanguageFile(source, it) }
            }.toHashSet()
        }

    private fun isLanguageFile(file: FileLocation, language: Language): Boolean =
        language.extensions.any {
            file.relativePath.endsWith(it)
        }

    private fun isBinaryTargetOfLanguage(kind: String, language: Language): Boolean =
        language.binary_targets.contains(kind)

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

    private fun modifyModules(
        modulesFromBazel: Sequence<Module>, workspaceRoot: URI, workspaceContext: WorkspaceContext
    ): Sequence<Module> {
        // TODO make this feature configurable with flag in project view file
        return IntelliJProjectTreeViewFix(bazelPathsResolver, bazelInfo).createModules(
            workspaceRoot,
            modulesFromBazel,
            workspaceContext,
        )
    }

    private fun buildReverseSourceMapping(modules: Sequence<Module>): Map<URI, Label> =
        modules.flatMap(::buildReverseSourceMappingForModule).toMap()

    private fun buildReverseSourceMappingForModule(module: Module): List<Pair<URI, Label>> =
        with(module) {
            (sourceSet.sources + resources).map { Pair(it, label) }
        }

    private fun environmentItem(target: TargetInfo): Map<String, String> {
        val inheritedEnvs = collectInheritedEnvs(target)
        val targetEnv = target.envMap
        return inheritedEnvs + targetEnv
    }

    private fun collectInheritedEnvs(targetInfo: TargetInfo): Map<String, String> =
        targetInfo.envInheritList.associateWith { System.getenv(it) }

}
