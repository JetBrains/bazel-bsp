package org.jetbrains.bsp.bazel.server.sync

import ch.epfl.scala.bsp4j.BuildClientCapabilities
import ch.epfl.scala.bsp4j.CppOptionsParams
import ch.epfl.scala.bsp4j.CppOptionsResult
import ch.epfl.scala.bsp4j.DependencyModulesParams
import ch.epfl.scala.bsp4j.DependencyModulesResult
import ch.epfl.scala.bsp4j.DependencySourcesParams
import ch.epfl.scala.bsp4j.DependencySourcesResult
import ch.epfl.scala.bsp4j.InitializeBuildParams
import ch.epfl.scala.bsp4j.InitializeBuildResult
import ch.epfl.scala.bsp4j.InverseSourcesParams
import ch.epfl.scala.bsp4j.InverseSourcesResult
import ch.epfl.scala.bsp4j.JavacOptionsParams
import ch.epfl.scala.bsp4j.JavacOptionsResult
import ch.epfl.scala.bsp4j.JvmCompileClasspathParams
import ch.epfl.scala.bsp4j.JvmCompileClasspathResult
import ch.epfl.scala.bsp4j.JvmRunEnvironmentParams
import ch.epfl.scala.bsp4j.JvmRunEnvironmentResult
import ch.epfl.scala.bsp4j.JvmTestEnvironmentParams
import ch.epfl.scala.bsp4j.JvmTestEnvironmentResult
import ch.epfl.scala.bsp4j.OutputPathsParams
import ch.epfl.scala.bsp4j.OutputPathsResult
import ch.epfl.scala.bsp4j.PythonOptionsParams
import ch.epfl.scala.bsp4j.PythonOptionsResult
import ch.epfl.scala.bsp4j.ResourcesParams
import ch.epfl.scala.bsp4j.ResourcesResult
import ch.epfl.scala.bsp4j.RustWorkspaceParams
import ch.epfl.scala.bsp4j.RustWorkspaceResult
import ch.epfl.scala.bsp4j.ScalaMainClassesParams
import ch.epfl.scala.bsp4j.ScalaMainClassesResult
import ch.epfl.scala.bsp4j.ScalaTestClassesParams
import ch.epfl.scala.bsp4j.ScalaTestClassesResult
import ch.epfl.scala.bsp4j.ScalacOptionsParams
import ch.epfl.scala.bsp4j.ScalacOptionsResult
import ch.epfl.scala.bsp4j.SourcesParams
import ch.epfl.scala.bsp4j.SourcesResult
import ch.epfl.scala.bsp4j.WorkspaceBuildTargetsResult
import com.google.gson.JsonObject
import org.eclipse.lsp4j.jsonrpc.CancelChecker
import org.jetbrains.bsp.JvmBinaryJarsParams
import org.jetbrains.bsp.JvmBinaryJarsResult
import org.jetbrains.bsp.WorkspaceDirectoriesResult
import org.jetbrains.bsp.WorkspaceInvalidTargetsResult
import org.jetbrains.bsp.WorkspaceLibrariesResult
import org.jetbrains.bsp.bazel.server.benchmark.TelemetryConfig
import org.jetbrains.bsp.bazel.server.benchmark.setupTelemetry
import org.jetbrains.bsp.bazel.server.model.Language

/** A facade for all project sync related methods  */
class ProjectSyncService(
  private val bspMapper: BspProjectMapper,
  private val projectProvider: ProjectProvider,
  private val telemetryConfig: TelemetryConfig,
) {

  private lateinit var clientCapabilities: BuildClientCapabilities

  fun initialize(cancelChecker: CancelChecker, initializeBuildParams: InitializeBuildParams): InitializeBuildResult {
    this.clientCapabilities = initializeBuildParams.capabilities
    setupTelemetry(initializeBuildParams)
    return bspMapper.initializeServer(Language.all())
  }

  private fun setupTelemetry(initializeBuildParams: InitializeBuildParams) {
    val openTelemetryEndpoint =
      (initializeBuildParams.data as? JsonObject)?.get("openTelemetryEndpoint")?.asString
    val telemetryConfigWithHttp = telemetryConfig.copy(openTelemetryEndpoint = openTelemetryEndpoint)
    setupTelemetry(telemetryConfigWithHttp)
  }


  // TODO https://youtrack.jetbrains.com/issue/BAZEL-639
  // We might consider doing the actual project reload in this endpoint
  // i.e. just run projectProvider.refreshAndGet() and in workspaceBuildTargets
  // just run projectProvider.get() although current approach seems to work
  // correctly, so I am not changing anything.
  fun workspaceReload(cancelChecker: CancelChecker): Any = Any()

  fun workspaceBuildTargets(cancelChecker: CancelChecker, build: Boolean): WorkspaceBuildTargetsResult {
    val project = projectProvider.refreshAndGet(cancelChecker, build = build)
    return bspMapper.workspaceTargets(project)
  }

  fun workspaceBuildLibraries(cancelChecker: CancelChecker): WorkspaceLibrariesResult {
    val project = projectProvider.get(cancelChecker)
    return bspMapper.workspaceLibraries(project)
  }

  fun workspaceDirectories(cancelChecker: CancelChecker): WorkspaceDirectoriesResult {
    val project = projectProvider.get(cancelChecker)
    return bspMapper.workspaceDirectories(project)
  }

  fun workspaceInvalidTargets(cancelChecker: CancelChecker): WorkspaceInvalidTargetsResult {
    val project = projectProvider.get(cancelChecker)
    return bspMapper.workspaceInvalidTargets(project)
  }

  fun buildTargetSources(cancelChecker: CancelChecker, sourcesParams: SourcesParams): SourcesResult {
    val project = projectProvider.get(cancelChecker)
    return bspMapper.sources(project, sourcesParams)
  }

  fun buildTargetResources(cancelChecker: CancelChecker, resourcesParams: ResourcesParams): ResourcesResult {
    val project = projectProvider.get(cancelChecker)
    return bspMapper.resources(project, resourcesParams)
  }

  fun buildTargetInverseSources(
    cancelChecker: CancelChecker,
    inverseSourcesParams: InverseSourcesParams
  ): InverseSourcesResult {
    val project = projectProvider.get(cancelChecker)
    return bspMapper.inverseSources(project, inverseSourcesParams, cancelChecker)
  }

  fun buildTargetDependencySources(
    cancelChecker: CancelChecker,
    dependencySourcesParams: DependencySourcesParams
  ): DependencySourcesResult {
    val project = projectProvider.get(cancelChecker)
    return bspMapper.dependencySources(project, dependencySourcesParams)
  }

  fun buildTargetOutputPaths(cancelChecker: CancelChecker, params: OutputPathsParams): OutputPathsResult {
    val project = projectProvider.get(cancelChecker)
    return bspMapper.outputPaths(project, params)
  }

  fun jvmRunEnvironment(cancelChecker: CancelChecker, params: JvmRunEnvironmentParams): JvmRunEnvironmentResult {
    val project = projectProvider.get(cancelChecker)
    return bspMapper.jvmRunEnvironment(project, params, cancelChecker)
  }

  fun jvmTestEnvironment(cancelChecker: CancelChecker, params: JvmTestEnvironmentParams): JvmTestEnvironmentResult {
    val project = projectProvider.get(cancelChecker)
    return bspMapper.jvmTestEnvironment(project, params, cancelChecker)
  }

  fun jvmBinaryJars(cancelChecker: CancelChecker, params: JvmBinaryJarsParams): JvmBinaryJarsResult {
    val project = projectProvider.get(cancelChecker)
    return bspMapper.jvmBinaryJars(project, params)
  }

  fun jvmCompileClasspath(cancelChecker: CancelChecker, params: JvmCompileClasspathParams): JvmCompileClasspathResult {
    val project = projectProvider.get(cancelChecker)
    return bspMapper.jvmCompileClasspath(project, params, cancelChecker)
  }

  fun buildTargetJavacOptions(cancelChecker: CancelChecker, params: JavacOptionsParams): JavacOptionsResult {
    val project = projectProvider.get(cancelChecker)
    val includeClasspath = clientCapabilities.jvmCompileClasspathReceiver == false
    return bspMapper.buildTargetJavacOptions(project, params, includeClasspath, cancelChecker)
  }

  fun buildTargetCppOptions(cancelChecker: CancelChecker, params: CppOptionsParams): CppOptionsResult {
    val project = projectProvider.get(cancelChecker)
    return bspMapper.buildTargetCppOptions(project, params)
  }

  fun buildTargetPythonOptions(cancelChecker: CancelChecker, params: PythonOptionsParams): PythonOptionsResult {
    val project = projectProvider.get(cancelChecker)
    return bspMapper.buildTargetPythonOptions(project, params)
  }

  fun buildTargetScalacOptions(cancelChecker: CancelChecker, params: ScalacOptionsParams): ScalacOptionsResult {
    val project = projectProvider.get(cancelChecker)
    val includeClasspath = clientCapabilities.jvmCompileClasspathReceiver == false
    return bspMapper.buildTargetScalacOptions(project, params, includeClasspath, cancelChecker)
  }

  fun buildTargetScalaTestClasses(
    cancelChecker: CancelChecker,
    params: ScalaTestClassesParams
  ): ScalaTestClassesResult {
    val project = projectProvider.get(cancelChecker)
    return bspMapper.buildTargetScalaTestClasses(project, params)
  }

  fun buildTargetScalaMainClasses(
    cancelChecker: CancelChecker,
    params: ScalaMainClassesParams
  ): ScalaMainClassesResult {
    val project = projectProvider.get(cancelChecker)
    return bspMapper.buildTargetScalaMainClasses(project, params)
  }

  fun buildTargetDependencyModules(
    cancelChecker: CancelChecker,
    params: DependencyModulesParams
  ): DependencyModulesResult {
    val project = projectProvider.get(cancelChecker)
    return bspMapper.buildDependencyModules(project, params)
  }

  fun rustWorkspace(
    cancelChecker: CancelChecker,
    params: RustWorkspaceParams
  ): RustWorkspaceResult {
    val project = projectProvider.get(cancelChecker)
    return bspMapper.rustWorkspace(project, params)
  }
}

