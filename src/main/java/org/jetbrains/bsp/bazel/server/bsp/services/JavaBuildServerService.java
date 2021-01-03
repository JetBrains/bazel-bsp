package org.jetbrains.bsp.bazel.server.bsp.services;

import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import ch.epfl.scala.bsp4j.JavacOptionsItem;
import ch.epfl.scala.bsp4j.JavacOptionsParams;
import ch.epfl.scala.bsp4j.JavacOptionsResult;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseError;
import org.jetbrains.bsp.bazel.commons.Constants;
import org.jetbrains.bsp.bazel.commons.Uri;
import org.jetbrains.bsp.bazel.server.bazel.data.BazelData;
import org.jetbrains.bsp.bazel.server.bsp.resolvers.ActionGraphResolver;
import org.jetbrains.bsp.bazel.server.bsp.resolvers.TargetsResolver;
import org.jetbrains.bsp.bazel.server.bsp.resolvers.ActionGraphParser;

public class JavaBuildServerService {

  private final BazelData bazelData;
  private final TargetsResolver targetsResolver;
  private final ActionGraphResolver actionGraphResolver;

  public JavaBuildServerService(
      BazelData bazelData,
      TargetsResolver targetsResolver,
      ActionGraphResolver actionGraphResolver) {
    this.bazelData = bazelData;
    this.targetsResolver = targetsResolver;
    this.actionGraphResolver = actionGraphResolver;
  }

  public Either<ResponseError, JavacOptionsResult> buildTargetJavacOptions(
      JavacOptionsParams javacOptionsParams) {
    List<String> targets =
        javacOptionsParams.getTargets().stream()
            .map(BuildTargetIdentifier::getUri)
            .collect(Collectors.toList());

    Map<String, List<String>> targetsOptions =
        targetsResolver.getTargetsOptions(targets, "javacopts");
    // TODO(andrefmrocha): Remove this when kotlin is natively supported
    ActionGraphParser actionGraphParser =
        actionGraphResolver.getActionGraphParser(
            targets, ImmutableList.of(Constants.JAVAC, Constants.KOTLINC));

    JavacOptionsResult result =
        new JavacOptionsResult(
            targets.stream()
                .flatMap(
                    target ->
                        collectJavacOptionsResult(
                            actionGraphParser,
                            targetsOptions.getOrDefault(target, new ArrayList<>()),
                            actionGraphParser.getInputsAsUri(target, bazelData.getExecRoot()),
                            target))
                .collect(Collectors.toList()));
    return Either.forRight(result);
  }

  private Stream<JavacOptionsItem> collectJavacOptionsResult(
      ActionGraphParser actionGraphParser,
      List<String> options,
      List<String> inputs,
      String target) {
    return actionGraphParser.getOutputs(target, ImmutableList.of(".jar", ".js")).stream()
        .map(
            output ->
                new JavacOptionsItem(
                    new BuildTargetIdentifier(target),
                    options,
                    inputs,
                    Uri.fromExecPath(Constants.EXEC_ROOT_PREFIX + output, bazelData.getExecRoot())
                        .toString()));
  }
}
