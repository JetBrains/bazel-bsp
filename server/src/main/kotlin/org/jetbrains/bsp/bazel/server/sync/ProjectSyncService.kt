package org.jetbrains.bsp.bazel.server.sync

import ch.epfl.scala.bsp4j.CppOptionsParams
import ch.epfl.scala.bsp4j.CppOptionsResult
import ch.epfl.scala.bsp4j.DependencyModulesParams
import ch.epfl.scala.bsp4j.DependencyModulesResult
import ch.epfl.scala.bsp4j.DependencySourcesParams
import ch.epfl.scala.bsp4j.DependencySourcesResult
import ch.epfl.scala.bsp4j.InitializeBuildResult
import ch.epfl.scala.bsp4j.InverseSourcesParams
import ch.epfl.scala.bsp4j.InverseSourcesResult
import ch.epfl.scala.bsp4j.JavacOptionsParams
import ch.epfl.scala.bsp4j.JavacOptionsResult
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
import ch.epfl.scala.bsp4j.ScalaMainClassesParams
import ch.epfl.scala.bsp4j.ScalaMainClassesResult
import ch.epfl.scala.bsp4j.ScalaTestClassesParams
import ch.epfl.scala.bsp4j.ScalaTestClassesResult
import ch.epfl.scala.bsp4j.ScalacOptionsParams
import ch.epfl.scala.bsp4j.ScalacOptionsResult
import ch.epfl.scala.bsp4j.SourcesParams
import ch.epfl.scala.bsp4j.SourcesResult
import ch.epfl.scala.bsp4j.WorkspaceBuildTargetsResult
import org.eclipse.lsp4j.jsonrpc.CancelChecker
import org.jetbrains.bsp.bazel.server.sync.model.Language

/** A facade for all project sync related methods  */
class ProjectSyncService(private val bspMapper: BspProjectMapper, private val projectProvider: ProjectProvider) {

  fun initialize(cancelChecker: CancelChecker): InitializeBuildResult =
    bspMapper.initializeServer(Language.all())


  // TODO https://youtrack.jetbrains.com/issue/BAZEL-639
  // We might consider doing the actual project reload in this endpoint
  // i.e. just run projectProvider.refreshAndGet() and in workspaceBuildTargets
  // just run projectProvider.get() although current approach seems to work
  // correctly, so I am not changing anything.
  fun workspaceReload(cancelChecker: CancelChecker): Any = Any()

  fun workspaceBuildTargets(cancelChecker: CancelChecker): WorkspaceBuildTargetsResult {
    val project = projectProvider.refreshAndGet(cancelChecker)
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
    return bspMapper.inverseSources(project, inverseSourcesParams)
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
    return bspMapper.jvmRunEnvironment(project, params)
  }

  fun jvmTestEnvironment(cancelChecker: CancelChecker, params: JvmTestEnvironmentParams): JvmTestEnvironmentResult {
    val project = projectProvider.get(cancelChecker)
    return bspMapper.jvmTestEnvironment(project, params)
  }

  fun buildTargetJavacOptions(cancelChecker: CancelChecker, params: JavacOptionsParams): JavacOptionsResult {
    val project = projectProvider.get(cancelChecker)
    return bspMapper.buildTargetJavacOptions(project, params)
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
    return bspMapper.buildTargetScalacOptions(project, params)
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
    // TODO https://youtrack.jetbrains.com/issue/BAZEL-616
    return DependencyModulesResult(emptyList())
  }
}
