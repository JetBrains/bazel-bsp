package org.jetbrains.bsp.bazel.server.sync;

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
import org.jetbrains.bsp.bazel.server.sync.model.Language;

/** A facade for all project sync related methods */
public class ProjectSyncService {
  private final BspProjectMapper bspMapper;
  private final ProjectProvider projectProvider;

  public ProjectSyncService(BspProjectMapper bspProjectMapper, ProjectProvider projectProvider) {
    this.bspMapper = bspProjectMapper;
    this.projectProvider = projectProvider;
  }

  public InitializeBuildResult initialize() {
    return bspMapper.initializeServer(Language.Companion.all());
  }

  // We might consider doing the actual project reload in this endpoint
  // i.e. just run projectProvider.refreshAndGet() and in workspaceBuildTargets
  // just run projectProvider.get() although current approach seems to work
  // correctly, so I am not changing anything.
  public Object workspaceReload() {
    return new Object();
  }

  public WorkspaceBuildTargetsResult workspaceBuildTargets() {
    var project = projectProvider.refreshAndGet();
    return bspMapper.workspaceTargets(project);
  }

  public SourcesResult buildTargetSources(SourcesParams sourcesParams) {
    var project = projectProvider.get();
    return bspMapper.sources(project, sourcesParams);
  }

  public ResourcesResult buildTargetResources(ResourcesParams resourcesParams) {
    var project = projectProvider.get();
    return bspMapper.resources(project, resourcesParams);
  }

  public InverseSourcesResult buildTargetInverseSources(InverseSourcesParams inverseSourcesParams) {
    var project = projectProvider.get();
    return bspMapper.inverseSources(project, inverseSourcesParams);
  }

  public DependencySourcesResult buildTargetDependencySources(
      DependencySourcesParams dependencySourcesParams) {
    var project = projectProvider.get();
    return bspMapper.dependencySources(project, dependencySourcesParams);
  }

  public JvmRunEnvironmentResult jvmRunEnvironment(JvmRunEnvironmentParams params) {
    var project = projectProvider.get();
    return bspMapper.jvmRunEnvironment(project, params);
  }

  public JvmTestEnvironmentResult jvmTestEnvironment(JvmTestEnvironmentParams params) {
    var project = projectProvider.get();
    return bspMapper.jvmTestEnvironment(project, params);
  }

  public JavacOptionsResult buildTargetJavacOptions(JavacOptionsParams params) {
    var project = projectProvider.get();
    return bspMapper.buildTargetJavacOptions(project, params);
  }

  public ScalacOptionsResult buildTargetScalacOptions(ScalacOptionsParams params) {
    var project = projectProvider.get();
    return bspMapper.buildTargetScalacOptions(project, params);
  }

  public ScalaTestClassesResult buildTargetScalaTestClasses(ScalaTestClassesParams params) {
    var project = projectProvider.get();
    return bspMapper.buildTargetScalaTestClasses(project, params);
  }

  public ScalaMainClassesResult buildTargetScalaMainClasses(ScalaMainClassesParams params) {
    var project = projectProvider.get();
    return bspMapper.buildTargetScalaMainClasses(project, params);
  }

  // TODO implement this endpoint to return libraries with maven coordinates that target depends on
  // this should be helpful for 3rd party shared indexes in IntelliJ, however the endpoint is not
  // yet used in the client
  public DependencyModulesResult buildTargetDependencyModules(DependencyModulesParams params) {
    return new DependencyModulesResult(Collections.emptyList());
  }
}
