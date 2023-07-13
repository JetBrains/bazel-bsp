package org.jetbrains.bsp.bazel.server.sync

import ch.epfl.scala.bsp4j.BuildServerCapabilities
import ch.epfl.scala.bsp4j.BuildTarget
import ch.epfl.scala.bsp4j.BuildTargetCapabilities
import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import ch.epfl.scala.bsp4j.CompileProvider
import ch.epfl.scala.bsp4j.CppOptionsItem
import ch.epfl.scala.bsp4j.CppOptionsParams
import ch.epfl.scala.bsp4j.CppOptionsResult
import ch.epfl.scala.bsp4j.DependencySourcesItem
import ch.epfl.scala.bsp4j.DependencySourcesParams
import ch.epfl.scala.bsp4j.DependencySourcesResult
import ch.epfl.scala.bsp4j.InitializeBuildResult
import ch.epfl.scala.bsp4j.InverseSourcesParams
import ch.epfl.scala.bsp4j.InverseSourcesResult
import ch.epfl.scala.bsp4j.JavacOptionsItem
import ch.epfl.scala.bsp4j.JavacOptionsParams
import ch.epfl.scala.bsp4j.JavacOptionsResult
import ch.epfl.scala.bsp4j.JvmEnvironmentItem
import ch.epfl.scala.bsp4j.JvmRunEnvironmentParams
import ch.epfl.scala.bsp4j.JvmRunEnvironmentResult
import ch.epfl.scala.bsp4j.JvmTestEnvironmentParams
import ch.epfl.scala.bsp4j.JvmTestEnvironmentResult
import ch.epfl.scala.bsp4j.OutputPathItem
import ch.epfl.scala.bsp4j.OutputPathItemKind
import ch.epfl.scala.bsp4j.OutputPathsItem
import ch.epfl.scala.bsp4j.OutputPathsParams
import ch.epfl.scala.bsp4j.OutputPathsResult
import ch.epfl.scala.bsp4j.ResourcesItem
import ch.epfl.scala.bsp4j.ResourcesParams
import ch.epfl.scala.bsp4j.ResourcesResult
import ch.epfl.scala.bsp4j.RunProvider
import ch.epfl.scala.bsp4j.ScalaMainClassesParams
import ch.epfl.scala.bsp4j.ScalaMainClassesResult
import ch.epfl.scala.bsp4j.ScalaTestClassesParams
import ch.epfl.scala.bsp4j.ScalaTestClassesResult
import ch.epfl.scala.bsp4j.ScalacOptionsParams
import ch.epfl.scala.bsp4j.ScalacOptionsResult
import ch.epfl.scala.bsp4j.SourceItem
import ch.epfl.scala.bsp4j.SourceItemKind
import ch.epfl.scala.bsp4j.SourcesItem
import ch.epfl.scala.bsp4j.SourcesParams
import ch.epfl.scala.bsp4j.SourcesResult
import ch.epfl.scala.bsp4j.TestProvider
import ch.epfl.scala.bsp4j.WorkspaceBuildTargetsResult
import org.jetbrains.bsp.bazel.commons.Constants
import org.jetbrains.bsp.bazel.server.sync.languages.LanguagePluginsService
import org.jetbrains.bsp.bazel.server.sync.languages.jvm.javaModule
import org.jetbrains.bsp.bazel.server.sync.model.Label
import org.jetbrains.bsp.bazel.server.sync.model.Language
import org.jetbrains.bsp.bazel.server.sync.model.Module
import org.jetbrains.bsp.bazel.server.sync.model.Project
import org.jetbrains.bsp.bazel.server.sync.model.Tag
import org.jetbrains.bsp.bazel.workspacecontext.WorkspaceContextProvider

class BspProjectMapper(
    private val languagePluginsService: LanguagePluginsService,
    private val workspaceContextProvider: WorkspaceContextProvider
) {

    fun initializeServer(supportedLanguages: Set<Language>): InitializeBuildResult {
        val languageNames = supportedLanguages.map { it.id }
        val capabilities = BuildServerCapabilities().apply {
            compileProvider = CompileProvider(languageNames)
            runProvider = RunProvider(languageNames)
            testProvider = TestProvider(languageNames)
            outputPathsProvider = true
            dependencySourcesProvider = true
            inverseSourcesProvider = true
            resourcesProvider = true
            jvmRunEnvironmentProvider = true
            jvmTestEnvironmentProvider = true
        }
        return InitializeBuildResult(
            Constants.NAME, Constants.VERSION, Constants.BSP_VERSION, capabilities
        )
    }

    fun workspaceTargets(project: Project): WorkspaceBuildTargetsResult {
        val buildTargets = project.modules.map(::toBuildTarget)
        return WorkspaceBuildTargetsResult(buildTargets)
    }

    fun workspaceLibraries(project: Project): WorkspaceLibrariesResult {
        val libraries = project.libraries.values.map { LibraryItem(
                BuildTargetIdentifier(it.label),
                it.dependencies.map { dep -> BuildTargetIdentifier(dep) },
                it.outputs.map { uri -> uri.toString() }
        ) }
        return WorkspaceLibrariesResult(libraries)
    }

    private fun toBuildTarget(module: Module): BuildTarget {
        val label = BspMappings.toBspId(module)
        val dependencies =
            module.directDependencies.map(BspMappings::toBspId)
        val languages = module.languages.flatMap(Language::allNames)
        val capabilities = inferCapabilities(module)
        val tags = module.tags.mapNotNull(BspMappings::toBspTag)
        val baseDirectory = BspMappings.toBspUri(module.baseDirectory)
        val buildTarget = BuildTarget(
            label,
            tags,
            languages,
            dependencies,
            capabilities
        )
        buildTarget.displayName = label.uri
        buildTarget.baseDirectory = baseDirectory
        applyLanguageData(module, buildTarget)
        return buildTarget
    }

    private fun inferCapabilities(module: Module): BuildTargetCapabilities {
        val canCompile = !module.tags.contains(Tag.NO_BUILD) && isBuildableIfManual(module)
        val canTest = module.tags.contains(Tag.TEST) && !module.tags.contains(Tag.MANUAL)
        val canRun = module.tags.contains(Tag.APPLICATION) && !module.tags.contains(Tag.MANUAL)
        return BuildTargetCapabilities(canCompile, canTest, canRun)
    }

    private fun isBuildableIfManual(module: Module): Boolean =
        (!module.tags.contains(Tag.MANUAL)
                || workspaceContextProvider.currentWorkspaceContext().buildManualTargets.value)


    private fun applyLanguageData(module: Module, buildTarget: BuildTarget) {
        val plugin = languagePluginsService.getPlugin(module.languages)
        module.languageData?.let { plugin.setModuleData(it, buildTarget) }
    }

    fun sources(project: Project, sourcesParams: SourcesParams): SourcesResult {
        fun toSourcesItem(module: Module): SourcesItem {
            val sourceSet = module.sourceSet
            val sourceItems = sourceSet.sources.map {
                SourceItem(
                    BspMappings.toBspUri(it),
                    SourceItemKind.FILE,
                    false
                )
            }
            val sourceRoots = sourceSet.sourceRoots.map(BspMappings::toBspUri)
            val sourcesItem = SourcesItem(BspMappings.toBspId(module), sourceItems)
            sourcesItem.roots = sourceRoots
            return sourcesItem
        }

        fun emptySourcesItem(label: Label): SourcesItem =
            SourcesItem(BspMappings.toBspId(label), emptyList())

        // TODO handle generated sources. google's plugin doesn't ever mark source root as generated
        // we need a use case with some generated files and then figure out how to handle it
        val labels = BspMappings.toLabels(sourcesParams.targets)
        val sourcesItems = labels.map {
            project.findModule(it)?.let(::toSourcesItem) ?: emptySourcesItem(it)
        }
        return SourcesResult(sourcesItems)
    }


    fun resources(project: Project, resourcesParams: ResourcesParams): ResourcesResult {
        fun toResourcesItem(module: Module): ResourcesItem {
            val resources = module.resources.map(BspMappings::toBspUri)
            return ResourcesItem(BspMappings.toBspId(module), resources)
        }

        fun emptyResourcesItem(label: Label): ResourcesItem {
            return ResourcesItem(BspMappings.toBspId(label), emptyList())
        }

        val labels = BspMappings.toLabels(resourcesParams.targets)
        val resourcesItems = labels.map {
            project.findModule(it)?.let(::toResourcesItem) ?: emptyResourcesItem(it)
        }
        return ResourcesResult(resourcesItems)
    }


    fun inverseSources(
        project: Project, inverseSourcesParams: InverseSourcesParams
    ): InverseSourcesResult {
        val documentUri = BspMappings.toUri(inverseSourcesParams.textDocument)
        val targets = project.findTargetBySource(documentUri)
            ?.let { listOf(BspMappings.toBspId(it)) }
            .orEmpty()
        return InverseSourcesResult(targets)
    }

    fun dependencySources(
        project: Project, dependencySourcesParams: DependencySourcesParams
    ): DependencySourcesResult {
        fun getDependencySourcesItem(label: Label): DependencySourcesItem {
            val sources = project.findModule(label)
                    ?.sourceDependencies
                    ?.map(BspMappings::toBspUri)
                    .orEmpty()
            return DependencySourcesItem(BspMappings.toBspId(label), sources)
        }

        val labels = BspMappings.toLabels(dependencySourcesParams.targets)
        val items = labels.map(::getDependencySourcesItem)
        return DependencySourcesResult(items)
    }


    fun outputPaths(project: Project, params: OutputPathsParams): OutputPathsResult {
        fun getItem(label: Label): OutputPathsItem {
            val items = project.findModule(label)?.let { module ->
                module.outputs.map { OutputPathItem(BspMappings.toBspUri(it), OutputPathItemKind.DIRECTORY) }
            }.orEmpty()
            return OutputPathsItem(BspMappings.toBspId(label), items)
        }

        val labels = BspMappings.toLabels(params.targets)
        val items = labels.map(::getItem)
        return OutputPathsResult(items)
    }

    fun jvmRunEnvironment(
        project: Project, params: JvmRunEnvironmentParams
    ): JvmRunEnvironmentResult {
        val targets = params.targets
        val result = getJvmEnvironmentItems(project, targets)
        return JvmRunEnvironmentResult(result)
    }

    fun jvmTestEnvironment(
        project: Project, params: JvmTestEnvironmentParams
    ): JvmTestEnvironmentResult {
        val targets = params.targets
        val result = getJvmEnvironmentItems(project, targets)
        return JvmTestEnvironmentResult(result)
    }

    private fun getJvmEnvironmentItems(
        project: Project, targets: List<BuildTargetIdentifier>
    ): List<JvmEnvironmentItem> {
        fun extractJvmEnvironmentItem(module: Module): JvmEnvironmentItem? =
            module.javaModule?.let {
                languagePluginsService.javaLanguagePlugin.toJvmEnvironmentItem(module, it)
            }

        val labels = BspMappings.toLabels(targets)
        return labels.mapNotNull {
            project.findModule(it)?.let(::extractJvmEnvironmentItem)
        }
    }

    fun buildTargetJavacOptions(project: Project, params: JavacOptionsParams): JavacOptionsResult {
        fun extractJavacOptionsItem(module: Module): JavacOptionsItem? =
            module.javaModule?.let {
                languagePluginsService.javaLanguagePlugin.toJavacOptionsItem(module, it)
            }

        val modules = BspMappings.getModules(project, params.targets)
        val items = modules.mapNotNull(::extractJavacOptionsItem)
        return JavacOptionsResult(items)
    }

    fun buildTargetCppOptions(project: Project, params: CppOptionsParams): CppOptionsResult {
        fun extractCppOptionsItem(module: Module): CppOptionsItem? =
            languagePluginsService.extractCppModule(module)?.let {
                languagePluginsService.cppLanguagePlugin.toCppOptionsItem(module, it)
            }

        val modules = BspMappings.getModules(project, params.targets)
        val items = modules.mapNotNull(::extractCppOptionsItem)
        return CppOptionsResult(items)
    }


    fun buildTargetScalacOptions(
        project: Project,
        params: ScalacOptionsParams
    ): ScalacOptionsResult {
        val labels = params.targets.map(BuildTargetIdentifier::getUri).map(::Label)
        val modules = labels.mapNotNull(project::findModule)
        val scalaLanguagePlugin = languagePluginsService.scalaLanguagePlugin
        val items = modules.mapNotNull(scalaLanguagePlugin::toScalacOptionsItem)
        return ScalacOptionsResult(items)
    }

    fun buildTargetScalaTestClasses(
        project: Project, params: ScalaTestClassesParams
    ): ScalaTestClassesResult {
        val modules = BspMappings.getModules(project, params.targets)
        val scalaLanguagePlugin = languagePluginsService.scalaLanguagePlugin
        val items = modules.mapNotNull(scalaLanguagePlugin::toScalaTestClassesItem)
        return ScalaTestClassesResult(items)
    }

    fun buildTargetScalaMainClasses(
        project: Project, params: ScalaMainClassesParams
    ): ScalaMainClassesResult {
        val modules = BspMappings.getModules(project, params.targets)
        val scalaLanguagePlugin = languagePluginsService.scalaLanguagePlugin
        val items = modules.mapNotNull(scalaLanguagePlugin::toScalaMainClassesItem)
        return ScalaMainClassesResult(items)
    }
}
