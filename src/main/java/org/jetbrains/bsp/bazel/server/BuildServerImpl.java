package org.jetbrains.bsp.bazel.server;

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

public class BuildServerImpl implements BuildServer {

  // TODO won't be cyclical, make dependencies more organised
  private final BazelBspServer bazelBspServer;

  public BuildServerImpl(BazelBspServer bazelBspServer) {
    this.bazelBspServer = bazelBspServer;
  }

  @Override
  public CompletableFuture<InitializeBuildResult> buildInitialize(InitializeBuildParams params) {
    return bazelBspServer.buildInitialize(params);
  }

  @Override
  public void onBuildInitialized() {
    bazelBspServer.onBuildInitialized();
  }

  @Override
  public CompletableFuture<Object> buildShutdown() {
    return bazelBspServer.buildShutdown();
  }

  @Override
  public void onBuildExit() {
    bazelBspServer.onBuildExit();
  }

  @Override
  public CompletableFuture<WorkspaceBuildTargetsResult> workspaceBuildTargets() {
    return bazelBspServer.workspaceBuildTargets();
  }

  @Override
  public CompletableFuture<SourcesResult> buildTargetSources(SourcesParams params) {
    return bazelBspServer.buildTargetSources(params);
  }

  @Override
  public CompletableFuture<InverseSourcesResult> buildTargetInverseSources(
      InverseSourcesParams params) {
    return bazelBspServer.buildTargetInverseSources(params);
  }

  @Override
  public CompletableFuture<DependencySourcesResult> buildTargetDependencySources(
      DependencySourcesParams params) {
    return bazelBspServer.buildTargetDependencySources(params);
  }

  @Override
  public CompletableFuture<ResourcesResult> buildTargetResources(ResourcesParams params) {
    return bazelBspServer.buildTargetResources(params);
  }

  @Override
  public CompletableFuture<CompileResult> buildTargetCompile(CompileParams params) {
    return bazelBspServer.buildTargetCompile(params);
  }

  @Override
  public CompletableFuture<TestResult> buildTargetTest(TestParams params) {
    return bazelBspServer.buildTargetTest(params);
  }

  @Override
  public CompletableFuture<RunResult> buildTargetRun(RunParams params) {
    return bazelBspServer.buildTargetRun(params);
  }

  @Override
  public CompletableFuture<CleanCacheResult> buildTargetCleanCache(CleanCacheParams params) {
    return bazelBspServer.buildTargetCleanCache(params);
  }
}
