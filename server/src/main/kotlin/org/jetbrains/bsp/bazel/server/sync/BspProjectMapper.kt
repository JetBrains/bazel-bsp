package org.jetbrains.bsp.bazel.server.sync

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
import ch.epfl.scala.bsp4j.JvmMainClass
import ch.epfl.scala.bsp4j.JvmRunEnvironmentParams
import ch.epfl.scala.bsp4j.JvmRunEnvironmentResult
import ch.epfl.scala.bsp4j.JvmTestEnvironmentParams
import ch.epfl.scala.bsp4j.JvmTestEnvironmentResult
import ch.epfl.scala.bsp4j.OutputPathItem
import ch.epfl.scala.bsp4j.OutputPathItemKind
import ch.epfl.scala.bsp4j.OutputPathsItem
import ch.epfl.scala.bsp4j.OutputPathsParams
import ch.epfl.scala.bsp4j.OutputPathsResult
import ch.epfl.scala.bsp4j.PythonOptionsItem
import ch.epfl.scala.bsp4j.PythonOptionsParams
import ch.epfl.scala.bsp4j.PythonOptionsResult
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
import org.eclipse.lsp4j.jsonrpc.CancelChecker
import org.jetbrains.bsp.BazelBuildServerCapabilities
import org.jetbrains.bsp.DirectoryItem
import org.jetbrains.bsp.LibraryItem
import org.jetbrains.bsp.WorkspaceDirectoriesResult
import org.jetbrains.bsp.WorkspaceInvalidTargetsResult
import org.jetbrains.bsp.WorkspaceLibrariesResult
import org.jetbrains.bsp.bazel.bazelrunner.BazelRunner
import org.jetbrains.bsp.bazel.commons.Constants
import org.jetbrains.bsp.bazel.server.bsp.info.BspInfo
import org.jetbrains.bsp.bazel.server.sync.languages.LanguagePluginsService
import org.jetbrains.bsp.bazel.server.sync.languages.jvm.javaModule
import org.jetbrains.bsp.bazel.server.sync.model.Label
import org.jetbrains.bsp.bazel.server.sync.model.Language
import org.jetbrains.bsp.bazel.server.sync.model.Module
import org.jetbrains.bsp.bazel.server.sync.model.Project
import org.jetbrains.bsp.bazel.server.sync.model.Tag
import org.jetbrains.bsp.bazel.workspacecontext.WorkspaceContextProvider
import java.net.URI
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.name
import kotlin.io.path.toPath

class BspProjectMapper(
        private val languagePluginsService: LanguagePluginsService,
        private val workspaceContextProvider: WorkspaceContextProvider,
        private val bazelPathsResolver: BazelPathsResolver,
        private val bazelRunner: BazelRunner,
        private val bspInfo: BspInfo,
) {

    fun initializeServer(supportedLanguages: Set<Language>): InitializeBuildResult {
        val languageNames = supportedLanguages.map { it.id }
        val capabilities = BazelBuildServerCapabilities(
            compileProvider = CompileProvider(languageNames),
            runProvider = RunProvider(languageNames),
            testProvider = TestProvider(languageNames),
            outputPathsProvider = true,
            dependencySourcesProvider = true,
            inverseSourcesProvider = true,
            resourcesProvider = true,
            jvmRunEnvironmentProvider = true,
            jvmTestEnvironmentProvider = true,
            workspaceLibrariesProvider = true,
            workspaceDirectoriesProvider = true,
            workspaceInvalidTargetsProvider = true,
            runWithDebugProvider = true
        )
        return InitializeBuildResult(
            Constants.NAME, Constants.VERSION, Constants.BSP_VERSION, capabilities
        )
    }

    fun workspaceTargets(project: Project): WorkspaceBuildTargetsResult {
        val buildTargets = project.modules.map(::toBuildTarget)
        return WorkspaceBuildTargetsResult(buildTargets)
    }

    fun workspaceInvalidTargets(project: Project): WorkspaceInvalidTargetsResult =
        WorkspaceInvalidTargetsResult(project.invalidTargets.map { BuildTargetIdentifier(it.value) })

    fun workspaceLibraries(project: Project): WorkspaceLibrariesResult {
        val libraries = project.libraries.values.map {
            LibraryItem(
                id = BuildTargetIdentifier(it.label),
                dependencies = it.dependencies.map { dep -> BuildTargetIdentifier(dep) },
                jars = it.outputs.map { uri -> uri.toString() },
                sourceJars = it.sources.map { uri -> uri.toString() },
            )
        }
        return WorkspaceLibrariesResult(libraries)
    }

    fun workspaceDirectories(project: Project): WorkspaceDirectoriesResult {
        val workspaceContext = workspaceContextProvider.currentWorkspaceContext()
        val directoriesSection = workspaceContext.directories

        val symlinksToExclude = computeSymlinksToExclude(project.workspaceRoot)
        val directoriesToExclude = directoriesSection.excludedValues + symlinksToExclude

        return WorkspaceDirectoriesResult(
            includedDirectories = directoriesSection.values.map { it.toDirectoryItem() },
            excludedDirectories = directoriesToExclude.map { it.toDirectoryItem() }
        )
    }

    // copied from IntelliJProjectTreeViewFix, but it will be removed with:
    // https://youtrack.jetbrains.com/issue/BAZEL-665
    private fun computeSymlinksToExclude(workspaceRoot: URI): List<Path> {
        val stableSymlinkNames = setOf("bazel-out", "bazel-testlogs", "bazel-bin")
        val workspaceRootPath = workspaceRoot.toPath()
        val sanitizedWorkspaceRootPath = workspaceRootPath
            .name
            .replace("[^A-Za-z0-9]".toRegex(), "-")
        val workspaceSymlinkNames = setOf("bazel-$sanitizedWorkspaceRootPath")

        return (stableSymlinkNames + workspaceSymlinkNames).map { workspaceRootPath.resolve(it) }
    }

    private fun Path.toDirectoryItem() =
        DirectoryItem(
            uri = this.toUri().toString()
        )

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
        return BuildTargetCapabilities().also { it.canCompile = canCompile; it.canTest = canTest; it.canRun = canRun; it.canDebug = false }
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
            project: Project, params: JvmRunEnvironmentParams, cancelChecker: CancelChecker
    ): JvmRunEnvironmentResult {
        val targets = params.targets
        val result = getJvmEnvironmentItems(project, targets, cancelChecker)
        return JvmRunEnvironmentResult(result)
    }

    fun jvmTestEnvironment(
            project: Project, params: JvmTestEnvironmentParams, cancelChecker: CancelChecker
    ): JvmTestEnvironmentResult {
        val targets = params.targets
        val result = getJvmEnvironmentItems(project, targets, cancelChecker)
        return JvmTestEnvironmentResult(result)
    }

    private fun getJvmEnvironmentItems(
            project: Project, targets: List<BuildTargetIdentifier>, cancelChecker: CancelChecker
    ): List<JvmEnvironmentItem> {
        fun extractJvmEnvironmentItem(module: Module, runtimeClasspath: List<URI>): JvmEnvironmentItem? {
            return module.javaModule?.let { javaModule ->
                JvmEnvironmentItem(
                        BspMappings.toBspId(module),
                        runtimeClasspath.map { it.toString() },
                        javaModule.jvmOps.toList(),
                        bazelPathsResolver.unresolvedWorkspaceRoot().toString(),
                        module.environmentVariables
                ).apply {
                    mainClasses = javaModule.mainClass?.let { listOf(JvmMainClass(it, javaModule.args)) }.orEmpty()
                }
            }
        }

        return targets.mapNotNull {
            val label = Label(it.uri)
            val module = project.findModule(label)
            val cqueryResult = ClasspathQuery.classPathQuery(it, cancelChecker, bspInfo, bazelRunner).runtime_classpath
            val resolvedClasspath = resolveClasspath(cqueryResult)
            module?.let { extractJvmEnvironmentItem(module, resolvedClasspath) }
        }
    }

    private fun resolveClasspath(cqueryResult: List<String>) = cqueryResult
            .map { bazelPathsResolver.resolveOutput(Paths.get(it)) }
            .filter { it.toFile().exists() } // I'm surprised this is needed, but we literally test it in e2e tests
            .map { it.toUri() }

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


    fun buildTargetPythonOptions(project: Project, params: PythonOptionsParams): PythonOptionsResult {
        val modules = BspMappings.getModules(project, params.targets)
        val items = modules.mapNotNull(::extractPythonOptionsItem)
        return PythonOptionsResult(items)
    }

    private fun extractPythonOptionsItem(module: Module): PythonOptionsItem? =
        languagePluginsService.extractPythonModule(module)?.let {
            languagePluginsService.pythonLanguagePlugin.toPythonOptionsItem(module, it)
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
