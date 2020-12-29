package org.jetbrains.bsp.bazel.server.impl;

import ch.epfl.scala.bsp4j.BuildServer;
import ch.epfl.scala.bsp4j.CleanCacheParams;
import ch.epfl.scala.bsp4j.CleanCacheResult;
import ch.epfl.scala.bsp4j.CompileParams;
import ch.epfl.scala.bsp4j.CompileResult;
import ch.epfl.scala.bsp4j.DependencySourcesParams;
import ch.epfl.scala.bsp4j.DependencySourcesResult;
import ch.epfl.scala.bsp4j.InitializeBuildParams;
import ch.epfl.scala.bsp4j.InitializeBuildResult;
import ch.epfl.scala.bsp4j.InverseSourcesParams;
import ch.epfl.scala.bsp4j.InverseSourcesResult;
import ch.epfl.scala.bsp4j.ResourcesParams;
import ch.epfl.scala.bsp4j.ResourcesResult;
import ch.epfl.scala.bsp4j.RunParams;
import ch.epfl.scala.bsp4j.RunResult;
import ch.epfl.scala.bsp4j.SourcesParams;
import ch.epfl.scala.bsp4j.SourcesResult;
import ch.epfl.scala.bsp4j.TestParams;
import ch.epfl.scala.bsp4j.TestResult;
import ch.epfl.scala.bsp4j.WorkspaceBuildTargetsResult;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.bsp.bazel.server.bsp.BazelBspServerRequestHelpers;
import org.jetbrains.bsp.bazel.server.service.BuildServerService;

public class BuildServerImpl implements BuildServer {

  private final BuildServerService buildServerService;
  private final BazelBspServerRequestHelpers serverRequestHelpers;

  public BuildServerImpl(
      BuildServerService buildServerService, BazelBspServerRequestHelpers serverRequestHelpers) {
    this.buildServerService = buildServerService;
    this.serverRequestHelpers = serverRequestHelpers;
  }

  @Override
  public CompletableFuture<InitializeBuildResult> buildInitialize(
      InitializeBuildParams initializeBuildParams) {
    return buildServerService.buildInitialize(initializeBuildParams);
  }

  @Override
  public void onBuildInitialized() {
    buildServerService.onBuildInitialized();
  }

  @Override
  public CompletableFuture<Object> buildShutdown() {
    return buildServerService.buildShutdown();
  }

  @Override
  public void onBuildExit() {
    buildServerService.onBuildExit();
  }

  @Override
  public CompletableFuture<WorkspaceBuildTargetsResult> workspaceBuildTargets() {
    return buildServerService.workspaceBuildTargets();
  }

  @Override
  public CompletableFuture<SourcesResult> buildTargetSources(SourcesParams sourcesParams) {
    return serverRequestHelpers.executeCommand(
        () -> buildServerService.buildTargetSources(sourcesParams));
  }

  @Override
  public CompletableFuture<InverseSourcesResult> buildTargetInverseSources(
      InverseSourcesParams inverseSourcesParams) {
    return serverRequestHelpers.executeCommand(
        () -> buildServerService.buildTargetInverseSources(inverseSourcesParams));
  }

  @Override
  public CompletableFuture<DependencySourcesResult> buildTargetDependencySources(
      DependencySourcesParams dependencySourcesParams) {
    return serverRequestHelpers.executeCommand(
        () -> buildServerService.buildTargetDependencySources(dependencySourcesParams));
  }

  @Override
  public CompletableFuture<ResourcesResult> buildTargetResources(ResourcesParams resourcesParams) {
    return serverRequestHelpers.executeCommand(
        () -> buildServerService.buildTargetResources(resourcesParams));
  }

  @Override
  public CompletableFuture<CompileResult> buildTargetCompile(CompileParams compileParams) {
    return serverRequestHelpers.executeCommand(
        () -> buildServerService.buildTargetCompile(compileParams));
  }

  @Override
  public CompletableFuture<TestResult> buildTargetTest(TestParams testParams) {
    return serverRequestHelpers.executeCommand(
        () -> buildServerService.buildTargetTest(testParams));
  }

  @Override
  public CompletableFuture<RunResult> buildTargetRun(RunParams runParams) {
    return serverRequestHelpers.executeCommand(() -> buildServerService.buildTargetRun(runParams));
  }

  @Override
  public CompletableFuture<CleanCacheResult> buildTargetCleanCache(
      CleanCacheParams cleanCacheParams) {
    return serverRequestHelpers.executeCommand(
        () -> buildServerService.buildTargetCleanCache(cleanCacheParams));
  }
}
