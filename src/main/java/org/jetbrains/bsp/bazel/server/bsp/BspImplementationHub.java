package org.jetbrains.bsp.bazel.server.bsp;

import ch.epfl.scala.bsp4j.BuildServer;
import ch.epfl.scala.bsp4j.CleanCacheParams;
import ch.epfl.scala.bsp4j.CleanCacheResult;
import ch.epfl.scala.bsp4j.CompileParams;
import ch.epfl.scala.bsp4j.CompileResult;
import ch.epfl.scala.bsp4j.CppBuildServer;
import ch.epfl.scala.bsp4j.CppOptionsParams;
import ch.epfl.scala.bsp4j.CppOptionsResult;
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

public class BspImplementationHub
    implements BuildServer, ScalaBuildServer, JavaBuildServer, CppBuildServer {

  private final BuildServer buildServer;
  private final ScalaBuildServer scalaBuildServer;
  private final JavaBuildServer javaBuildServer;
  private final CppBuildServer cppBuildServer;

  public BspImplementationHub(
      BuildServer buildServer,
      ScalaBuildServer scalaBuildServer,
      JavaBuildServer javaBuildServer,
      CppBuildServer cppBuildServer) {
    this.buildServer = buildServer;
    this.scalaBuildServer = scalaBuildServer;
    this.javaBuildServer = javaBuildServer;
    this.cppBuildServer = cppBuildServer;
  }

  @Override
  public CompletableFuture<InitializeBuildResult> buildInitialize(InitializeBuildParams params) {
    return buildServer.buildInitialize(params);
  }

  @Override
  public void onBuildInitialized() {
    buildServer.onBuildInitialized();
  }

  @Override
  public CompletableFuture<Object> buildShutdown() {
    return buildServer.buildShutdown();
  }

  @Override
  public void onBuildExit() {
    buildServer.onBuildExit();
  }

  @Override
  public CompletableFuture<WorkspaceBuildTargetsResult> workspaceBuildTargets() {
    return buildServer.workspaceBuildTargets();
  }

  @Override
  public CompletableFuture<Object> workspaceReload() {
    return buildServer.workspaceReload();
  }

  @Override
  public CompletableFuture<SourcesResult> buildTargetSources(SourcesParams params) {
    return buildServer.buildTargetSources(params);
  }

  @Override
  public CompletableFuture<InverseSourcesResult> buildTargetInverseSources(
      InverseSourcesParams params) {
    return buildServer.buildTargetInverseSources(params);
  }

  @Override
  public CompletableFuture<DependencySourcesResult> buildTargetDependencySources(
      DependencySourcesParams params) {
    return buildServer.buildTargetDependencySources(params);
  }

  @Override
  public CompletableFuture<ResourcesResult> buildTargetResources(ResourcesParams params) {
    return buildServer.buildTargetResources(params);
  }

  @Override
  public CompletableFuture<CompileResult> buildTargetCompile(CompileParams params) {
    return buildServer.buildTargetCompile(params);
  }

  @Override
  public CompletableFuture<TestResult> buildTargetTest(TestParams params) {
    return buildServer.buildTargetTest(params);
  }

  @Override
  public CompletableFuture<RunResult> buildTargetRun(RunParams params) {
    return buildServer.buildTargetRun(params);
  }

  @Override
  public CompletableFuture<CleanCacheResult> buildTargetCleanCache(CleanCacheParams params) {
    return buildServer.buildTargetCleanCache(params);
  }

  @Override
  public CompletableFuture<DependencyModulesResult> buildTargetDependencyModules(
      DependencyModulesParams params) {
    return buildServer.buildTargetDependencyModules(params);
  }

  @Override
  public CompletableFuture<ScalacOptionsResult> buildTargetScalacOptions(
      ScalacOptionsParams params) {
    return scalaBuildServer.buildTargetScalacOptions(params);
  }

  @Override
  public CompletableFuture<ScalaTestClassesResult> buildTargetScalaTestClasses(
      ScalaTestClassesParams params) {
    return scalaBuildServer.buildTargetScalaTestClasses(params);
  }

  @Override
  public CompletableFuture<ScalaMainClassesResult> buildTargetScalaMainClasses(
      ScalaMainClassesParams params) {
    return scalaBuildServer.buildTargetScalaMainClasses(params);
  }

  @Override
  public CompletableFuture<JavacOptionsResult> buildTargetJavacOptions(JavacOptionsParams params) {
    return javaBuildServer.buildTargetJavacOptions(params);
  }

  @Override
  public CompletableFuture<CppOptionsResult> buildTargetCppOptions(
      CppOptionsParams cppOptionsParams) {
    return cppBuildServer.buildTargetCppOptions(cppOptionsParams);
  }
}
