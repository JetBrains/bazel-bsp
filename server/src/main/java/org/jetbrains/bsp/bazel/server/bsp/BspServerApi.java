package org.jetbrains.bsp.bazel.server.bsp;

import ch.epfl.scala.bsp4j.BuildClient;
import ch.epfl.scala.bsp4j.BuildServer;
import ch.epfl.scala.bsp4j.CleanCacheParams;
import ch.epfl.scala.bsp4j.CleanCacheResult;
import ch.epfl.scala.bsp4j.CompileParams;
import ch.epfl.scala.bsp4j.CompileResult;
import ch.epfl.scala.bsp4j.CppBuildServer;
import ch.epfl.scala.bsp4j.CppOptionsParams;
import ch.epfl.scala.bsp4j.CppOptionsResult;
import ch.epfl.scala.bsp4j.DebugSessionAddress;
import ch.epfl.scala.bsp4j.DebugSessionParams;
import ch.epfl.scala.bsp4j.DependencyModulesParams;
import ch.epfl.scala.bsp4j.DependencyModulesResult;
import ch.epfl.scala.bsp4j.DependencySourcesParams;
import ch.epfl.scala.bsp4j.DependencySourcesResult;
import ch.epfl.scala.bsp4j.InitializeBuildParams;
import ch.epfl.scala.bsp4j.InitializeBuildResult;
import ch.epfl.scala.bsp4j.InverseSourcesParams;
import ch.epfl.scala.bsp4j.InverseSourcesResult;
import ch.epfl.scala.bsp4j.JavaBuildServer;
import ch.epfl.scala.bsp4j.JavacOptionsParams;
import ch.epfl.scala.bsp4j.JavacOptionsResult;
import ch.epfl.scala.bsp4j.JvmBuildServer;
import ch.epfl.scala.bsp4j.JvmRunEnvironmentParams;
import ch.epfl.scala.bsp4j.JvmRunEnvironmentResult;
import ch.epfl.scala.bsp4j.JvmTestEnvironmentParams;
import ch.epfl.scala.bsp4j.JvmTestEnvironmentResult;
import ch.epfl.scala.bsp4j.OutputPathsParams;
import ch.epfl.scala.bsp4j.OutputPathsResult;
import ch.epfl.scala.bsp4j.ResourcesParams;
import ch.epfl.scala.bsp4j.ResourcesResult;
import ch.epfl.scala.bsp4j.RunParams;
import ch.epfl.scala.bsp4j.RunResult;
import ch.epfl.scala.bsp4j.ScalaBuildServer;
import ch.epfl.scala.bsp4j.ScalaMainClassesParams;
import ch.epfl.scala.bsp4j.ScalaMainClassesResult;
import ch.epfl.scala.bsp4j.ScalaTestClassesParams;
import ch.epfl.scala.bsp4j.ScalaTestClassesResult;
import ch.epfl.scala.bsp4j.ScalacOptionsParams;
import ch.epfl.scala.bsp4j.ScalacOptionsResult;
import ch.epfl.scala.bsp4j.SourcesParams;
import ch.epfl.scala.bsp4j.SourcesResult;
import ch.epfl.scala.bsp4j.TestParams;
import ch.epfl.scala.bsp4j.TestResult;
import ch.epfl.scala.bsp4j.WorkspaceBuildTargetsResult;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import org.jetbrains.bsp.bazel.server.sync.ExecuteService;
import org.jetbrains.bsp.bazel.server.sync.ProjectSyncService;

public class BspServerApi
    implements BuildServer, JvmBuildServer, ScalaBuildServer, JavaBuildServer, CppBuildServer {

  private final Supplier<BazelServices> bazelServicesBuilder;
  private BazelBspServerLifetime serverLifetime = null;
  private BspRequestsRunner runner = null;
  private ProjectSyncService projectSyncService = null;
  private ExecuteService executeService = null;

  public BspServerApi(Supplier<BazelServices> bazelServicesBuilder) {
    this.bazelServicesBuilder = bazelServicesBuilder;
  }

  void init() {
    var serverContainer = this.bazelServicesBuilder.get();

    this.serverLifetime = serverContainer.getServerLifetime();
    this.runner = serverContainer.getBspRequestsRunner();
    this.projectSyncService = serverContainer.getProjectSyncService();
    this.executeService = serverContainer.getExecuteService();
  }

  @Override
  public CompletableFuture<InitializeBuildResult> buildInitialize(
      InitializeBuildParams initializeBuildParams) {
    init();
    return runner.handleRequest(
        "buildInitialize", projectSyncService::initialize, runner::serverIsNotFinished);
  }

  @Override
  public void onBuildInitialized() {
    runner.handleNotification("onBuildInitialized", serverLifetime::setInitializedComplete);
  }

  @Override
  public CompletableFuture<Object> buildShutdown() {
    return runner.handleRequest(
        "buildShutdown",
        cancelChecker -> {
          serverLifetime.setFinishedComplete();
          return new Object();
        },
        runner::serverIsInitialized);
  }

  @Override
  public void onBuildExit() {
    runner.handleNotification("onBuildExit", serverLifetime::forceFinish);
  }

  @Override
  public CompletableFuture<WorkspaceBuildTargetsResult> workspaceBuildTargets() {
    return runner.handleRequest("workspaceBuildTargets", projectSyncService::workspaceBuildTargets);
  }

  @Override
  public CompletableFuture<Object> workspaceReload() {
    return runner.handleRequest("workspaceReload", projectSyncService::workspaceReload);
  }

  @Override
  public CompletableFuture<SourcesResult> buildTargetSources(SourcesParams params) {
    return runner.handleRequest(
        "buildTargetSources", projectSyncService::buildTargetSources, params);
  }

  @Override
  public CompletableFuture<InverseSourcesResult> buildTargetInverseSources(
      InverseSourcesParams params) {
    return runner.handleRequest(
        "buildTargetInverseSources", projectSyncService::buildTargetInverseSources, params);
  }

  @Override
  public CompletableFuture<DependencySourcesResult> buildTargetDependencySources(
      DependencySourcesParams params) {
    return runner.handleRequest(
        "buildTargetDependencySources", projectSyncService::buildTargetDependencySources, params);
  }

  @Override
  public CompletableFuture<ResourcesResult> buildTargetResources(ResourcesParams params) {
    return runner.handleRequest(
        "buildTargetResources", projectSyncService::buildTargetResources, params);
  }

  @Override
  public CompletableFuture<CompileResult> buildTargetCompile(CompileParams params) {
    return runner.handleRequest("buildTargetCompile", executeService::compile, params);
  }

  @Override
  public CompletableFuture<TestResult> buildTargetTest(TestParams params) {
    return runner.handleRequest("buildTargetTest", executeService::test, params);
  }

  @Override
  public CompletableFuture<RunResult> buildTargetRun(RunParams params) {
    return runner.handleRequest("buildTargetRun", executeService::run, params);
  }

  @Override
  public CompletableFuture<CleanCacheResult> buildTargetCleanCache(CleanCacheParams params) {
    return runner.handleRequest("buildTargetCleanCache", executeService::clean, params);
  }

  @Override
  public CompletableFuture<DependencyModulesResult> buildTargetDependencyModules(
      DependencyModulesParams params) {
    return runner.handleRequest(
        "buildTargetDependencyModules", projectSyncService::buildTargetDependencyModules, params);
  }

  @Override
  public CompletableFuture<DebugSessionAddress> debugSessionStart(DebugSessionParams params) {
    // TODO: https://youtrack.jetbrains.com/issue/BAZEL-239
    return CompletableFuture.failedFuture(new Exception("This endpoint is not implemented yet"));
  }

  @Override
  public CompletableFuture<OutputPathsResult> buildTargetOutputPaths(OutputPathsParams params) {
    return runner.handleRequest(
        "buildTargetOutputPaths", projectSyncService::buildTargetOutputPaths, params);
  }

  @Override
  public void onConnectWithClient(BuildClient buildClient) {
    BuildServer.super.onConnectWithClient(buildClient);
  }

  @Override
  public CompletableFuture<ScalacOptionsResult> buildTargetScalacOptions(
      ScalacOptionsParams params) {
    return runner.handleRequest(
        "buildTargetScalacOptions", projectSyncService::buildTargetScalacOptions, params);
  }

  @Override
  public CompletableFuture<ScalaTestClassesResult> buildTargetScalaTestClasses(
      ScalaTestClassesParams params) {
    return runner.handleRequest(
        "buildTargetScalaTestClasses", projectSyncService::buildTargetScalaTestClasses, params);
  }

  @Override
  public CompletableFuture<ScalaMainClassesResult> buildTargetScalaMainClasses(
      ScalaMainClassesParams params) {
    return runner.handleRequest(
        "buildTargetScalaMainClasses", projectSyncService::buildTargetScalaMainClasses, params);
  }

  @Override
  public CompletableFuture<JavacOptionsResult> buildTargetJavacOptions(
      JavacOptionsParams javacOptionsParams) {
    return runner.handleRequest(
        "buildTargetJavacOptions", projectSyncService::buildTargetJavacOptions, javacOptionsParams);
  }

  @Override
  public CompletableFuture<CppOptionsResult> buildTargetCppOptions(CppOptionsParams params) {
    return runner.handleRequest(
        "buildTargetCppOptions", projectSyncService::buildTargetCppOptions, params);
  }

  @Override
  public CompletableFuture<JvmRunEnvironmentResult> jvmRunEnvironment(
      JvmRunEnvironmentParams params) {
    return runner.handleRequest("jvmRunEnvironment", projectSyncService::jvmRunEnvironment, params);
  }

  @Override
  public CompletableFuture<JvmTestEnvironmentResult> jvmTestEnvironment(
      JvmTestEnvironmentParams params) {
    return runner.handleRequest(
        "jvmTestEnvironment", projectSyncService::jvmTestEnvironment, params);
  }
}
