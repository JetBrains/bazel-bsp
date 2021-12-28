package org.jetbrains.bsp.bazel.server.bsp;

import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import ch.epfl.scala.bsp4j.CompileResult;
import ch.epfl.scala.bsp4j.SourceItem;
import ch.epfl.scala.bsp4j.WorkspaceBuildTargetsResult;
import com.google.common.collect.ImmutableList;
import com.google.devtools.build.lib.query2.proto.proto2api.Build;

import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseError;
import org.jetbrains.bsp.bazel.bazelrunner.BazelRunner;
import org.jetbrains.bsp.bazel.bazelrunner.data.BazelData;
import org.jetbrains.bsp.bazel.commons.Constants;
import org.jetbrains.bsp.bazel.commons.Lazy;
import org.jetbrains.bsp.bazel.server.bep.BepServer;
import org.jetbrains.bsp.bazel.server.bsp.managers.BazelBspAspectsManager;
import org.jetbrains.bsp.bazel.server.bsp.managers.BazelBspCompilationManager;
import org.jetbrains.bsp.bazel.server.bsp.managers.BazelBspQueryManager;
import org.jetbrains.bsp.bazel.server.bsp.managers.BazelBspTargetManager;
import org.jetbrains.bsp.bazel.server.bsp.managers.BazelCppTargetManager;

public class BazelBspServerBuildManager {

  public static final String BAZEL_PRINT_ASPECT = "@//.bazelbsp:aspects.bzl%print_aspect";

  private final BazelBspServerRequestHelpers serverRequestHelpers;
  private final BazelData bazelData;
  private final BazelBspQueryManager bazelBspQueryManager;
  private final BazelBspCompilationManager bazelBspCompilationManager;
  private final BazelBspTargetManager bazelBspTargetManager;
  private final BazelBspAspectsManager bazelBspAspectsManager;
  private final BazelCppTargetManager bazelCppTargetManager;

  private BepServer bepServer;

  public BazelBspServerBuildManager(
      BazelBspServerRequestHelpers serverRequestHelpers,
      BazelData bazelData,
      BazelRunner bazelRunner,
      BazelBspCompilationManager bazelBspCompilationManager,
      BazelBspAspectsManager bazelBspAspectsManager,
      BazelBspTargetManager bazelBspTargetManager,
      BazelCppTargetManager bazelCppTargetManager,
      BazelBspQueryManager bazelBspQueryManager) {
    this.serverRequestHelpers = serverRequestHelpers;
    this.bazelData = bazelData;
    this.bazelBspCompilationManager = bazelBspCompilationManager;
    this.bazelBspAspectsManager = bazelBspAspectsManager;
    this.bazelCppTargetManager = bazelCppTargetManager;
    this.bazelBspTargetManager = bazelBspTargetManager;
    this.bazelBspQueryManager = bazelBspQueryManager;
  }

  public CompletableFuture<WorkspaceBuildTargetsResult> getWorkspaceBuildTargets() {
    return serverRequestHelpers.executeCommand(
        "workspaceBuildTargets", bazelBspQueryManager::getWorkspaceBuildTargets);
  }

  public List<SourceItem> getSourceItems(Build.Rule rule, BuildTargetIdentifier label) {
    return bazelBspQueryManager.getSourceItems(rule, label);
  }

  public String getSourcesRoot(URI sourceUri) {
    Path sourcePath = Paths.get(sourceUri);
    FileSystem fs = FileSystems.getDefault();
    PathMatcher sourceRootPattern = fs.getPathMatcher("glob:**/" +
            "{main,test,tests,src,3rdparty,3rd_party,thirdparty,third_party}/" +
            "{resources,scala,java,kotlin,jvm,proto,python,protobuf,py}");
    PathMatcher defaultTestRootPattern = fs.getPathMatcher("glob:**/{test,tests}");
    Path sourceRootGuess = null;
    for(PathMatcher pattern : new PathMatcher[]{sourceRootPattern, defaultTestRootPattern}){
      sourceRootGuess = approximateSourceRoot(sourcePath, pattern);
      if(sourceRootGuess != null)
        break;
    }
    if(sourceRootGuess == null) {
      return sourcePath.getParent().toString();
    }
    return sourceRootGuess.toAbsolutePath().toString();
  }

  private Path approximateSourceRoot(Path dir, PathMatcher matcher) {
    if (matcher.matches(dir)) return dir;
    Path parent = dir.getParent();
    if (parent != null) {
      return approximateSourceRoot(parent, matcher);
    }
    return null;
  }

  public List<String> lookUpTransitiveSourceJars(String target) {
    // TODO(illicitonion): Use an aspect output group, rather than parsing stderr
    // logging
    return bazelBspAspectsManager
        .fetchLinesFromAspect(target, BAZEL_PRINT_ASPECT)
        .filter(
            parts ->
                parts.size() == 3
                    && parts.get(0).equals(BazelBspAspectsManager.DEBUG_MESSAGE)
                    && parts.get(1).contains(BazelBspAspectsManager.ASPECT_LOCATION)
                    && parts.get(2).endsWith(".jar"))
        .map(parts -> Constants.EXEC_ROOT_PREFIX + parts.get(2))
        .collect(Collectors.toList());
  }

  public List<String> getResources(Build.Rule rule, Build.QueryResult queryResult) {
    return bazelBspQueryManager.getResources(rule, queryResult);
  }

  public void setBepServer(BepServer bepServer) {
    this.bepServer = bepServer;
    this.bazelBspQueryManager.setBepServer(bepServer);
    this.bazelBspCompilationManager.setBepServer(bepServer);
    this.bazelBspAspectsManager.setBepServer(bepServer);
  }

  public Either<ResponseError, CompileResult> buildTargetsWithBep(
      List<BuildTargetIdentifier> targets, ArrayList<String> extraFlags) {
    if (bepServer.getBuildTargetsSources().isEmpty()) {
      bazelBspQueryManager.getWorkspaceBuildTargets();
    }
    return bazelBspCompilationManager.buildTargetsWithBep(targets, extraFlags);
  }

  public List<Lazy<?>> getLazyVals() {
    return ImmutableList.of(
        bazelBspTargetManager.getBazelBspJvmTargetManager(),
        bazelBspTargetManager.getBazelBspScalaTargetManager());
  }
}
