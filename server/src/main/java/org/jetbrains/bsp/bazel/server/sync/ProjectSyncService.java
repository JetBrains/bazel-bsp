package org.jetbrains.bsp.bazel.server.sync;

import com.jetbrains.bsp.bsp4kt.CppOptionsParams;
import com.jetbrains.bsp.bsp4kt.CppOptionsResult;
import com.jetbrains.bsp.bsp4kt.DependencyModulesParams;
import com.jetbrains.bsp.bsp4kt.DependencyModulesResult;
import com.jetbrains.bsp.bsp4kt.DependencySourcesParams;
import com.jetbrains.bsp.bsp4kt.DependencySourcesResult;
import com.jetbrains.bsp.bsp4kt.InitializeBuildResult;
import com.jetbrains.bsp.bsp4kt.InverseSourcesParams;
import com.jetbrains.bsp.bsp4kt.InverseSourcesResult;
import com.jetbrains.bsp.bsp4kt.JavacOptionsParams;
import com.jetbrains.bsp.bsp4kt.JavacOptionsResult;
import com.jetbrains.bsp.bsp4kt.JvmRunEnvironmentParams;
import com.jetbrains.bsp.bsp4kt.JvmRunEnvironmentResult;
import com.jetbrains.bsp.bsp4kt.JvmTestEnvironmentParams;
import com.jetbrains.bsp.bsp4kt.JvmTestEnvironmentResult;
import com.jetbrains.bsp.bsp4kt.OutputPathsParams;
import com.jetbrains.bsp.bsp4kt.OutputPathsResult;
import com.jetbrains.bsp.bsp4kt.PythonOptionsParams;
import com.jetbrains.bsp.bsp4kt.PythonOptionsResult;
import com.jetbrains.bsp.bsp4kt.ResourcesParams;
import com.jetbrains.bsp.bsp4kt.ResourcesResult;
import com.jetbrains.bsp.bsp4kt.ScalaMainClassesParams;
import com.jetbrains.bsp.bsp4kt.ScalaMainClassesResult;
import com.jetbrains.bsp.bsp4kt.ScalaTestClassesParams;
import com.jetbrains.bsp.bsp4kt.ScalaTestClassesResult;
import com.jetbrains.bsp.bsp4kt.ScalacOptionsParams;
import com.jetbrains.bsp.bsp4kt.ScalacOptionsResult;
import com.jetbrains.bsp.bsp4kt.SourcesParams;
import com.jetbrains.bsp.bsp4kt.SourcesResult;
import com.jetbrains.bsp.bsp4kt.WorkspaceBuildTargetsResult;
import java.util.Collections;
import com.jetbrains.jsonrpc4kt.CancelChecker;
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

  public PythonOptionsResult buildTargetPythonOptions(
          CancelChecker cancelChecker, PythonOptionsParams params) {
    var project = projectProvider.get(cancelChecker);
    return bspMapper.buildTargetPythonOptions(project, params);
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
