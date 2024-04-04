package org.jetbrains.bsp.bazel.server.bsp

import ch.epfl.scala.bsp4j.BuildClient
import ch.epfl.scala.bsp4j.CleanCacheParams
import ch.epfl.scala.bsp4j.CleanCacheResult
import ch.epfl.scala.bsp4j.CompileParams
import ch.epfl.scala.bsp4j.CompileResult
import ch.epfl.scala.bsp4j.CppOptionsParams
import ch.epfl.scala.bsp4j.CppOptionsResult
import ch.epfl.scala.bsp4j.DebugSessionAddress
import ch.epfl.scala.bsp4j.DebugSessionParams
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
import ch.epfl.scala.bsp4j.ReadParams
import ch.epfl.scala.bsp4j.ResourcesParams
import ch.epfl.scala.bsp4j.ResourcesResult
import ch.epfl.scala.bsp4j.RunParams
import ch.epfl.scala.bsp4j.RunResult
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
import ch.epfl.scala.bsp4j.TestParams
import ch.epfl.scala.bsp4j.TestResult
import ch.epfl.scala.bsp4j.WorkspaceBuildTargetsResult
import org.eclipse.lsp4j.jsonrpc.CancelChecker
import org.jetbrains.bsp.JoinedBuildServer
import org.jetbrains.bsp.JvmBinaryJarsParams
import org.jetbrains.bsp.JvmBinaryJarsResult
import org.jetbrains.bsp.MobileInstallParams
import org.jetbrains.bsp.MobileInstallResult
import org.jetbrains.bsp.RunWithDebugParams
import org.jetbrains.bsp.WorkspaceDirectoriesResult
import org.jetbrains.bsp.WorkspaceInvalidTargetsResult
import org.jetbrains.bsp.WorkspaceLibrariesResult
import org.jetbrains.bsp.bazel.server.sync.ExecuteService
import org.jetbrains.bsp.bazel.server.sync.ProjectSyncService
import java.util.concurrent.CompletableFuture

class BspServerApi(private val bazelServicesBuilder: (BuildClient) -> BazelServices) : JoinedBuildServer {

  private lateinit var serverLifetime: BazelBspServerLifetime
  private lateinit var runner: BspRequestsRunner
  private lateinit var projectSyncService: ProjectSyncService
  private lateinit var executeService: ExecuteService

  fun init(client: BuildClient) {
    val serverContainer = bazelServicesBuilder(client)

    this.serverLifetime = serverContainer.serverLifetime
    this.runner = serverContainer.bspRequestsRunner
    this.projectSyncService = serverContainer.projectSyncService
    this.executeService = serverContainer.executeService
  }

  override fun buildInitialize(
    initializeBuildParams: InitializeBuildParams
  ): CompletableFuture<InitializeBuildResult> {
    return runner.handleRequest("buildInitialize", { cancelChecker: CancelChecker ->
      projectSyncService.initialize(
        cancelChecker
      )
    }, { methodName: String ->
      runner.serverIsNotFinished(
        methodName
      )
    })
  }

  override fun onBuildInitialized() {
    runner.handleNotification("onBuildInitialized") { serverLifetime.initialize() }
  }

  override fun buildShutdown(): CompletableFuture<Any> {
    return runner.handleRequest<Any>("buildShutdown", {
      serverLifetime.finish()
      Any()
    }, { methodName: String ->
      runner.serverIsInitialized(
        methodName
      )
    })
  }

  override fun onBuildExit() {
    runner.handleNotification("onBuildExit") { serverLifetime.forceFinish() }
  }

  override fun workspaceBuildTargets(): CompletableFuture<WorkspaceBuildTargetsResult> {
    return runner.handleRequest("workspaceBuildTargets") { cancelChecker: CancelChecker ->
      projectSyncService.workspaceBuildTargets(
        cancelChecker, build = false
      )
    }
  }

  override fun workspaceBuildAndGetBuildTargets(): CompletableFuture<WorkspaceBuildTargetsResult> {
    return runner.handleRequest("workspaceBuildAndGetBuildTargets") { cancelChecker: CancelChecker ->
      projectSyncService.workspaceBuildTargets(
        cancelChecker, true
      )
    }
  }

  override fun workspaceReload(): CompletableFuture<Any> {
    return runner.handleRequest("workspaceReload") { cancelChecker: CancelChecker ->
      projectSyncService.workspaceReload(
        cancelChecker
      )
    }
  }

  override fun buildTargetSources(params: SourcesParams): CompletableFuture<SourcesResult> {
    return runner.handleRequest(
      "buildTargetSources", { cancelChecker: CancelChecker, sourcesParams: SourcesParams ->
        projectSyncService.buildTargetSources(
          cancelChecker, sourcesParams
        )
      }, params
    )
  }

  override fun buildTargetInverseSources(
    params: InverseSourcesParams
  ): CompletableFuture<InverseSourcesResult> {
    return runner.handleRequest(
      "buildTargetInverseSources", { cancelChecker: CancelChecker, inverseSourcesParams: InverseSourcesParams ->
        projectSyncService.buildTargetInverseSources(
          cancelChecker, inverseSourcesParams
        )
      }, params
    )
  }

  override fun buildTargetDependencySources(
    params: DependencySourcesParams
  ): CompletableFuture<DependencySourcesResult> {
    return runner.handleRequest(
      "buildTargetDependencySources",
      { cancelChecker: CancelChecker, dependencySourcesParams: DependencySourcesParams ->
        projectSyncService.buildTargetDependencySources(
          cancelChecker, dependencySourcesParams
        )
      },
      params
    )
  }

  override fun buildTargetResources(params: ResourcesParams): CompletableFuture<ResourcesResult> {
    return runner.handleRequest(
      "buildTargetResources", { cancelChecker: CancelChecker, resourcesParams: ResourcesParams ->
        projectSyncService.buildTargetResources(
          cancelChecker, resourcesParams
        )
      }, params
    )
  }

  override fun buildTargetCompile(params: CompileParams): CompletableFuture<CompileResult> {
    return runner.handleRequest("buildTargetCompile", { cancelChecker: CancelChecker, params: CompileParams ->
      executeService.compile(
        cancelChecker, params
      )
    }, params)
  }

  override fun buildTargetTest(params: TestParams): CompletableFuture<TestResult> {
    return runner.handleRequest("buildTargetTest", { cancelChecker: CancelChecker, params: TestParams ->
      executeService.test(
        cancelChecker, params
      )
    }, params)
  }

  override fun buildTargetRun(params: RunParams): CompletableFuture<RunResult> {
    return runner.handleRequest("buildTargetRun", { cancelChecker: CancelChecker, params: RunParams ->
      executeService.run(
        cancelChecker, params
      )
    }, params)
  }

  override fun buildTargetRunWithDebug(params: RunWithDebugParams): CompletableFuture<RunResult> {
    return runner.handleRequest(
      "buildTargetRunWithDebug", { cancelChecker: CancelChecker, params: RunWithDebugParams ->
        executeService.runWithDebug(
          cancelChecker, params
        )
      }, params
    )
  }

  override fun buildTargetMobileInstall(params: MobileInstallParams): CompletableFuture<MobileInstallResult> {
    return runner.handleRequest(
      "buildTargetMobileInstall",
      { cancelChecker: CancelChecker, params: MobileInstallParams ->
        executeService.mobileInstall(
          cancelChecker, params,
        )
      },
      params,
    )
  }

  override fun buildTargetCleanCache(params: CleanCacheParams): CompletableFuture<CleanCacheResult> {
    return runner.handleRequest("buildTargetCleanCache", { cancelChecker: CancelChecker, params: CleanCacheParams ->
      executeService.clean(
        cancelChecker, params
      )
    }, params)
  }

  override fun onRunReadStdin(readParams: ReadParams) {}

  override fun buildTargetDependencyModules(
    params: DependencyModulesParams
  ): CompletableFuture<DependencyModulesResult> {
    return runner.handleRequest(
      "buildTargetDependencyModules", { cancelChecker: CancelChecker, params: DependencyModulesParams ->
        projectSyncService.buildTargetDependencyModules(
          cancelChecker, params
        )
      }, params
    )
  }

  override fun debugSessionStart(params: DebugSessionParams): CompletableFuture<DebugSessionAddress> {
    // TODO: https://youtrack.jetbrains.com/issue/BAZEL-239
    return CompletableFuture.failedFuture(Exception("This endpoint is not implemented yet"))
  }

  override fun buildTargetOutputPaths(params: OutputPathsParams): CompletableFuture<OutputPathsResult> {
    return runner.handleRequest(
      "buildTargetOutputPaths", { cancelChecker: CancelChecker, params: OutputPathsParams ->
        projectSyncService.buildTargetOutputPaths(
          cancelChecker, params
        )
      }, params
    )
  }

  override fun buildTargetScalacOptions(
    params: ScalacOptionsParams
  ): CompletableFuture<ScalacOptionsResult> {
    return runner.handleRequest(
      "buildTargetScalacOptions", { cancelChecker: CancelChecker, params: ScalacOptionsParams ->
        projectSyncService.buildTargetScalacOptions(
          cancelChecker, params
        )
      }, params
    )
  }

  override fun buildTargetScalaTestClasses(
    params: ScalaTestClassesParams
  ): CompletableFuture<ScalaTestClassesResult> {
    return runner.handleRequest(
      "buildTargetScalaTestClasses", { cancelChecker: CancelChecker, params: ScalaTestClassesParams ->
        projectSyncService.buildTargetScalaTestClasses(
          cancelChecker, params
        )
      }, params
    )
  }

  override fun buildTargetScalaMainClasses(
    params: ScalaMainClassesParams
  ): CompletableFuture<ScalaMainClassesResult> {
    return runner.handleRequest(
      "buildTargetScalaMainClasses", { cancelChecker: CancelChecker, params: ScalaMainClassesParams ->
        projectSyncService.buildTargetScalaMainClasses(
          cancelChecker, params
        )
      }, params
    )
  }

  override fun buildTargetJavacOptions(
    javacOptionsParams: JavacOptionsParams
  ): CompletableFuture<JavacOptionsResult> {
    return runner.handleRequest(
      "buildTargetJavacOptions", { cancelChecker: CancelChecker, params: JavacOptionsParams ->
        projectSyncService.buildTargetJavacOptions(
          cancelChecker, params
        )
      }, javacOptionsParams
    )
  }

  override fun buildTargetCppOptions(params: CppOptionsParams): CompletableFuture<CppOptionsResult> {
    return runner.handleRequest(
      "buildTargetCppOptions", { cancelChecker: CancelChecker, params: CppOptionsParams ->
        projectSyncService.buildTargetCppOptions(
          cancelChecker, params
        )
      }, params
    )
  }

  override fun buildTargetPythonOptions(
    params: PythonOptionsParams
  ): CompletableFuture<PythonOptionsResult> {
    return runner.handleRequest(
      "buildTargetPythonOptions", { cancelChecker: CancelChecker, params: PythonOptionsParams ->
        projectSyncService.buildTargetPythonOptions(
          cancelChecker, params
        )
      }, params
    )
  }

  override fun buildTargetJvmRunEnvironment(
    params: JvmRunEnvironmentParams
  ): CompletableFuture<JvmRunEnvironmentResult> {
    return runner.handleRequest(
      "jvmRunEnvironment", { cancelChecker: CancelChecker, params: JvmRunEnvironmentParams ->
        projectSyncService.jvmRunEnvironment(
          cancelChecker, params
        )
      }, params
    )
  }

  override fun buildTargetJvmCompileClasspath(params: JvmCompileClasspathParams): CompletableFuture<JvmCompileClasspathResult> {
    return runner.handleRequest(
      "jvmCompileClasspath", { cancelChecker: CancelChecker, params: JvmCompileClasspathParams ->
        projectSyncService.jvmCompileClasspath(
          cancelChecker, params
        )
      }, params
    )
  }

  override fun buildTargetJvmTestEnvironment(
    params: JvmTestEnvironmentParams
  ): CompletableFuture<JvmTestEnvironmentResult> {
    return runner.handleRequest(
      "jvmTestEnvironment", { cancelChecker: CancelChecker, params: JvmTestEnvironmentParams ->
        projectSyncService.jvmTestEnvironment(
          cancelChecker, params
        )
      }, params
    )
  }

  override fun buildTargetJvmBinaryJars(params: JvmBinaryJarsParams): CompletableFuture<JvmBinaryJarsResult> {
    return runner.handleRequest(
      "jvmBinaryJars", { cancelChecker: CancelChecker, params: JvmBinaryJarsParams ->
        projectSyncService.jvmBinaryJars(
          cancelChecker, params
        )
      }, params
    )
  }

  override fun workspaceLibraries(): CompletableFuture<WorkspaceLibrariesResult> {
    return runner.handleRequest("libraries") { cancelChecker: CancelChecker ->
      projectSyncService.workspaceBuildLibraries(
        cancelChecker
      )
    }
  }

  override fun workspaceInvalidTargets(): CompletableFuture<WorkspaceInvalidTargetsResult> {
    return runner.handleRequest("invalidTargets") { cancelChecker: CancelChecker ->
      projectSyncService.workspaceInvalidTargets(
        cancelChecker
      )
    }
  }

  override fun workspaceDirectories(): CompletableFuture<WorkspaceDirectoriesResult> {
    return runner.handleRequest("directories") { cancelChecker: CancelChecker ->
      projectSyncService.workspaceDirectories(
        cancelChecker
      )
    }
  }

  override fun rustWorkspace(params: RustWorkspaceParams): CompletableFuture<RustWorkspaceResult> {
    return runner.handleRequest("rustWorkspace", { cancelChecker: CancelChecker, params: RustWorkspaceParams ->
      projectSyncService.rustWorkspace(
        cancelChecker, params
      )
    }, params)
  }
}
