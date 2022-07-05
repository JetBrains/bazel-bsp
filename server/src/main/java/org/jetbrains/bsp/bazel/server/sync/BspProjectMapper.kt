package org.jetbrains.bsp.bazel.server.sync

import ch.epfl.scala.bsp4j.*
import org.jetbrains.bsp.bazel.commons.Constants
import org.jetbrains.bsp.bazel.logger.BspClientLogger
import org.jetbrains.bsp.bazel.server.sync.languages.LanguagePluginsService
import org.jetbrains.bsp.bazel.server.sync.model.*
import org.jetbrains.bsp.bazel.workspacecontext.WorkspaceContextProvider
import java.net.URI

class BspProjectMapper(
    private val languagePluginsService: LanguagePluginsService,
    private val workspaceContextProvider: WorkspaceContextProvider
) {
    fun initializeServer(supportedLanguages: Set<Language>): InitializeBuildResult {
        val languageNames = supportedLanguages.map { obj: Language -> obj.id }
        val capabilities = BuildServerCapabilities().apply {
            compileProvider = CompileProvider(languageNames)
            runProvider = RunProvider(languageNames)
            testProvider = TestProvider(languageNames)
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
        // TODO handle generated sources. google's plugin doesn't ever mark source root as generated
        // we need a use case with some generated files and then figure out how to handle it
        val labels = BspMappings.toLabels(sourcesParams.targets)
        val sourcesItems = labels.map { label: Label ->
            project.findModule(label)?.let(::toSourcesItem) ?: emptySourcesItem(label)
        }
        return SourcesResult(sourcesItems)
    }

    private fun toSourcesItem(module: Module): SourcesItem {
        val sourceSet = module.sourceSet
        val sourceItems = sourceSet.sources.map { source: URI ->
            SourceItem(
                BspMappings.toBspUri(source),
                SourceItemKind.FILE,
                false
            )
        }
        val sourceRoots = sourceSet.sourceRoots.map(BspMappings::toBspUri)
        val sourcesItem = SourcesItem(BspMappings.toBspId(module), sourceItems)
        sourcesItem.roots = sourceRoots
        return sourcesItem
    }

    private fun emptySourcesItem(label: Label): SourcesItem =
        SourcesItem(BspMappings.toBspId(label), emptyList())

    fun resources(project: Project, resourcesParams: ResourcesParams): ResourcesResult {
        val labels = BspMappings.toLabels(resourcesParams.targets)
        val resourcesItems = labels.map { label: Label ->
            project.findModule(label)?.let(::toResourcesItem) ?: emptyResourcesItem(label)
        }
        return ResourcesResult(resourcesItems)
    }

    private fun toResourcesItem(module: Module): ResourcesItem {
        val resources = module.resources.map(BspMappings::toBspUri)
        return ResourcesItem(BspMappings.toBspId(module), resources)
    }

    private fun emptyResourcesItem(label: Label): ResourcesItem {
        return ResourcesItem(BspMappings.toBspId(label), emptyList())
    }

    fun inverseSources(
        project: Project, inverseSourcesParams: InverseSourcesParams
    ): InverseSourcesResult {
        val documentUri = BspMappings.toUri(inverseSourcesParams.textDocument)
        val targets =
            project.findTargetBySource(documentUri)?.let { listOf(BspMappings.toBspId(it)) }
                .orEmpty()
        return InverseSourcesResult(targets)
    }

    fun dependencySources(
        project: Project, dependencySourcesParams: DependencySourcesParams
    ): DependencySourcesResult {
        val labels = dependencySourcesParams.targets.map { Label(it.uri) }.toSet()
        val items = labels.map { label: Label -> getDependencySourcesItem(project, label) }
        return DependencySourcesResult(items)
    }

    private fun getDependencySourcesItem(project: Project, label: Label): DependencySourcesItem {
        val sources = project
            .findModule(label)?.let { module: Module ->
                module.sourceDependencies.map(BspMappings::toBspUri)
            }.orEmpty()
        return DependencySourcesItem(BspMappings.toBspId(label), sources)
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
        val labels = BspMappings.toLabels(targets)
        return labels.mapNotNull { label: Label ->
            project.findModule(label)?.let(::extractJvmEnvironmentItem)
        }
    }

    private fun extractJvmEnvironmentItem(module: Module): JvmEnvironmentItem? =
        languagePluginsService.extractJavaModule(module)?.let {
            languagePluginsService.javaLanguagePlugin.toJvmEnvironmentItem(
                module,
                it
            )
        }

    fun buildTargetJavacOptions(project: Project, params: JavacOptionsParams): JavacOptionsResult {
        val modules = BspMappings.getModules(project, params.targets)
        val items = modules.mapNotNull(::extractJavacOptionsItem)
        return JavacOptionsResult(items)
    }

    private fun extractJavacOptionsItem(module: Module): JavacOptionsItem? =
        languagePluginsService.extractJavaModule(module)?.let {
            languagePluginsService.javaLanguagePlugin.toJavacOptionsItem(
                module,
                it
            )
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
        val items =
            modules.mapNotNull(scalaLanguagePlugin::toScalaTestClassesItem)
        return ScalaTestClassesResult(items)
    }

    fun buildTargetScalaMainClasses(
        project: Project, params: ScalaMainClassesParams
    ): ScalaMainClassesResult {
        val modules = BspMappings.getModules(project, params.targets)
        val scalaLanguagePlugin = languagePluginsService.scalaLanguagePlugin
        val items =
            modules.mapNotNull(scalaLanguagePlugin::toScalaMainClassesItem)
        return ScalaMainClassesResult(items)
    }
}
