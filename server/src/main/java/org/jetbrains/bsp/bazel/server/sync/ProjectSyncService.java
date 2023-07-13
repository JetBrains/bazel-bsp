package org.jetbrains.bsp.bazel.server.sync;

import ch.epfl.scala.bsp4j.CppOptionsParams;
import ch.epfl.scala.bsp4j.CppOptionsResult;
import ch.epfl.scala.bsp4j.DependencyModulesParams;
import ch.epfl.scala.bsp4j.DependencyModulesResult;
import ch.epfl.scala.bsp4j.DependencySourcesParams;
import ch.epfl.scala.bsp4j.DependencySourcesResult;
import ch.epfl.scala.bsp4j.InitializeBuildResult;
import ch.epfl.scala.bsp4j.InverseSourcesParams;
import ch.epfl.scala.bsp4j.InverseSourcesResult;
import ch.epfl.scala.bsp4j.JavacOptionsParams;
import ch.epfl.scala.bsp4j.JavacOptionsResult;
import ch.epfl.scala.bsp4j.JvmRunEnvironmentParams;
import ch.epfl.scala.bsp4j.JvmRunEnvironmentResult;
import ch.epfl.scala.bsp4j.JvmTestEnvironmentParams;
import ch.epfl.scala.bsp4j.JvmTestEnvironmentResult;
import ch.epfl.scala.bsp4j.OutputPathsParams;
import ch.epfl.scala.bsp4j.OutputPathsResult;
import ch.epfl.scala.bsp4j.ResourcesParams;
import ch.epfl.scala.bsp4j.ResourcesResult;
import ch.epfl.scala.bsp4j.ScalaMainClassesParams;
import ch.epfl.scala.bsp4j.ScalaMainClassesResult;
import ch.epfl.scala.bsp4j.ScalaTestClassesParams;
import ch.epfl.scala.bsp4j.ScalaTestClassesResult;
import ch.epfl.scala.bsp4j.ScalacOptionsParams;
import ch.epfl.scala.bsp4j.ScalacOptionsResult;
import ch.epfl.scala.bsp4j.SourcesParams;
import ch.epfl.scala.bsp4j.SourcesResult;
import ch.epfl.scala.bsp4j.WorkspaceBuildTargetsResult;
import java.util.Collections;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.jetbrains.bsp.bazel.server.sync.model.Language;

/** A facade for all project sync related methods */
public class ProjectSyncService {
  private final BspProjectMapper bspMapper;
  private final ProjectProvider projectProvider;

  public ProjectSyncService(BspProjectMapper bspProjectMapper, ProjectProvider projectProvider) {
    this.bspMapper = bspProjectMapper;
    this.projectProvider = projectProvider;
  }

  public InitializeBuildResult initialize(CancelChecker cancelChecker) {
    return bspMapper.initializeServer(Language.Companion.all());
  }

  // We might consider doing the actual project reload in this endpoint
  // i.e. just run projectProvider.refreshAndGet() and in workspaceBuildTargets
  // just run projectProvider.get() although current approach seems to work
  // correctly, so I am not changing anything.
  public Object workspaceReload(CancelChecker cancelChecker) {
    return new Object();
  }

  public WorkspaceBuildTargetsResult workspaceBuildTargets(CancelChecker cancelChecker) {
    var project = projectProvider.refreshAndGet(cancelChecker);
    return bspMapper.workspaceTargets(project);
  }

  public WorkspaceLibrariesResult workspaceBuildLibraries(CancelChecker cancelChecker) {
    var project = projectProvider.get(cancelChecker);
    return bspMapper.workspaceLibraries(project);
  }

  public SourcesResult buildTargetSources(
      CancelChecker cancelChecker, SourcesParams sourcesParams) {
    var project = projectProvider.get(cancelChecker);
    return bspMapper.sources(project, sourcesParams);
  }

  public ResourcesResult buildTargetResources(
      CancelChecker cancelChecker, ResourcesParams resourcesParams) {
    var project = projectProvider.get(cancelChecker);
    return bspMapper.resources(project, resourcesParams);
  }

  public InverseSourcesResult buildTargetInverseSources(
      CancelChecker cancelChecker, InverseSourcesParams inverseSourcesParams) {
    var project = projectProvider.get(cancelChecker);
    return bspMapper.inverseSources(project, inverseSourcesParams);
  }

  public DependencySourcesResult buildTargetDependencySources(
      CancelChecker cancelChecker, DependencySourcesParams dependencySourcesParams) {
    var project = projectProvider.get(cancelChecker);
    return bspMapper.dependencySources(project, dependencySourcesParams);
  }

  public OutputPathsResult buildTargetOutputPaths(
      CancelChecker cancelChecker, OutputPathsParams params) {
    var project = projectProvider.get(cancelChecker);
    return bspMapper.outputPaths(project, params);
  }

  public JvmRunEnvironmentResult jvmRunEnvironment(
      CancelChecker cancelChecker, JvmRunEnvironmentParams params) {
    var project = projectProvider.get(cancelChecker);
    return bspMapper.jvmRunEnvironment(project, params);
  }

  public JvmTestEnvironmentResult jvmTestEnvironment(
      CancelChecker cancelChecker, JvmTestEnvironmentParams params) {
    var project = projectProvider.get(cancelChecker);
    return bspMapper.jvmTestEnvironment(project, params);
  }

  public JavacOptionsResult buildTargetJavacOptions(
      CancelChecker cancelChecker, JavacOptionsParams params) {
    var project = projectProvider.get(cancelChecker);
    return bspMapper.buildTargetJavacOptions(project, params);
  }

  public CppOptionsResult buildTargetCppOptions(
      CancelChecker cancelChecker, CppOptionsParams params) {
    var project = projectProvider.get(cancelChecker);
    return bspMapper.buildTargetCppOptions(project, params);
  }

  public ScalacOptionsResult buildTargetScalacOptions(
      CancelChecker cancelChecker, ScalacOptionsParams params) {
    var project = projectProvider.get(cancelChecker);
    return bspMapper.buildTargetScalacOptions(project, params);
  }

  public ScalaTestClassesResult buildTargetScalaTestClasses(
      CancelChecker cancelChecker, ScalaTestClassesParams params) {
    var project = projectProvider.get(cancelChecker);
    return bspMapper.buildTargetScalaTestClasses(project, params);
  }

  public ScalaMainClassesResult buildTargetScalaMainClasses(
      CancelChecker cancelChecker, ScalaMainClassesParams params) {
    var project = projectProvider.get(cancelChecker);
    return bspMapper.buildTargetScalaMainClasses(project, params);
  }

  // TODO implement this endpoint to return libraries with maven coordinates that target depends on
  // this should be helpful for 3rd party shared indexes in IntelliJ, however the endpoint is not
  // yet used in the client
  public DependencyModulesResult buildTargetDependencyModules(
      CancelChecker cancelChecker, DependencyModulesParams params) {
    return new DependencyModulesResult(Collections.emptyList());
  }
}
