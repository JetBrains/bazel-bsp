package org.jetbrains.bsp.bazel.server;

import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import ch.epfl.scala.bsp4j.ScalaMainClassesParams;
import ch.epfl.scala.bsp4j.ScalaMainClassesResult;
import ch.epfl.scala.bsp4j.ScalaTestClassesItem;
import ch.epfl.scala.bsp4j.ScalaTestClassesParams;
import ch.epfl.scala.bsp4j.ScalaTestClassesResult;
import ch.epfl.scala.bsp4j.ScalacOptionsItem;
import ch.epfl.scala.bsp4j.ScalacOptionsParams;
import ch.epfl.scala.bsp4j.ScalacOptionsResult;
import com.google.common.collect.ImmutableList;
import com.google.devtools.build.lib.query2.proto.proto2api.Build;
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
import org.jetbrains.bsp.bazel.server.data.BazelData;
import org.jetbrains.bsp.bazel.server.resolvers.ActionGraphResolver;
import org.jetbrains.bsp.bazel.server.resolvers.BazelQueryRunner;
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
  private final BazelQueryRunner bazelQueryRunner;
  private final BazelData bazelData;

  private final String execRoot;

  public ScalaBspServer(
      TargetsResolver targetsResolver, ActionGraphResolver actionGraphResolver,
      BazelQueryRunner bazelQueryRunner,
      BazelData bazelData,
      String execRoot) {
    this.targetsResolver = targetsResolver;
    this.actionGraphResolver = actionGraphResolver;
    this.bazelQueryRunner = bazelQueryRunner;
    this.bazelData = bazelData;
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
                targetsOptions.getOrDefault(target, ImmutableList.of()),
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

  public Either<ResponseError, ScalaTestClassesResult> buildTargetScalaTestClasses(
      ScalaTestClassesParams scalaTestClassesParams) {
    Build.QueryResult query = bazelQueryRunner.queryAllTargets();
    ScalaTestClassesResult scalaTestClassesResult =
        new ScalaTestClassesResult(
            query.getTargetList().stream()
                .map(Build.Target::getRule)
                .filter(
                    rule ->
                        scalaTestClassesParams.getTargets().stream()
                            .anyMatch(target -> target.getUri().equals(rule.getName())))
                .filter(
                    rule ->
                        rule.getAttributeList().stream()
                            .anyMatch(
                                attribute ->
                                    attribute.getName().equals("main_class")
                                        && attribute.hasExplicitlySpecified()
                                        && attribute.getExplicitlySpecified()))
                .map(
                    rule ->
                        new ScalaTestClassesItem(
                            new BuildTargetIdentifier(rule.getName()),
                            getTestClasses(rule, query)))
                .collect(Collectors.toList()));
    return Either.forRight(scalaTestClassesResult);
  }

  private List<String> getTestClasses(Build.Rule rule, Build.QueryResult queryResult) {
    return rule.getAttributeList().stream()
        .filter(
            attribute ->
                attribute.getName().equals("main_class")
                    && attribute.hasExplicitlySpecified()
                    && attribute.getExplicitlySpecified())
        .flatMap(attribute -> attribute.getStringListValueList().stream()
            .map(label -> Uri.fromFileLabel(label, bazelData.getWorkspaceRoot()).toString())
        )
        .collect(Collectors.toList());
  }

  public CompletableFuture<ScalaMainClassesResult> buildTargetScalaMainClasses(
      ScalaMainClassesParams scalaMainClassesParams) {
    System.out.printf("DWH: Got buildTargetScalaMainClasses: %s%n", scalaMainClassesParams);
    // TODO(illicitonion): Populate
    return CompletableFuture.completedFuture(new ScalaMainClassesResult(ImmutableList.of()));
  }
}
