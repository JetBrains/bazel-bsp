package org.jetbrains.bsp.bazel.server.bsp.services;

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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.devtools.build.lib.query2.proto.proto2api.Build;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseError;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseErrorCode;
import org.jetbrains.bsp.bazel.commons.Constants;
import org.jetbrains.bsp.bazel.commons.Lazy;
import org.jetbrains.bsp.bazel.commons.Uri;
import org.jetbrains.bsp.bazel.projectview.model.ProjectView;
import org.jetbrains.bsp.bazel.server.bazel.BazelProcess;
import org.jetbrains.bsp.bazel.server.bazel.BazelRunner;
import org.jetbrains.bsp.bazel.server.bazel.data.BazelData;
import org.jetbrains.bsp.bazel.server.bazel.data.BazelProcessResult;
import org.jetbrains.bsp.bazel.server.bazel.params.BazelQueryKindParameters;
import org.jetbrains.bsp.bazel.server.bazel.params.BazelRunnerFlag;
import org.jetbrains.bsp.bazel.server.bsp.BazelBspServerBuildManager;
import org.jetbrains.bsp.bazel.server.bsp.BazelBspServerLifetime;
import org.jetbrains.bsp.bazel.server.bsp.BazelBspServerRequestHelpers;
import org.jetbrains.bsp.bazel.server.bsp.resolvers.QueryResolver;
import org.jetbrains.bsp.bazel.server.bsp.resolvers.TargetRulesResolver;
import org.jetbrains.bsp.bazel.server.bsp.resolvers.TargetsUtils;

public class BuildServerService {

  private static final Logger LOGGER = LogManager.getLogger(BuildServerService.class);

  private final BazelBspServerRequestHelpers serverRequestHelpers;
  private final BazelBspServerLifetime serverLifetime;
  private final BazelBspServerBuildManager serverBuildManager;

  private final BazelData bazelData;
  private final BazelRunner bazelRunner;
  private final ProjectView projectView;

  public BuildServerService(
      BazelBspServerRequestHelpers serverRequestHelpers,
      BazelBspServerLifetime serverLifetime,
      BazelBspServerBuildManager serverBuildManager,
      BazelData bazelData,
      BazelRunner bazelRunner,
      ProjectView projectView) {
    this.serverRequestHelpers = serverRequestHelpers;
    this.serverLifetime = serverLifetime;
    this.serverBuildManager = serverBuildManager;
    this.bazelData = bazelData;
    this.bazelRunner = bazelRunner;
    this.projectView = projectView;
  }

  public CompletableFuture<InitializeBuildResult> buildInitialize(
      InitializeBuildParams initializeBuildParams) {
    LOGGER.info("buildInitialize call with param: {}", initializeBuildParams);

    return processBuildInitialize("buildInitialize", this::handleBuildInitialize);
  }

  private <T> CompletableFuture<T> processBuildInitialize(
      String methodName, Supplier<Either<ResponseError, T>> request) {
    if (serverLifetime.isFinished()) {
      return serverRequestHelpers.completeExceptionally(
          methodName,
          new ResponseError(
              ResponseErrorCode.serverErrorEnd, "Server has already shutdown!", false));
    }

    return serverRequestHelpers.getValue(methodName, request);
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
    LOGGER.info("onBuildInitialized call");

    serverLifetime.setInitializedComplete();
  }

  public CompletableFuture<Object> buildShutdown() {
    LOGGER.info("buildShutdown call");

    return processBuildShutdown("buildShutdown", this::handleBuildShutdown);
  }

  private <T> CompletableFuture<T> processBuildShutdown(
      String methodName, Supplier<Either<ResponseError, T>> request) {
    if (!serverLifetime.isInitialized()) {
      return serverRequestHelpers.completeExceptionally(
          methodName,
          new ResponseError(
              ResponseErrorCode.serverErrorEnd, "Server has not been initialized yet!", false));
    }

    return serverRequestHelpers.getValue(methodName, request);
  }

  private Either<ResponseError, Object> handleBuildShutdown() {
    serverLifetime.setFinishedComplete();
    return Either.forRight(new Object());
  }

  public void onBuildExit() {
    LOGGER.info("onBuildExit call");

    serverLifetime.forceFinish();
  }

  public CompletableFuture<WorkspaceBuildTargetsResult> workspaceBuildTargets() {
    LOGGER.info("workspaceBuildTargets call");

    return serverBuildManager.getWorkspaceBuildTargets();
  }

  public Either<ResponseError, SourcesResult> buildTargetSources(SourcesParams sourcesParams) {
    LOGGER.info("buildTargetSources call with param: {}", sourcesParams);

    TargetRulesResolver<SourcesItem> targetRulesResolver =
        TargetRulesResolver.withBazelRunnerAndMapper(bazelRunner, this::mapBuildRuleToSourcesItem);

    List<SourcesItem> sourceItems =
        targetRulesResolver.getItemsForTargets(sourcesParams.getTargets());

    SourcesResult sourcesResult = new SourcesResult(sourceItems);

    return Either.forRight(sourcesResult);
  }

  private SourcesItem mapBuildRuleToSourcesItem(Build.Rule rule) {
    BuildTargetIdentifier ruleLabel = new BuildTargetIdentifier(rule.getName());
    List<SourceItem> items = serverBuildManager.getSourceItems(rule, ruleLabel);
    List<String> roots = getRuleRoots(rule);

    return createSourcesForLabelAndItemsAndRoots(ruleLabel, items, roots);
  }

  private List<String> getRuleRoots(Build.Rule rule) {
    String sourcesRootUriString = serverBuildManager.getSourcesRoot(rule.getName());
    Uri uri = Uri.fromAbsolutePath(sourcesRootUriString);

    return ImmutableList.of(uri.toString());
  }

  private SourcesItem createSourcesForLabelAndItemsAndRoots(
      BuildTargetIdentifier label, List<SourceItem> items, List<String> roots) {
    SourcesItem item = new SourcesItem(label, items);
    item.setRoots(roots);

    return item;
  }

  public Either<ResponseError, InverseSourcesResult> buildTargetInverseSources(
      InverseSourcesParams inverseSourcesParams) {
    LOGGER.info("buildTargetInverseSources call with param: {}", inverseSourcesParams);

    String fileUri = inverseSourcesParams.getTextDocument().getUri();
    String workspaceRoot = bazelData.getWorkspaceRoot();
    String prefix = Uri.fromWorkspacePath("", workspaceRoot).toString();
    if (!inverseSourcesParams.getTextDocument().getUri().startsWith(prefix)) {
      LOGGER.error("Could not resolve {} within workspace {}", fileUri, prefix);

      throw new RuntimeException("Could not resolve " + fileUri + " within workspace " + prefix);
    }
    String kindInput = TargetsUtils.getKindInput(projectView, fileUri, prefix);
    BazelQueryKindParameters kindParameter =
        BazelQueryKindParameters.fromPatternAndInput("rule", kindInput);

    BazelProcess bazelProcess =
        bazelRunner
            .commandBuilder()
            .query()
            .withFlag(BazelRunnerFlag.OUTPUT_PROTO)
            .withKind(kindParameter)
            .executeBazelBesCommand();

    Build.QueryResult result = QueryResolver.getQueryResultForProcess(bazelProcess);

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
    LOGGER.info("buildTargetDependencySources call with param: {}", dependencySourcesParams);

    List<String> targets = TargetsUtils.getTargetsUris(dependencySourcesParams.getTargets());

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
    LOGGER.info("buildTargetResources call with param: {}", resourcesParams);

    BazelProcess bazelProcess =
        bazelRunner
            .commandBuilder()
            .query()
            .withFlag(BazelRunnerFlag.OUTPUT_PROTO)
            .withArgument(TargetsUtils.getAllProjectTargetsWithExcludedTargets(projectView))
            .executeBazelBesCommand();

    Build.QueryResult query = QueryResolver.getQueryResultForProcess(bazelProcess);

    LOGGER.info("Resources query result {}", query);

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
    LOGGER.info("buildTargetCompile call with param: {}", compileParams);
    return serverBuildManager.buildTargetsWithBep(compileParams.getTargets(), new ArrayList<>());
  }

  public Either<ResponseError, TestResult> buildTargetTest(TestParams testParams) {
    LOGGER.info("buildTargetTest call with param: {}", testParams);

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

    List<String> testTargets = TargetsUtils.getTargetsUris(testParams.getTargets());

    BazelProcessResult bazelProcessResult =
        bazelRunner
            .commandBuilder()
            .test()
            .withTargets(testTargets)
            .withArguments(testParams.getArguments())
            .executeBazelBesCommand()
            .waitAndGetResult();

    return Either.forRight(new TestResult(bazelProcessResult.getStatusCode()));
  }

  public Either<ResponseError, RunResult> buildTargetRun(RunParams runParams) {
    LOGGER.info("buildTargetRun call with param: {}", runParams);

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

    BazelProcessResult bazelProcessResult =
        bazelRunner
            .commandBuilder()
            .run()
            .withArgument(runParams.getTarget().getUri())
            .withArguments(runParams.getArguments())
            .executeBazelBesCommand()
            .waitAndGetResult();

    return Either.forRight(new RunResult(bazelProcessResult.getStatusCode()));
  }

  public Either<ResponseError, CleanCacheResult> buildTargetCleanCache(
      CleanCacheParams cleanCacheParams) {
    LOGGER.info("buildTargetCleanCache call with param: {}", cleanCacheParams);

    CleanCacheResult result;
    try {
      List<String> lines =
          bazelRunner
              .commandBuilder()
              .clean()
              .executeBazelBesCommand()
              .waitAndGetResult()
              .getStdout();

      result = new CleanCacheResult(String.join("\n", lines), true);
    } catch (RuntimeException e) {
      // TODO does it make sense to return a successful response here?
      // If we caught an exception here, there was an internal server error...
      result = new CleanCacheResult(e.getMessage(), false);
    }
    return Either.forRight(result);
  }

  public Either<ResponseError, Object> workspaceReload() {
    LOGGER.info("workspaceReload call");

    bazelRunner.commandBuilder().fetch().executeBazelBesCommand().waitAndGetResult();
    serverBuildManager.getLazyVals().forEach(Lazy::recalculateValue);

    return Either.forRight(new Object());
  }
}
