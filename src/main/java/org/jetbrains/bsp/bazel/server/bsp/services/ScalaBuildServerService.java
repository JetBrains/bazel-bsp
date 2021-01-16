package org.jetbrains.bsp.bazel.server.bsp.services;

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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseError;
import org.jetbrains.bsp.bazel.commons.Constants;
import org.jetbrains.bsp.bazel.commons.Uri;
import org.jetbrains.bsp.bazel.server.bazel.BazelRunner;
import org.jetbrains.bsp.bazel.server.bazel.data.BazelData;
import org.jetbrains.bsp.bazel.server.bsp.resolvers.TargetRulesResolver;
import org.jetbrains.bsp.bazel.server.bsp.resolvers.TargetsLanguageOptionsResolver;

public class ScalaBuildServerService {

  private static final String SCALA_COMPILER_OPTIONS_NAME = "scalacopts";
  private static final List<String> SCALA_LANGUAGES_IDS =
      ImmutableList.of(Constants.SCALAC, Constants.JAVAC);

  private final BazelRunner bazelRunner;
  private final String workspaceRoot;
  private final TargetsLanguageOptionsResolver<ScalacOptionsItem> targetsLanguageOptionsResolver;

  public ScalaBuildServerService(BazelData bazelData, BazelRunner bazelRunner) {
    this.bazelRunner = bazelRunner;
    this.workspaceRoot = bazelData.getWorkspaceRoot();
    this.targetsLanguageOptionsResolver =
        TargetsLanguageOptionsResolver.<ScalacOptionsItem>builder()
            .bazelData(bazelData)
            .bazelRunner(bazelRunner)
            .compilerOptionsName(SCALA_COMPILER_OPTIONS_NAME)
            .languagesIds(SCALA_LANGUAGES_IDS)
            .resultItemsCollector(ScalacOptionsItem::new)
            .build();
  }

  public Either<ResponseError, ScalacOptionsResult> buildTargetScalacOptions(
      ScalacOptionsParams scalacOptionsParams) {
    List<ScalacOptionsItem> resultItems =
        targetsLanguageOptionsResolver.getResultItemsForTargets(scalacOptionsParams.getTargets());

    ScalacOptionsResult javacOptionsResult = new ScalacOptionsResult(resultItems);
    return Either.forRight(javacOptionsResult);
  }

  public Either<ResponseError, ScalaTestClassesResult> buildTargetScalaTestClasses(
      ScalaTestClassesParams scalaTestClassesParams) {
    TargetRulesResolver<ScalaTestClassesItem> targetRulesResolver =
        TargetRulesResolver.withBazelRunnerAndFilterAndMapper(
            bazelRunner,
            this::doesAttributesContainMainClass,
            this::map);

    List<ScalaTestClassesItem> resultItems =
        targetRulesResolver.getItemsForTargets(scalaTestClassesParams.getTargets());

    ScalaTestClassesResult scalaTestClassesResult = new ScalaTestClassesResult(resultItems);

    return Either.forRight(scalaTestClassesResult);
  }

  private boolean doesAttributesContainMainClass(Build.Rule rule) {
    return rule.getAttributeList().stream()
        .anyMatch(
                attribute ->
                    attribute.getName().equals("main_class")
                        && attribute.hasExplicitlySpecified()
                        && attribute.getExplicitlySpecified());
  }

  private ScalaTestClassesItem map(Build.Rule rule) {
    return new ScalaTestClassesItem(
        new BuildTargetIdentifier(rule.getName()),
        getTestClasses(rule));
  }

  private List<String> getTestClasses(Build.Rule rule) {
    return rule.getAttributeList().stream()
        .filter(
            attribute ->
                attribute.getName().equals("main_class")
                    && attribute.hasExplicitlySpecified()
                    && attribute.getExplicitlySpecified())
        .flatMap(attribute -> attribute.getStringListValueList().stream()
            .map(label -> Uri.fromFileLabel(label, workspaceRoot).toString()))
        .collect(Collectors.toList());
  }

  public CompletableFuture<ScalaMainClassesResult> buildTargetScalaMainClasses(
      ScalaMainClassesParams scalaMainClassesParams) {
    System.out.printf("DWH: Got buildTargetScalaMainClasses: %s%n", scalaMainClassesParams);
    // TODO(illicitonion): Populate
    return CompletableFuture.completedFuture(new ScalaMainClassesResult(new ArrayList<>()));
  }
}
