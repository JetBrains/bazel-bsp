package org.jetbrains.bsp.bazel.server.bsp

import com.jetbrains.bsp.bsp4kt.BuildServer
import com.jetbrains.bsp.bsp4kt.CleanCacheParams
import com.jetbrains.bsp.bsp4kt.CleanCacheResult
import com.jetbrains.bsp.bsp4kt.CompileParams
import com.jetbrains.bsp.bsp4kt.CompileResult
import com.jetbrains.bsp.bsp4kt.CppBuildServer
import com.jetbrains.bsp.bsp4kt.CppOptionsParams
import com.jetbrains.bsp.bsp4kt.CppOptionsResult
import com.jetbrains.bsp.bsp4kt.DebugSessionAddress
import com.jetbrains.bsp.bsp4kt.DebugSessionParams
import com.jetbrains.bsp.bsp4kt.DependencyModulesParams
import com.jetbrains.bsp.bsp4kt.DependencyModulesResult
import com.jetbrains.bsp.bsp4kt.DependencySourcesParams
import com.jetbrains.bsp.bsp4kt.DependencySourcesResult
import com.jetbrains.bsp.bsp4kt.InitializeBuildParams
import com.jetbrains.bsp.bsp4kt.InitializeBuildResult
import com.jetbrains.bsp.bsp4kt.InverseSourcesParams
import com.jetbrains.bsp.bsp4kt.InverseSourcesResult
import com.jetbrains.bsp.bsp4kt.JavaBuildServer
import com.jetbrains.bsp.bsp4kt.JavacOptionsParams
import com.jetbrains.bsp.bsp4kt.JavacOptionsResult
import com.jetbrains.bsp.bsp4kt.JvmBuildServer
import com.jetbrains.bsp.bsp4kt.JvmRunEnvironmentParams
import com.jetbrains.bsp.bsp4kt.JvmRunEnvironmentResult
import com.jetbrains.bsp.bsp4kt.JvmTestEnvironmentParams
import com.jetbrains.bsp.bsp4kt.JvmTestEnvironmentResult
import com.jetbrains.bsp.bsp4kt.OutputPathsParams
import com.jetbrains.bsp.bsp4kt.OutputPathsResult
import com.jetbrains.bsp.bsp4kt.PythonBuildServer
import com.jetbrains.bsp.bsp4kt.PythonOptionsParams
import com.jetbrains.bsp.bsp4kt.PythonOptionsResult
import com.jetbrains.bsp.bsp4kt.ResourcesParams
import com.jetbrains.bsp.bsp4kt.ResourcesResult
import com.jetbrains.bsp.bsp4kt.RunParams
import com.jetbrains.bsp.bsp4kt.RunResult
import com.jetbrains.bsp.bsp4kt.ScalaBuildServer
import com.jetbrains.bsp.bsp4kt.ScalaMainClassesParams
import com.jetbrains.bsp.bsp4kt.ScalaMainClassesResult
import com.jetbrains.bsp.bsp4kt.ScalaTestClassesParams
import com.jetbrains.bsp.bsp4kt.ScalaTestClassesResult
import com.jetbrains.bsp.bsp4kt.ScalacOptionsParams
import com.jetbrains.bsp.bsp4kt.ScalacOptionsResult
import com.jetbrains.bsp.bsp4kt.SourcesParams
import com.jetbrains.bsp.bsp4kt.SourcesResult
import com.jetbrains.bsp.bsp4kt.TestParams
import com.jetbrains.bsp.bsp4kt.TestResult
import com.jetbrains.bsp.bsp4kt.WorkspaceBuildTargetsResult
import com.jetbrains.jsonrpc4kt.CancelChecker
import org.jetbrains.bsp.bazel.server.sync.BazelBuildServer
import org.jetbrains.bsp.bazel.server.sync.WorkspaceLibrariesResult
import java.util.concurrent.CompletableFuture
import java.util.function.Supplier

class BspServerApi(bazelServicesBuilder: Supplier<BazelServices>) : BuildServer, JvmBuildServer,
  ScalaBuildServer, JavaBuildServer, CppBuildServer, BazelBuildServer, PythonBuildServer {
  private val bazelServices = bazelServicesBuilder.get()
  private val serverLifetime = bazelServices.serverLifetime
  private val runner = bazelServices.bspRequestsRunner
  private val projectSyncService = bazelServices.projectSyncService
  private val executeService = bazelServices.executeService

  override fun buildInitialize(
    params: InitializeBuildParams,
  ): CompletableFuture<InitializeBuildResult> {
    return runner.handleRequest(
      "buildInitialize",
      { cancelChecker: CancelChecker -> projectSyncService.initialize(cancelChecker) }) { methodName: String ->
      runner.serverIsNotFinished(
        methodName
      )
    }
  }

  override fun onBuildInitialized() {
    runner.handleNotification("onBuildInitialized") { serverLifetime.setInitializedComplete() }
  }

  override fun buildShutdown(): CompletableFuture<Unit> {
    return runner.handleRequest<Unit>(
      "buildShutdown",
      {
        serverLifetime.setFinishedComplete()
        Any()
      }) { methodName: String -> runner.serverIsInitialized(methodName) }
  }

  override fun onBuildExit() {
    runner.handleNotification("onBuildExit") { serverLifetime.forceFinish() }
  }

  override fun workspaceBuildTargets(): CompletableFuture<WorkspaceBuildTargetsResult> {
    return runner.handleRequest("workspaceBuildTargets") { cancelChecker: CancelChecker ->
      projectSyncService.workspaceBuildTargets(
        cancelChecker
      )
    }
  }

  override fun workspaceReload(): CompletableFuture<Unit> {
    return runner.handleRequest("workspaceReload") { cancelChecker: CancelChecker ->
      projectSyncService.workspaceReload(
        cancelChecker
      )
    }
  }

  override fun buildTargetSources(params: SourcesParams): CompletableFuture<SourcesResult> {
    return runner.handleRequest(
      "buildTargetSources",
      { cancelChecker: CancelChecker, sourcesParams: SourcesParams? ->
        projectSyncService.buildTargetSources(
          cancelChecker,
          sourcesParams
        )
      },
      params
    )
  }

  override fun buildTargetInverseSources(
    params: InverseSourcesParams,
  ): CompletableFuture<InverseSourcesResult> {
    return runner.handleRequest(
      "buildTargetInverseSources",
      { cancelChecker: CancelChecker, inverseSourcesParams: InverseSourcesParams? ->
        projectSyncService.buildTargetInverseSources(
          cancelChecker,
          inverseSourcesParams
        )
      },
      params
    )
  }

  override fun buildTargetDependencySources(
    params: DependencySourcesParams,
  ): CompletableFuture<DependencySourcesResult> {
    return runner.handleRequest(
      "buildTargetDependencySources",
      { cancelChecker: CancelChecker, dependencySourcesParams: DependencySourcesParams? ->
        projectSyncService.buildTargetDependencySources(
          cancelChecker,
          dependencySourcesParams
        )
      },
      params
    )
  }

  override fun buildTargetResources(params: ResourcesParams): CompletableFuture<ResourcesResult> {
    return runner.handleRequest(
      "buildTargetResources",
      { cancelChecker: CancelChecker, resourcesParams: ResourcesParams? ->
        projectSyncService.buildTargetResources(
          cancelChecker,
          resourcesParams
        )
      },
      params
    )
  }

  override fun buildTargetCompile(params: CompileParams): CompletableFuture<CompileResult> {
    return runner.handleRequest(
      "buildTargetCompile",
      { cancelChecker: CancelChecker, params: CompileParams -> executeService.compile(cancelChecker, params) },
      params
    )
  }

  override fun buildTargetTest(params: TestParams): CompletableFuture<TestResult> {
    return runner.handleRequest(
      "buildTargetTest",
      { cancelChecker: CancelChecker, params: TestParams -> executeService.test(cancelChecker, params) },
      params
    )
  }

  override fun buildTargetRun(params: RunParams): CompletableFuture<RunResult> {
    return runner.handleRequest(
      "buildTargetRun",
      { cancelChecker: CancelChecker, params: RunParams -> executeService.run(cancelChecker, params) },
      params
    )
  }

  override fun buildTargetCleanCache(params: CleanCacheParams): CompletableFuture<CleanCacheResult> {
    return runner.handleRequest(
      "buildTargetCleanCache",
      { cancelChecker: CancelChecker, params: CleanCacheParams? -> executeService.clean(cancelChecker, params) },
      params
    )
  }

  override fun buildTargetDependencyModules(
    params: DependencyModulesParams,
  ): CompletableFuture<DependencyModulesResult> {
    return runner.handleRequest(
      "buildTargetDependencyModules",
      { cancelChecker: CancelChecker, params: DependencyModulesParams? ->
        projectSyncService.buildTargetDependencyModules(
          cancelChecker,
          params
        )
      },
      params
    )
  }

  override fun debugSessionStart(params: DebugSessionParams): CompletableFuture<DebugSessionAddress> {
    // TODO: https://youtrack.jetbrains.com/issue/BAZEL-239
    return CompletableFuture.failedFuture(Exception("This endpoint is not implemented yet"))
  }

  override fun buildTargetOutputPaths(params: OutputPathsParams): CompletableFuture<OutputPathsResult> {
    return runner.handleRequest(
      "buildTargetOutputPaths",
      { cancelChecker: CancelChecker, params: OutputPathsParams? ->
        projectSyncService.buildTargetOutputPaths(
          cancelChecker,
          params
        )
      },
      params
    )
  }

  override fun buildTargetScalacOptions(
    params: ScalacOptionsParams,
  ): CompletableFuture<ScalacOptionsResult> {
    return runner.handleRequest(
      "buildTargetScalacOptions",
      { cancelChecker: CancelChecker, params: ScalacOptionsParams? ->
        projectSyncService.buildTargetScalacOptions(
          cancelChecker,
          params
        )
      },
      params
    )
  }

  override fun buildTargetScalaTestClasses(
    params: ScalaTestClassesParams,
  ): CompletableFuture<ScalaTestClassesResult> {
    return runner.handleRequest(
      "buildTargetScalaTestClasses",
      { cancelChecker: CancelChecker, params: ScalaTestClassesParams? ->
        projectSyncService.buildTargetScalaTestClasses(
          cancelChecker,
          params
        )
      },
      params
    )
  }

  override fun buildTargetScalaMainClasses(
    params: ScalaMainClassesParams,
  ): CompletableFuture<ScalaMainClassesResult> {
    return runner.handleRequest(
      "buildTargetScalaMainClasses",
      { cancelChecker: CancelChecker, params: ScalaMainClassesParams? ->
        projectSyncService.buildTargetScalaMainClasses(
          cancelChecker,
          params
        )
      },
      params
    )
  }

  override fun buildTargetJavacOptions(
    javacOptionsParams: JavacOptionsParams,
  ): CompletableFuture<JavacOptionsResult> {
    return runner.handleRequest(
      "buildTargetJavacOptions",
      { cancelChecker: CancelChecker, params: JavacOptionsParams? ->
        projectSyncService.buildTargetJavacOptions(
          cancelChecker,
          params
        )
      },
      javacOptionsParams
    )
  }

  override fun buildTargetCppOptions(params: CppOptionsParams): CompletableFuture<CppOptionsResult> {
    return runner.handleRequest(
      "buildTargetCppOptions",
      { cancelChecker: CancelChecker, params: CppOptionsParams? ->
        projectSyncService.buildTargetCppOptions(
          cancelChecker,
          params
        )
      },
      params
    )
  }

  override fun buildTargetPythonOptions(
    params: PythonOptionsParams,
  ): CompletableFuture<PythonOptionsResult> {
    return runner.handleRequest(
      "buildTargetPythonOptions",
      { cancelChecker: CancelChecker, params: PythonOptionsParams? ->
        projectSyncService.buildTargetPythonOptions(
          cancelChecker,
          params
        )
      },
      params
    )
  }

  override fun buildTargetJvmRunEnvironment(
    params: JvmRunEnvironmentParams,
  ): CompletableFuture<JvmRunEnvironmentResult> {
    return runner.handleRequest(
      "jvmRunEnvironment",
      { cancelChecker: CancelChecker, params: JvmRunEnvironmentParams? ->
        projectSyncService.jvmRunEnvironment(
          cancelChecker,
          params
        )
      },
      params
    )
  }

  override fun buildTargetJvmTestEnvironment(
    params: JvmTestEnvironmentParams,
  ): CompletableFuture<JvmTestEnvironmentResult> {
    return runner.handleRequest(
      "jvmTestEnvironment",
      { cancelChecker: CancelChecker, params: JvmTestEnvironmentParams? ->
        projectSyncService.jvmTestEnvironment(
          cancelChecker,
          params
        )
      },
      params
    )
  }

  override fun workspaceLibraries(): CompletableFuture<WorkspaceLibrariesResult> {
    return runner.handleRequest("libraries") { cancelChecker: CancelChecker ->
      projectSyncService.workspaceBuildLibraries(
        cancelChecker
      )
    }
  }
}
