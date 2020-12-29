package org.jetbrains.bsp.bazel.server.service;

import ch.epfl.scala.bsp4j.BuildServerCapabilities;
import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import ch.epfl.scala.bsp4j.CleanCacheParams;
import ch.epfl.scala.bsp4j.CleanCacheResult;
import ch.epfl.scala.bsp4j.CompileParams;
import ch.epfl.scala.bsp4j.CompileProvider;
import ch.epfl.scala.bsp4j.CompileResult;
import ch.epfl.scala.bsp4j.DependencySourcesItem;
import ch.epfl.scala.bsp4j.DependencySourcesParams;
import ch.epfl.scala.bsp4j.DependencySourcesResult;
import ch.epfl.scala.bsp4j.InitializeBuildParams;
import ch.epfl.scala.bsp4j.InitializeBuildResult;
import ch.epfl.scala.bsp4j.InverseSourcesParams;
import ch.epfl.scala.bsp4j.InverseSourcesResult;
import ch.epfl.scala.bsp4j.ResourcesItem;
import ch.epfl.scala.bsp4j.ResourcesParams;
import ch.epfl.scala.bsp4j.ResourcesResult;
import ch.epfl.scala.bsp4j.RunParams;
import ch.epfl.scala.bsp4j.RunProvider;
import ch.epfl.scala.bsp4j.RunResult;
import ch.epfl.scala.bsp4j.SourceItem;
import ch.epfl.scala.bsp4j.SourcesItem;
import ch.epfl.scala.bsp4j.SourcesParams;
import ch.epfl.scala.bsp4j.SourcesResult;
import ch.epfl.scala.bsp4j.StatusCode;
import ch.epfl.scala.bsp4j.TestParams;
import ch.epfl.scala.bsp4j.TestProvider;
import ch.epfl.scala.bsp4j.TestResult;
import ch.epfl.scala.bsp4j.WorkspaceBuildTargetsResult;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.devtools.build.lib.query2.proto.proto2api.Build;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseError;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseErrorCode;
import org.jetbrains.bsp.bazel.common.Constants;
import org.jetbrains.bsp.bazel.common.Uri;
import org.jetbrains.bsp.bazel.server.bsp.BazelBspServerBuildManager;
import org.jetbrains.bsp.bazel.server.bsp.BazelBspServerLifetime;
import org.jetbrains.bsp.bazel.server.bsp.BazelBspServerRequestHelpers;
import org.jetbrains.bsp.bazel.server.data.BazelData;
import org.jetbrains.bsp.bazel.server.data.ProcessResults;
import org.jetbrains.bsp.bazel.server.resolver.BazelRunner;
import org.jetbrains.bsp.bazel.server.resolver.QueryResolver;
import org.jetbrains.bsp.bazel.server.util.ParsingUtils;

public class BuildServerService {

  private final BazelBspServerRequestHelpers serverRequestHelpers;
  private final BazelBspServerLifetime serverLifetime;
  private final BazelBspServerBuildManager serverBuildManager;

  private final BazelData bazelData;
  private final BazelRunner bazelRunner;
  private final QueryResolver queryResolver;

  public BuildServerService(
      BazelBspServerRequestHelpers serverRequestHelpers,
      BazelBspServerLifetime serverLifetime,
      BazelBspServerBuildManager serverBuildManager,
      BazelData bazelData,
      BazelRunner bazelRunner,
      QueryResolver queryResolver) {
    this.serverRequestHelpers = serverRequestHelpers;
    this.serverLifetime = serverLifetime;
    this.serverBuildManager = serverBuildManager;
    this.bazelData = bazelData;
    this.bazelRunner = bazelRunner;
    this.queryResolver = queryResolver;
  }

  public CompletableFuture<InitializeBuildResult> buildInitialize(
      InitializeBuildParams initializeBuildParams) {
    return processBuildInitialize(this::handleBuildInitialize);
  }

  private <T> CompletableFuture<T> processBuildInitialize(
      Supplier<Either<ResponseError, T>> request) {
    if (serverLifetime.isFinished()) {
      return serverRequestHelpers.completeExceptionally(
          new ResponseError(
              ResponseErrorCode.serverErrorEnd, "Server has already shutdown!", false));
    }

    return serverRequestHelpers.getValue(request);
  }

  private Either<ResponseError, InitializeBuildResult> handleBuildInitialize() {
    BuildServerCapabilities capabilities = new BuildServerCapabilities();
    capabilities.setCompileProvider(new CompileProvider(Constants.SUPPORTED_LANGUAGES));
    capabilities.setRunProvider(new RunProvider(Constants.SUPPORTED_LANGUAGES));
    capabilities.setTestProvider(new TestProvider(Constants.SUPPORTED_LANGUAGES));
    capabilities.setDependencySourcesProvider(true);
    capabilities.setInverseSourcesProvider(true);
    capabilities.setResourcesProvider(true);
    return Either.forRight(
        new InitializeBuildResult(
            Constants.NAME, Constants.VERSION, Constants.BSP_VERSION, capabilities));
  }

  public void onBuildInitialized() {
    serverLifetime.setInitializedComplete();
  }

  public CompletableFuture<Object> buildShutdown() {
    return processBuildShutdown(this::handleBuildShutdown);
  }

  private <T> CompletableFuture<T> processBuildShutdown(
      Supplier<Either<ResponseError, T>> request) {
    if (!serverLifetime.isInitialized()) {
      return serverRequestHelpers.completeExceptionally(
          new ResponseError(
              ResponseErrorCode.serverErrorEnd, "Server has not been initialized yet!", false));
    }

    return serverRequestHelpers.getValue(request);
  }

  private Either<ResponseError, Object> handleBuildShutdown() {
    serverLifetime.setFinishedComplete();
    return Either.forRight(new Object());
  }

  public void onBuildExit() {
    serverLifetime.forceFinish();
  }

  public CompletableFuture<WorkspaceBuildTargetsResult> workspaceBuildTargets() {
    return serverBuildManager.getWorkspaceBuildTargets();
  }

  public Either<ResponseError, SourcesResult> buildTargetSources(SourcesParams sourcesParams) {
    Build.QueryResult queryResult =
        queryResolver.getQuery(
            "query",
            "--output=proto",
            "("
                + sourcesParams.getTargets().stream()
                    .map(BuildTargetIdentifier::getUri)
                    .collect(Collectors.joining("+"))
                + ")");

    List<SourcesItem> sources =
        queryResult.getTargetList().stream()
            .map(Build.Target::getRule)
            .map(
                rule -> {
                  BuildTargetIdentifier label = new BuildTargetIdentifier(rule.getName());
                  List<SourceItem> items = serverBuildManager.getSourceItems(rule, label);
                  List<String> roots =
                      Lists.newArrayList(
                          Uri.fromAbsolutePath(serverBuildManager.getSourcesRoot(rule.getName()))
                              .toString());
                  SourcesItem item = new SourcesItem(label, items);
                  item.setRoots(roots);
                  return item;
                })
            .collect(Collectors.toList());

    return Either.forRight(new SourcesResult(sources));
  }

  public Either<ResponseError, InverseSourcesResult> buildTargetInverseSources(
      InverseSourcesParams inverseSourcesParams) {
    String fileUri = inverseSourcesParams.getTextDocument().getUri();
    String workspaceRoot = bazelData.getWorkspaceRoot();
    String prefix = Uri.fromWorkspacePath("", workspaceRoot).toString();
    if (!inverseSourcesParams.getTextDocument().getUri().startsWith(prefix)) {
      throw new RuntimeException("Could not resolve " + fileUri + " within workspace " + prefix);
    }
    Build.QueryResult result =
        queryResolver.getQuery(
            "query",
            "--output=proto",
            "kind(rule, rdeps(//..., " + fileUri.substring(prefix.length()) + ", 1))");
    List<BuildTargetIdentifier> targets =
        result.getTargetList().stream()
            .map(Build.Target::getRule)
            .map(Build.Rule::getName)
            .map(BuildTargetIdentifier::new)
            .collect(Collectors.toList());

    return Either.forRight(new InverseSourcesResult(targets));
  }

  public Either<ResponseError, DependencySourcesResult> buildTargetDependencySources(
      DependencySourcesParams dependencySourcesParams) {
    List<String> targets =
        dependencySourcesParams.getTargets().stream()
            .map(BuildTargetIdentifier::getUri)
            .collect(Collectors.toList());

    DependencySourcesResult result =
        new DependencySourcesResult(
            targets.stream()
                .sorted()
                .map(
                    target -> {
                      List<String> files =
                          serverBuildManager.lookUpTransitiveSourceJars(target).stream()
                              .map(
                                  execPath ->
                                      Uri.fromExecPath(execPath, bazelData.getExecRoot())
                                          .toString())
                              .collect(Collectors.toList());
                      return new DependencySourcesItem(new BuildTargetIdentifier(target), files);
                    })
                .collect(Collectors.toList()));

    return Either.forRight(result);
  }

  public Either<ResponseError, ResourcesResult> buildTargetResources(
      ResourcesParams resourcesParams) {
    Build.QueryResult query = queryResolver.getQuery("query", "--output=proto", "//...");
    System.out.println("Resources query result " + query);
    ResourcesResult resourcesResult =
        new ResourcesResult(
            query.getTargetList().stream()
                .map(Build.Target::getRule)
                .filter(
                    rule ->
                        resourcesParams.getTargets().stream()
                            .anyMatch(target -> target.getUri().equals(rule.getName())))
                .filter(
                    rule ->
                        rule.getAttributeList().stream()
                            .anyMatch(
                                attribute ->
                                    attribute.getName().equals("resources")
                                        && attribute.hasExplicitlySpecified()
                                        && attribute.getExplicitlySpecified()))
                .map(
                    rule ->
                        new ResourcesItem(
                            new BuildTargetIdentifier(rule.getName()),
                            serverBuildManager.getResources(rule, query)))
                .collect(Collectors.toList()));

    return Either.forRight(resourcesResult);
  }

  public Either<ResponseError, CompileResult> buildTargetCompile(CompileParams compileParams) {
    return serverBuildManager.buildTargetsWithBep(compileParams.getTargets(), new ArrayList<>());
  }

  public Either<ResponseError, TestResult> buildTargetTest(TestParams testParams) {
    Either<ResponseError, CompileResult> build =
        serverBuildManager.buildTargetsWithBep(
            Lists.newArrayList(testParams.getTargets()), new ArrayList<>());
    if (build.isLeft()) {
      return Either.forLeft(build.getLeft());
    }

    CompileResult result = build.getRight();
    if (result.getStatusCode() != StatusCode.OK) {
      return Either.forRight(new TestResult(result.getStatusCode()));
    }

    String testTargets =
        Joiner.on("+")
            .join(
                testParams.getTargets().stream()
                    .map(BuildTargetIdentifier::getUri)
                    .collect(Collectors.toList()));
    ProcessResults processResults =
        bazelRunner.runBazelCommand(
            Lists.asList(
                Constants.BAZEL_TEST_COMMAND,
                "(" + testTargets + ")",
                testParams.getArguments().toArray(new String[0])));

    return Either.forRight(
        new TestResult(ParsingUtils.parseExitCode(processResults.getExitCode())));
  }

  public Either<ResponseError, RunResult> buildTargetRun(RunParams runParams) {
    Either<ResponseError, CompileResult> build =
        serverBuildManager.buildTargetsWithBep(
            Lists.newArrayList(runParams.getTarget()), new ArrayList<>());
    if (build.isLeft()) {
      return Either.forLeft(build.getLeft());
    }

    CompileResult result = build.getRight();
    if (result.getStatusCode() != StatusCode.OK) {
      return Either.forRight(new RunResult(result.getStatusCode()));
    }

    ProcessResults processResults =
        bazelRunner.runBazelCommand(
            Lists.asList(
                Constants.BAZEL_RUN_COMMAND,
                runParams.getTarget().getUri(),
                runParams.getArguments().toArray(new String[0])));

    return Either.forRight(new RunResult(ParsingUtils.parseExitCode(processResults.getExitCode())));
  }

  public Either<ResponseError, CleanCacheResult> buildTargetCleanCache(
      CleanCacheParams cleanCacheParams) {
    CleanCacheResult result;
    try {
      result =
          new CleanCacheResult(
              String.join(
                  "\n", bazelRunner.runBazelCommand(Constants.BAZEL_CLEAN_COMMAND).getStdout()),
              true);
    } catch (RuntimeException e) {
      // TODO does it make sense to return a successful response here?
      // If we caught an exception here, there was an internal server error...
      result = new CleanCacheResult(e.getMessage(), false);
    }
    return Either.forRight(result);
  }
}
