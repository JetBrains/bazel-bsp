package org.jetbrains.bsp.bazel.server.sync;

import static org.jetbrains.bsp.bazel.server.sync.BspMappings.getModules;
import static org.jetbrains.bsp.bazel.server.sync.BspMappings.toBspUri;

import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import ch.epfl.scala.bsp4j.CleanCacheParams;
import ch.epfl.scala.bsp4j.CleanCacheResult;
import ch.epfl.scala.bsp4j.CompileParams;
import ch.epfl.scala.bsp4j.CompileResult;
import ch.epfl.scala.bsp4j.RunParams;
import ch.epfl.scala.bsp4j.RunResult;
import ch.epfl.scala.bsp4j.TestParams;
import ch.epfl.scala.bsp4j.TestResult;
import io.vavr.collection.Set;
import java.util.Collections;
import java.util.List;
import org.eclipse.lsp4j.jsonrpc.ResponseErrorException;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseError;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseErrorCode;
import org.jetbrains.bsp.bazel.bazelrunner.BazelRunner;
import org.jetbrains.bsp.bazel.bazelrunner.data.BazelProcessResult;
import org.jetbrains.bsp.bazel.server.bsp.managers.BazelBspCompilationManager;
import org.jetbrains.bsp.bazel.server.sync.model.Module;
import org.jetbrains.bsp.bazel.server.sync.model.Tag;

public class ExecuteService {

  private final BazelBspCompilationManager compilationManager;
  private final ProjectProvider projectProvider;
  private final BazelRunner bazelRunner;

  public ExecuteService(
      BazelBspCompilationManager compilationManager,
      ProjectProvider projectProvider,
      BazelRunner bazelRunner) {
    this.compilationManager = compilationManager;
    this.projectProvider = projectProvider;
    this.bazelRunner = bazelRunner;
  }

  public CompileResult compile(CompileParams params) {
    var targets = selectTargets(params.getTargets());
    var result = build(targets);
    return new CompileResult(result.getStatusCode());
  }

  public TestResult test(TestParams params) {
    var targets = selectTargets(params.getTargets());
    var result = build(targets);

    if (result.isNotSuccess()) {
      return new TestResult(result.getStatusCode());
    }

    result =
        bazelRunner
            .commandBuilder()
            .test()
            .withTargets(targets.map(BspMappings::toBspUri).toJavaList())
            .withArguments(params.getArguments())
            .executeBazelBesCommand()
            .waitAndGetResult();

    return new TestResult(result.getStatusCode());
  }

  public RunResult run(RunParams params) {
    var targets = selectTargets(List.of(params.getTarget()));

    if (targets.isEmpty()) {
      throw new ResponseErrorException(
          new ResponseError(
              ResponseErrorCode.InvalidRequest,
              "No supported target found for " + params.getTarget().getUri(),
              null));
    }

    var bspId = targets.single();

    var result = build(targets);

    if (result.isNotSuccess()) {
      return new RunResult(result.getStatusCode());
    }

    var bazelProcessResult =
        bazelRunner
            .commandBuilder()
            .run()
            .withArgument(toBspUri(bspId))
            .withArguments(params.getArguments())
            .executeBazelBesCommand()
            .waitAndGetResult();

    return new RunResult(bazelProcessResult.getStatusCode());
  }

  public CleanCacheResult clean(CleanCacheParams params) {
    var bazelResult =
        bazelRunner.commandBuilder().clean().executeBazelBesCommand().waitAndGetResult();

    return new CleanCacheResult(bazelResult.getStdout(), true);
  }

  private BazelProcessResult build(Set<BuildTargetIdentifier> bspIds) {
    return compilationManager
        .buildTargetsWithBep(bspIds.toJavaList(), List.of(), Collections.emptyList())
        .processResult();
  }

  private Set<BuildTargetIdentifier> selectTargets(java.util.List<BuildTargetIdentifier> targets) {
    var project = projectProvider.get();
    var modules = getModules(project, targets);
    var modulesToBuild = modules.filter(this::isBuildable);
    return modulesToBuild.map(BspMappings::toBspId);
  }

  private boolean isBuildable(Module m) {
    return !m.isSynthetic() && !m.tags().contains(Tag.NO_BUILD);
  }
}
