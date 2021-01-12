package org.jetbrains.bsp.bazel.server.bsp.services;

import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import ch.epfl.scala.bsp4j.ScalaMainClassesParams;
import ch.epfl.scala.bsp4j.ScalaMainClassesResult;
import ch.epfl.scala.bsp4j.ScalaTestClassesParams;
import ch.epfl.scala.bsp4j.ScalaTestClassesResult;
import ch.epfl.scala.bsp4j.ScalacOptionsItem;
import ch.epfl.scala.bsp4j.ScalacOptionsParams;
import ch.epfl.scala.bsp4j.ScalacOptionsResult;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseError;
import org.jetbrains.bsp.bazel.commons.Constants;
import org.jetbrains.bsp.bazel.commons.Uri;
import org.jetbrains.bsp.bazel.server.bazel.BazelRunner;
import org.jetbrains.bsp.bazel.server.bazel.data.BazelData;
import org.jetbrains.bsp.bazel.server.bsp.resolvers.ActionGraphParser;
import org.jetbrains.bsp.bazel.server.bsp.resolvers.ActionGraphResolver;
import org.jetbrains.bsp.bazel.server.bsp.resolvers.targets.TargetsResolver;
import org.jetbrains.bsp.bazel.server.bsp.resolvers.targets.TargetsUtils;

public class ScalaBuildServerService {

  private static final String SCALA_COMPILER_OPTIONS_NAME = "scalacopts";

  private final BazelData bazelData;
  private final TargetsResolver targetsResolver;
  private final ActionGraphResolver actionGraphResolver;

  public ScalaBuildServerService(
      BazelData bazelData,
      BazelRunner bazelRunner,
      ActionGraphResolver actionGraphResolver) {
    this.bazelData = bazelData;
    this.actionGraphResolver = actionGraphResolver;

    this.targetsResolver = new TargetsResolver(bazelData, bazelRunner, SCALA_COMPILER_OPTIONS_NAME);
  }

  public Either<ResponseError, ScalacOptionsResult> buildTargetScalacOptions(
      ScalacOptionsParams scalacOptionsParams) {
    List<String> targets = TargetsUtils.getTargetsUris(scalacOptionsParams.getTargets());

    Map<String, List<String>> targetsOptions = targetsResolver.getTargetsOptions(targets);
    ActionGraphParser actionGraphParser =
        actionGraphResolver.getActionGraphParser(
            targets, ImmutableList.of(Constants.SCALAC, Constants.JAVAC));

    ScalacOptionsResult result =
        new ScalacOptionsResult(
            targets.stream()
                .flatMap(
                    target ->
                        collectScalacOptionsResult(
                            actionGraphParser,
                            targetsOptions.getOrDefault(target, new ArrayList<>()),
                            actionGraphParser.getInputsAsUri(target, bazelData.getExecRoot()),
                            target))
                .collect(Collectors.toList()));
    return Either.forRight(result);
  }

  public CompletableFuture<ScalaTestClassesResult> buildTargetScalaTestClasses(
      ScalaTestClassesParams scalaTestClassesParams) {
    System.out.printf("DWH: Got buildTargetScalaTestClasses: %s%n", scalaTestClassesParams);
    // TODO(illicitonion): Populate
    return CompletableFuture.completedFuture(new ScalaTestClassesResult(new ArrayList<>()));
  }

  public CompletableFuture<ScalaMainClassesResult> buildTargetScalaMainClasses(
      ScalaMainClassesParams scalaMainClassesParams) {
    System.out.printf("DWH: Got buildTargetScalaMainClasses: %s%n", scalaMainClassesParams);
    // TODO(illicitonion): Populate
    return CompletableFuture.completedFuture(new ScalaMainClassesResult(new ArrayList<>()));
  }

  private Stream<ScalacOptionsItem> collectScalacOptionsResult(
      ActionGraphParser actionGraphParser,
      List<String> options,
      List<String> inputs,
      String target) {
    List<String> suffixes = ImmutableList.of(".jar", ".js");

    return actionGraphParser.getOutputs(target, suffixes).stream()
        .map(
            output ->
                new ScalacOptionsItem(
                    new BuildTargetIdentifier(target),
                    options,
                    inputs,
                    Uri.fromExecPath(Constants.EXEC_ROOT_PREFIX + output, bazelData.getExecRoot())
                        .toString()));
  }
}
