package org.jetbrains.bsp.bazel.server;

import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import ch.epfl.scala.bsp4j.ScalaMainClassesParams;
import ch.epfl.scala.bsp4j.ScalaMainClassesResult;
import ch.epfl.scala.bsp4j.ScalaTestClassesParams;
import ch.epfl.scala.bsp4j.ScalaTestClassesResult;
import ch.epfl.scala.bsp4j.ScalacOptionsItem;
import ch.epfl.scala.bsp4j.ScalacOptionsParams;
import ch.epfl.scala.bsp4j.ScalacOptionsResult;
import com.google.common.collect.ImmutableList;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseError;
import org.jetbrains.bsp.bazel.common.ActionGraphParser;
import org.jetbrains.bsp.bazel.common.Constants;
import org.jetbrains.bsp.bazel.common.Uri;
import org.jetbrains.bsp.bazel.server.resolvers.ActionGraphResolver;
import org.jetbrains.bsp.bazel.server.resolvers.TargetsResolver;
import org.jetbrains.bsp.bazel.server.utils.MnemonicsUtils;

// TODO: This class *should* implement a `ScalaBuildServer` interface,
// TODO: now `buildTargetScalacOptions` method returns a `Either<ResponseError,
// TODO: ScalacOptionsResult>`
// TODO: instead of a `CompletableFuture<ScalacOptionsResult>` because of the `BazelBspServer`
// TODO: command executing (`executeCommand`) implementation.
public class ScalaBspServer {

  private final TargetsResolver targetsResolver;
  private final ActionGraphResolver actionGraphResolver;

  private final String execRoot;

  public ScalaBspServer(
      TargetsResolver targetsResolver, ActionGraphResolver actionGraphResolver, String execRoot) {
    this.targetsResolver = targetsResolver;
    this.actionGraphResolver = actionGraphResolver;
    this.execRoot = execRoot;
  }

  public Either<ResponseError, ScalacOptionsResult> buildTargetScalacOptions(
      ScalacOptionsParams scalacOptionsParams) {
    List<String> targets = targetsResolver.getTargetsUris(scalacOptionsParams.getTargets());
    Map<String, List<String>> targetsOptions = targetsResolver.getScalacTargetsOptions(targets);

    String targetsMnemonics = MnemonicsUtils.getMnemonics(targets, ImmutableList.of(Constants.SCALAC, Constants.JAVAC));
    ActionGraphParser actionGraphParser = actionGraphResolver.parseActionGraph(targetsMnemonics);

    return buildTargetScalacOptionsResult(targets, targetsOptions, actionGraphParser);
  }

  private Either<ResponseError, ScalacOptionsResult> buildTargetScalacOptionsResult(List<String> targets,
      Map<String, List<String>> targetsOptions, ActionGraphParser actionGraphParser) {
    List<ScalacOptionsItem> scalacOptionsItems = getScalacOptionsResultItems(targets, targetsOptions,
        actionGraphParser);
    ScalacOptionsResult scalacOptionsResult = new ScalacOptionsResult(scalacOptionsItems);

    return Either.forRight(scalacOptionsResult);
  }

  private List<ScalacOptionsItem> getScalacOptionsResultItems(List<String> targets,
      Map<String, List<String>> targetsOptions,
      ActionGraphParser actionGraphParser) {
    return targets.stream()
        .flatMap(target ->
            collectScalacOptionsResult(
                actionGraphParser,
                targetsOptions.getOrDefault(target, new ArrayList<>()),
                actionGraphParser.getInputsAsUri(target, execRoot),
                target))
        .collect(Collectors.toList());
  }

  private Stream<ScalacOptionsItem> collectScalacOptionsResult(ActionGraphParser actionGraphParser,
      List<String> options, List<String> inputs, String target) {
    List<String> suffixes = ImmutableList.of(".jar", ".js");

    return actionGraphParser.getOutputs(target, suffixes).stream()
        .map(output -> buildScalacOptionsItem(options, inputs, target, output));
  }

  private ScalacOptionsItem buildScalacOptionsItem(List<String> options, List<String> inputs, String target,
      String output) {
    BuildTargetIdentifier buildTargetIdentifier = new BuildTargetIdentifier(target);
    String execRootUri = Uri.fromExecPath("exec-root://" + output, execRoot).toString();

    return new ScalacOptionsItem(buildTargetIdentifier, options, inputs, execRootUri);
  }

  public CompletableFuture<ScalaTestClassesResult> buildTargetScalaTestClasses(
      ScalaTestClassesParams scalaTestClassesParams) {
    return CompletableFuture.completedFuture(new ScalaTestClassesResult(new ArrayList<>()));
  }

  public CompletableFuture<ScalaMainClassesResult> buildTargetScalaMainClasses(
      ScalaMainClassesParams scalaMainClassesParams) {
    System.out.printf("DWH: Got buildTargetScalaMainClasses: %s%n", scalaMainClassesParams);
    // TODO(illicitonion): Populate
    return CompletableFuture.completedFuture(new ScalaMainClassesResult(new ArrayList<>()));
  }
}
