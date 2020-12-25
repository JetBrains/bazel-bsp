package org.jetbrains.bsp.bazel.server;

import ch.epfl.scala.bsp4j.BuildServer;
import ch.epfl.scala.bsp4j.BuildServerCapabilities;
import ch.epfl.scala.bsp4j.BuildTarget;
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
import org.jetbrains.bsp.bazel.server.data.ProcessResults;
import org.jetbrains.bsp.bazel.server.utils.ParsingUtils;

public class BuildServerImpl implements BuildServer {

  // TODO won't be cyclical, make dependencies more organised
  // TODO generally a more sensible dependency with this class, now just calls all of its methods
  private final BazelBspServer bazelBspServer;

  private final BazelBspServerLifetime serverLifetime;
  private final BazelBspServerRequestHelpers serverRequestHelpers;

  public BuildServerImpl(
      BazelBspServer bazelBspServer,
      BazelBspServerLifetime serverLifetime,
      BazelBspServerRequestHelpers serverRequestHelpers) {
    this.bazelBspServer = bazelBspServer;
    this.serverLifetime = serverLifetime;
    this.serverRequestHelpers = serverRequestHelpers;
  }

  @Override
  public CompletableFuture<InitializeBuildResult> buildInitialize(
      InitializeBuildParams initializeBuildParams) {
    return handleBuildInitialize(
        () -> {
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
        });
  }

  private <T> CompletableFuture<T> handleBuildInitialize(
      Supplier<Either<ResponseError, T>> request) {
    if (serverLifetime.isFinished()) {
      return serverRequestHelpers.completeExceptionally(
          new ResponseError(
              ResponseErrorCode.serverErrorEnd, "Server has already shutdown!", false));
    }

    return serverRequestHelpers.getValue(request);
  }

  @Override
  public void onBuildInitialized() {
    serverLifetime.setInitializedComplete();
  }

  @Override
  public CompletableFuture<Object> buildShutdown() {
    return handleBuildShutdown(
        () -> {
          serverLifetime.setFinishedComplete();
          return Either.forRight(new Object());
        });
  }

  private <T> CompletableFuture<T> handleBuildShutdown(Supplier<Either<ResponseError, T>> request) {
    if (!serverLifetime.isInitialized()) {
      return serverRequestHelpers.completeExceptionally(
          new ResponseError(
              ResponseErrorCode.serverErrorEnd, "Server has not been initialized yet!", false));
    }

    return serverRequestHelpers.getValue(request);
  }

  @Override
  public void onBuildExit() {
    serverLifetime.forceFinished();
  }

  @Override
  public CompletableFuture<WorkspaceBuildTargetsResult> workspaceBuildTargets() {
    return serverRequestHelpers.executeCommand(
        () -> {
          // TODO pass configuration instead of calling a method
          List<String> projectPaths = bazelBspServer.getConfiguration().getTargetProjectPaths();
          List<BuildTarget> targets = new ArrayList<>();
          for (String projectPath : projectPaths) {
            targets.addAll(getBuildTarget(projectPath));
          }
          return Either.forRight(new WorkspaceBuildTargetsResult(targets));
        });
  }

  private List<BuildTarget> getBuildTarget(String projectPath) {
    Build.QueryResult queryResult =
        bazelBspServer
            .getQueryResolver()
            .getQuery(
                "query",
                "--output=proto",
                "--nohost_deps",
                "--noimplicit_deps",
                String.format(
                    "kind(binary, %s:all) union kind(library, %s:all) union kind(test, %s:all)",
                    projectPath, projectPath, projectPath));
    return queryResult.getTargetList().stream()
        .map(Build.Target::getRule)
        .filter(rule -> !rule.getRuleClass().equals("filegroup"))
        .map(bazelBspServer::getBuildTarget)
        .collect(Collectors.toList());
  }

  @Override
  public CompletableFuture<SourcesResult> buildTargetSources(SourcesParams sourcesParams) {
    return serverRequestHelpers.executeCommand(
        () -> {
          Build.QueryResult queryResult =
              bazelBspServer
                  .getQueryResolver()
                  .getQuery(
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
                        List<SourceItem> items = bazelBspServer.getSourceItems(rule, label);
                        List<String> roots =
                            Lists.newArrayList(
                                Uri.fromAbsolutePath(bazelBspServer.getSourcesRoot(rule.getName()))
                                    .toString());
                        SourcesItem item = new SourcesItem(label, items);
                        item.setRoots(roots);
                        return item;
                      })
                  .collect(Collectors.toList());
          return Either.forRight(new SourcesResult(sources));
        });
  }

  @Override
  public CompletableFuture<InverseSourcesResult> buildTargetInverseSources(
      InverseSourcesParams inverseSourcesParams) {
    return serverRequestHelpers.executeCommand(
        () -> {
          String fileUri = inverseSourcesParams.getTextDocument().getUri();
          String workspaceRoot = bazelBspServer.getBazelData().getWorkspaceRoot();
          String prefix = Uri.fromWorkspacePath("", workspaceRoot).toString();
          if (!inverseSourcesParams.getTextDocument().getUri().startsWith(prefix)) {
            throw new RuntimeException(
                "Could not resolve " + fileUri + " within workspace " + prefix);
          }
          Build.QueryResult result =
              bazelBspServer
                  .getQueryResolver()
                  .getQuery(
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
        });
  }

  @Override
  public CompletableFuture<DependencySourcesResult> buildTargetDependencySources(
      DependencySourcesParams dependencySourcesParams) {
    return serverRequestHelpers.executeCommand(
        () -> {
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
                                bazelBspServer.lookupTransitiveSourceJars(target).stream()
                                    .map(
                                        execPath ->
                                            Uri.fromExecPath(
                                                    execPath,
                                                    bazelBspServer.getBazelData().getExecRoot())
                                                .toString())
                                    .collect(Collectors.toList());
                            return new DependencySourcesItem(
                                new BuildTargetIdentifier(target), files);
                          })
                      .collect(Collectors.toList()));
          return Either.forRight(result);
        });
  }

  @Override
  public CompletableFuture<ResourcesResult> buildTargetResources(ResourcesParams resourcesParams) {
    return serverRequestHelpers.executeCommand(
        () -> {
          Build.QueryResult query =
              bazelBspServer.getQueryResolver().getQuery("query", "--output=proto", "//...");
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
                                  bazelBspServer.getResources(rule, query)))
                      .collect(Collectors.toList()));
          return Either.forRight(resourcesResult);
        });
  }

  @Override
  public CompletableFuture<CompileResult> buildTargetCompile(CompileParams compileParams) {
    return serverRequestHelpers.executeCommand(
        () -> bazelBspServer.buildTargetsWithBep(compileParams.getTargets(), new ArrayList<>()));
  }

  @Override
  public CompletableFuture<TestResult> buildTargetTest(TestParams testParams) {
    return serverRequestHelpers.executeCommand(
        () -> {
          Either<ResponseError, CompileResult> build =
              bazelBspServer.buildTargetsWithBep(
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
              bazelBspServer
                  .getBazelRunner()
                  .runBazelCommand(
                      Lists.asList(
                          Constants.BAZEL_TEST_COMMAND,
                          "(" + testTargets + ")",
                          testParams.getArguments().toArray(new String[0])));

          return Either.forRight(
              new TestResult(ParsingUtils.parseExitCode(processResults.getExitCode())));
        });
  }

  @Override
  public CompletableFuture<RunResult> buildTargetRun(RunParams runParams) {
    return serverRequestHelpers.executeCommand(
        () -> {
          Either<ResponseError, CompileResult> build =
              bazelBspServer.buildTargetsWithBep(
                  Lists.newArrayList(runParams.getTarget()), new ArrayList<>());
          if (build.isLeft()) {
            return Either.forLeft(build.getLeft());
          }

          CompileResult result = build.getRight();
          if (result.getStatusCode() != StatusCode.OK) {
            return Either.forRight(new RunResult(result.getStatusCode()));
          }

          ProcessResults processResults =
              bazelBspServer
                  .getBazelRunner()
                  .runBazelCommand(
                      Lists.asList(
                          Constants.BAZEL_RUN_COMMAND,
                          runParams.getTarget().getUri(),
                          runParams.getArguments().toArray(new String[0])));

          return Either.forRight(
              new RunResult(ParsingUtils.parseExitCode(processResults.getExitCode())));
        });
  }

  @Override
  public CompletableFuture<CleanCacheResult> buildTargetCleanCache(
      CleanCacheParams cleanCacheParams) {
    return serverRequestHelpers.executeCommand(
        () -> {
          CleanCacheResult result;
          try {
            result =
                new CleanCacheResult(
                    String.join(
                        "\n",
                        bazelBspServer
                            .getBazelRunner()
                            .runBazelCommand(Constants.BAZEL_CLEAN_COMMAND)
                            .getStdout()),
                    true);
          } catch (RuntimeException e) {
            // TODO does it make sense to return a successful response here?
            // If we caught an exception here, there was an internal server error...
            result = new CleanCacheResult(e.getMessage(), false);
          }
          return Either.forRight(result);
        });
  }
}
