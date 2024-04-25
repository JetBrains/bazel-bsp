package org.jetbrains.bsp.bazel.server.sync

import ch.epfl.scala.bsp4j.BuildTarget
import ch.epfl.scala.bsp4j.BuildTargetCapabilities
import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import ch.epfl.scala.bsp4j.CompileProvider
import ch.epfl.scala.bsp4j.CppOptionsItem
import ch.epfl.scala.bsp4j.CppOptionsParams
import ch.epfl.scala.bsp4j.CppOptionsResult
import ch.epfl.scala.bsp4j.DependencyModule
import ch.epfl.scala.bsp4j.DependencyModuleDataKind
import ch.epfl.scala.bsp4j.DependencyModulesItem
import ch.epfl.scala.bsp4j.DependencyModulesParams
import ch.epfl.scala.bsp4j.DependencyModulesResult
import ch.epfl.scala.bsp4j.DependencySourcesItem
import ch.epfl.scala.bsp4j.DependencySourcesParams
import ch.epfl.scala.bsp4j.DependencySourcesResult
import ch.epfl.scala.bsp4j.InitializeBuildResult
import ch.epfl.scala.bsp4j.InverseSourcesParams
import ch.epfl.scala.bsp4j.InverseSourcesResult
import ch.epfl.scala.bsp4j.JavacOptionsItem
import ch.epfl.scala.bsp4j.JavacOptionsParams
import ch.epfl.scala.bsp4j.JavacOptionsResult
import ch.epfl.scala.bsp4j.JvmCompileClasspathItem
import ch.epfl.scala.bsp4j.JvmCompileClasspathParams
import ch.epfl.scala.bsp4j.JvmCompileClasspathResult
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
import ch.epfl.scala.bsp4j.RustWorkspaceParams
import ch.epfl.scala.bsp4j.RustWorkspaceResult
import ch.epfl.scala.bsp4j.ScalaMainClassesParams
import ch.epfl.scala.bsp4j.ScalaMainClassesResult
import ch.epfl.scala.bsp4j.ScalaTestClassesParams
import ch.epfl.scala.bsp4j.ScalaTestClassesResult
import ch.epfl.scala.bsp4j.ScalacOptionsItem
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
import org.jetbrains.bsp.JvmBinaryJarsItem
import org.jetbrains.bsp.JvmBinaryJarsParams
import org.jetbrains.bsp.JvmBinaryJarsResult
import org.jetbrains.bsp.LibraryItem
import org.jetbrains.bsp.WorkspaceDirectoriesResult
import org.jetbrains.bsp.WorkspaceInvalidTargetsResult
import org.jetbrains.bsp.WorkspaceLibrariesResult
import org.jetbrains.bsp.bazel.bazelrunner.BazelRunner
import org.jetbrains.bsp.bazel.commons.Constants
import org.jetbrains.bsp.bazel.server.bsp.info.BspInfo
import org.jetbrains.bsp.bazel.server.paths.BazelPathsResolver
import org.jetbrains.bsp.bazel.server.sync.languages.LanguagePluginsService
import org.jetbrains.bsp.bazel.server.sync.languages.java.IdeClasspathResolver
import org.jetbrains.bsp.bazel.server.sync.languages.java.JavaModule
import org.jetbrains.bsp.bazel.server.sync.languages.jvm.javaModule
import org.jetbrains.bsp.bazel.server.sync.languages.scala.ScalaModule
import org.jetbrains.bsp.bazel.server.sync.model.Label
import org.jetbrains.bsp.bazel.server.sync.model.Language
import org.jetbrains.bsp.bazel.server.sync.model.Module
import org.jetbrains.bsp.bazel.server.sync.model.Project
import org.jetbrains.bsp.bazel.server.sync.model.Tag
import org.jetbrains.bsp.bazel.workspacecontext.WorkspaceContextProvider
import java.net.URI
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.io.path.name
import kotlin.io.path.relativeToOrNull
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
            runWithDebugProvider = true,
            jvmBinaryJarsProvider = true,
            jvmCompileClasspathProvider = true,
        )
        return InitializeBuildResult(
            Constants.NAME, Constants.VERSION, Constants.BSP_VERSION, capabilities
        )
    }

    fun workspaceTargets(project: Project): WorkspaceBuildTargetsResult {
        val buildTargets = project.findNonExternalModules().map(::toBuildTarget)
        return WorkspaceBuildTargetsResult(buildTargets)
    }

    fun workspaceInvalidTargets(project: Project): WorkspaceInvalidTargetsResult =
        WorkspaceInvalidTargetsResult(project.invalidTargets.map { BuildTargetIdentifier(it.value) })

    fun workspaceLibraries(project: Project): WorkspaceLibrariesResult {
        val libraries = project.libraries.values.map {
            LibraryItem(
                id = BuildTargetIdentifier(it.label),
                dependencies = it.dependencies.map { dep -> BuildTargetIdentifier(dep) },
                ijars = it.interfaceJars.filter { o -> o.toPath().exists() }.map { o -> o.toString() },
                jars = it.outputs.filter { o -> o.toPath().exists() }.map { uri -> uri.toString() },
                sourceJars = it.sources.filter { o -> o.toPath().exists() }.map { uri -> uri.toString() },
            )
        }
        return WorkspaceLibrariesResult(libraries)
    }

    fun workspaceDirectories(project: Project): WorkspaceDirectoriesResult {
        val workspaceContext = workspaceContextProvider.currentWorkspaceContext()
        val directoriesSection = workspaceContext.directories

        val symlinksToExclude = computeSymlinksToExclude(project.workspaceRoot)
        val additionalDirectoriesToExclude = computeAdditionalDirectoriesToExclude()
        val directoriesToExclude = directoriesSection.excludedValues + symlinksToExclude + additionalDirectoriesToExclude

        return WorkspaceDirectoriesResult(
            includedDirectories = directoriesSection.values.map { it.toDirectoryItem() },
            excludedDirectories = directoriesToExclude.map { it.toDirectoryItem() }
        )
    }

    private fun computeSymlinksToExclude(workspaceRoot: URI): List<Path> {
        val stableSymlinkNames = setOf("bazel-out", "bazel-testlogs", "bazel-bin")
        val workspaceRootPath = workspaceRoot.toPath()
        val workspaceSymlinkNames = setOf("bazel-${workspaceRootPath.name}")

        return (stableSymlinkNames + workspaceSymlinkNames).map { workspaceRootPath.resolve(it) }
    }

    private fun computeAdditionalDirectoriesToExclude(): List<Path> =
        listOf(bspInfo.bazelBspDir())

    private fun Path.toDirectoryItem() =
        DirectoryItem(
            uri = this.toUri().toString()
        )

    private fun toBuildTarget(module: Module): BuildTarget {
        val label = BspMappings.toBspId(module)
        val dependencies =
            module.directDependencies.map(BspMappings::toBspId)
        val languages = module.languages.flatMap(Language::allNames).distinct()
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
        val canDebug = canRun || canTest // runnable and testable targets should be debuggable
        return BuildTargetCapabilities().also { it.canCompile = canCompile; it.canTest = canTest; it.canRun = canRun; it.canDebug = canDebug }
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
            val generatedSourceItems = sourceSet.generatedSources.map {
                SourceItem(
                    BspMappings.toBspUri(it),
                    SourceItemKind.FILE,
                    true
                )
            }
            val sourceRoots = sourceSet.sourceRoots.map(BspMappings::toBspUri)
            val sourcesItem = SourcesItem(BspMappings.toBspId(module), sourceItems + generatedSourceItems)
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
        project: Project, inverseSourcesParams: InverseSourcesParams, cancelChecker: CancelChecker
    ): InverseSourcesResult {
        val documentUri = BspMappings.toUri(inverseSourcesParams.textDocument)
        val documentRelativePath = documentUri
            .toPath()
            .relativeToOrNull(project.workspaceRoot.toPath()) ?: throw RuntimeException("File path outside of project root")
        return InverseSourcesQuery.inverseSourcesQuery(documentRelativePath, bazelRunner, project.bazelRelease, cancelChecker)
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

    fun jvmCompileClasspath(
            project: Project, params: JvmCompileClasspathParams, cancelChecker: CancelChecker
    ): JvmCompileClasspathResult {
        val items = params.targets.collectClasspathForTargetsAndApply(project, cancelChecker) { module, ideClasspath ->
            JvmCompileClasspathItem(BspMappings.toBspId(module), ideClasspath.map { it.toString() })
        }
        return JvmCompileClasspathResult(items)
    }

    private fun getJvmEnvironmentItems(
            project: Project, targets: List<BuildTargetIdentifier>, cancelChecker: CancelChecker
    ): List<JvmEnvironmentItem> {
        fun extractJvmEnvironmentItem(module: Module, runtimeClasspath: List<URI>): JvmEnvironmentItem? =
            module.javaModule?.let { javaModule ->
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

        return targets.mapNotNull {
            val label = Label(it.uri)
            val module = project.findModule(label)
            val cqueryResult = ClasspathQuery.classPathQuery(it, cancelChecker, bspInfo, bazelRunner).runtime_classpath
            val resolvedClasspath = resolveClasspath(cqueryResult)
            module?.let { extractJvmEnvironmentItem(module, resolvedClasspath) }
        }
    }

    fun jvmBinaryJars(project: Project, params: JvmBinaryJarsParams): JvmBinaryJarsResult {
        fun toJvmBinaryJarsItem(module: Module): JvmBinaryJarsItem? =
            module.javaModule?.let { javaModule ->
                val jars = javaModule.binaryOutputs.map { it.toString() }
                JvmBinaryJarsItem(BspMappings.toBspId(module), jars)
            }

        val jvmBinaryJarsItems = params.targets.mapNotNull { target ->
            val label = Label(target.uri)
            val module = project.findModule(label)
            module?.let { toJvmBinaryJarsItem(it) }
        }
        return JvmBinaryJarsResult(jvmBinaryJarsItems)
    }

    fun buildTargetJavacOptions(project: Project, params: JavacOptionsParams, cancelChecker: CancelChecker): JavacOptionsResult {
        val items = params.targets.collectClasspathForTargetsAndApply(project, cancelChecker) { module, ideClasspath ->
            module.javaModule?.let { toJavacOptionsItem(module, it, ideClasspath) }
        }
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
            params: ScalacOptionsParams,
            cancelChecker: CancelChecker
    ): ScalacOptionsResult {
        val items = params.targets.collectClasspathForTargetsAndApply(project, cancelChecker) { module, ideClasspath ->
            toScalacOptionsItem(module, ideClasspath)
        }
        return ScalacOptionsResult(items)
    }

    private fun <T> List<BuildTargetIdentifier>.collectClasspathForTargetsAndApply(
        project: Project,
        cancelChecker: CancelChecker,
        mapper: (Module, List<URI>) -> T?
    ): List<T> =
        this.mapNotNull { project.findModule(Label(it.uri)) }
            .mapNotNull { mapper(it, readIdeClasspath(it.label, cancelChecker)) }

    private fun readIdeClasspath(targetLabel: Label, cancelChecker: CancelChecker): List<URI> {
        val targetIdentifier = BspMappings.toBspId(targetLabel)
        val classPathFromQuery = ClasspathQuery.classPathQuery(targetIdentifier, cancelChecker, bspInfo, bazelRunner)
        val ideClasspath = IdeClasspathResolver.resolveIdeClasspath(
            label = targetLabel,
            bazelPathsResolver = bazelPathsResolver,
            runtimeClasspath = resolveClasspath(classPathFromQuery.runtime_classpath),
            compileClasspath = resolveClasspath(classPathFromQuery.compile_classpath))
        return ideClasspath
    }

    private fun resolveClasspath(cqueryResult: List<String>) = cqueryResult
        .map { bazelPathsResolver.resolveOutput(Paths.get(it)) }
        .filter { it.toFile().exists() } // I'm surprised this is needed, but we literally test it in e2e tests
        .map { it.toUri() }

    private fun toScalacOptionsItem(module: Module, ideClasspath: List<URI>): ScalacOptionsItem? =
        (module.languageData as? ScalaModule)?.let { scalaModule ->
            scalaModule.javaModule?.let { javaModule ->
                val javacOptions = toJavacOptionsItem(module, javaModule, ideClasspath)
                ScalacOptionsItem(
                    javacOptions.target,
                    scalaModule.scalacOpts,
                    javacOptions.classpath,
                    javacOptions.classDirectory
                )
            }
        }

    private fun toJavacOptionsItem(module: Module, javaModule: JavaModule, ideClasspath: List<URI>): JavacOptionsItem =
        JavacOptionsItem(
            BspMappings.toBspId(module),
            javaModule.javacOpts.toList(),
            ideClasspath.map { it.toString() },
            javaModule.mainOutput.toString()
        )

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

    fun buildDependencyModules(project: Project, params: DependencyModulesParams): DependencyModulesResult {
        val targetSet = params.targets.toSet()
        val dependencyModulesItems = project.modules.filter { targetSet.contains(BuildTargetIdentifier(it.label.value)) }.map { module ->
            val buildTargetId = BuildTargetIdentifier(module.label.value)
            val moduleItems = DependencyMapper.allModuleDependencies(project, module).flatMap { libraryDep ->
                if (libraryDep.outputs.isNotEmpty()) {
                    val mavenDependencyModule = DependencyMapper.extractMavenDependencyInfo(libraryDep)
                    val dependencyModule = DependencyModule(libraryDep.label, mavenDependencyModule?.version ?: "")
                    if (mavenDependencyModule != null) {
                        dependencyModule.data = mavenDependencyModule
                        dependencyModule.dataKind = DependencyModuleDataKind.MAVEN
                    }
                    listOf(dependencyModule)
                } else emptyList()
            }
            DependencyModulesItem(buildTargetId, moduleItems)
        }
        return DependencyModulesResult(dependencyModulesItems)
    }

    fun rustWorkspace(
        project: Project,
        params: RustWorkspaceParams
    ): RustWorkspaceResult {
        val allRustModules = project.findModulesByLanguage(Language.RUST)
        val requestedModules = BspMappings.getModules(project, params.targets)
                .filter { Language.RUST in it.languages }
        val toRustWorkspaceResult = languagePluginsService.rustLanguagePlugin::toRustWorkspaceResult

        return toRustWorkspaceResult(requestedModules, allRustModules)
    }
}
