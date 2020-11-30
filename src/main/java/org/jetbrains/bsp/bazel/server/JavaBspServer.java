package org.jetbrains.bsp.bazel.server;

import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import ch.epfl.scala.bsp4j.JavacOptionsItem;
import ch.epfl.scala.bsp4j.JavacOptionsParams;
import ch.epfl.scala.bsp4j.JavacOptionsResult;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseError;
import org.jetbrains.bsp.bazel.common.ActionGraphParser;
import org.jetbrains.bsp.bazel.common.Uri;
import org.jetbrains.bsp.bazel.server.resolvers.ActionGraphResolver;
import org.jetbrains.bsp.bazel.server.resolvers.TargetsResolver;
import org.jetbrains.bsp.bazel.server.utils.MnemonicsUtils;

// TODO: This class *should* implement a `JavaBuildServer` interface,
// TODO: now `buildTargetJavacOptions` method returns a `Either<ResponseError, JavacOptionsResult>`
// TODO: instead of a `CompletableFuture<JavacOptionsResult>` because of the `BazelBspServer`
// TODO: command executing (`executeCommand`) implementation.
public class JavaBspServer {

  private final TargetsResolver targetsResolver;
  private final ActionGraphResolver actionGraphResolver;

  private final String javac;
  private final String kotlinc;
  private final String execRoot;

  // TODO: too many arguments!!, constants will be moved
  public JavaBspServer(
      TargetsResolver targetsResolver,
      ActionGraphResolver actionGraphResolver,
      String javac,
      String kotlinc,
      String execRoot) {
    this.targetsResolver = targetsResolver;
    this.actionGraphResolver = actionGraphResolver;
    this.javac = javac;
    this.kotlinc = kotlinc;
    this.execRoot = execRoot;
  }

  public Either<ResponseError, JavacOptionsResult> buildTargetJavacOptions(
      JavacOptionsParams javacOptionsParams) {
    List<String> targets =
        javacOptionsParams.getTargets().stream()
            .map(BuildTargetIdentifier::getUri)
            .collect(Collectors.toList());

    String targetsUnion = Joiner.on(" + ").join(targets);
    Map<String, List<String>> targetsOptions =
        targetsResolver.getTargetsOptions(targetsUnion, "javacopts");
    // TODO(andrefmrocha): Remove this when kotlin is natively supported
    ActionGraphParser actionGraphParser =
        actionGraphResolver.parseActionGraph(
            MnemonicsUtils.getMnemonics(targetsUnion, Lists.newArrayList(javac, kotlinc)));

    JavacOptionsResult result =
        new JavacOptionsResult(
            targets.stream()
                .flatMap(
                    target ->
                        collectJavacOptionsResult(
                            actionGraphParser,
                            targetsOptions.getOrDefault(target, new ArrayList<>()),
                            actionGraphParser.getInputsAsUri(target, execRoot),
                            target))
                .collect(Collectors.toList()));
    return Either.forRight(result);
  }

  private Stream<JavacOptionsItem> collectJavacOptionsResult(
      ActionGraphParser actionGraphParser,
      List<String> options,
      List<String> inputs,
      String target) {
    return actionGraphParser.getOutputs(target, Lists.newArrayList(".jar", ".js")).stream()
        .map(
            output ->
                new JavacOptionsItem(
                    new BuildTargetIdentifier(target),
                    options,
                    inputs,
                    Uri.fromExecPath("exec-root://" + output, execRoot).toString()));
  }
}
