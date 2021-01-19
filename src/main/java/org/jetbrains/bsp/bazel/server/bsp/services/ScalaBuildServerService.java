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
import com.google.devtools.build.lib.query2.proto.proto2api.Build.Attribute;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseError;
import org.jetbrains.bsp.bazel.commons.Constants;
import org.jetbrains.bsp.bazel.server.bazel.BazelRunner;
import org.jetbrains.bsp.bazel.server.bazel.data.BazelData;
import org.jetbrains.bsp.bazel.server.bsp.resolvers.TargetRulesResolver;
import org.jetbrains.bsp.bazel.server.bsp.resolvers.TargetsLanguageOptionsResolver;
import org.jetbrains.bsp.bazel.server.bsp.resolvers.TargetsUtils;

public class ScalaBuildServerService {

  private static final String SCALA_COMPILER_OPTIONS_NAME = "scalacopts";
  private static final List<String> SCALA_LANGUAGES_IDS =
      ImmutableList.of(Constants.SCALAC, Constants.JAVAC);

  private static final String SCALA_TEST_RULE_NAME = "scala_test";

  private final TargetsLanguageOptionsResolver<ScalacOptionsItem> targetsLanguageOptionsResolver;
  private final TargetRulesResolver<ScalaTestClassesItem> targetsScalaTestClassesRulesResolver;

  public ScalaBuildServerService(BazelData bazelData, BazelRunner bazelRunner) {
    this.targetsLanguageOptionsResolver =
        TargetsLanguageOptionsResolver.<ScalacOptionsItem>builder()
            .bazelData(bazelData)
            .bazelRunner(bazelRunner)
            .compilerOptionsName(SCALA_COMPILER_OPTIONS_NAME)
            .languagesIds(SCALA_LANGUAGES_IDS)
            .resultItemsCollector(ScalacOptionsItem::new)
            .build();

    this.targetsScalaTestClassesRulesResolver =
        TargetRulesResolver.withBazelRunnerAndFilterAndMapper(
            bazelRunner, this::isScalaTestRule, this::mapRuleToTestClassesItem);
  }

  private boolean isScalaTestRule(Build.Rule rule) {
    return rule.getName().equals(SCALA_TEST_RULE_NAME);
  }

  private ScalaTestClassesItem mapRuleToTestClassesItem(Build.Rule rule) {
    BuildTargetIdentifier target = new BuildTargetIdentifier(rule.getName());
    List<String> classes = getTestMainClasses(rule);

    return new ScalaTestClassesItem(target, classes);
  }

  private List<String> getTestMainClasses(Build.Rule rule) {
    return rule.getAttributeList().stream()
        .filter(
            attribute ->
                TargetsUtils.isAttributeSpecifiedAndHasGivenName(
                    attribute, Constants.SCALA_TEST_MAIN_CLASSES_ATTRIBUTE_NAME))
        .map(Attribute::getStringValue)
        .collect(Collectors.toList());
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
    List<ScalaTestClassesItem> resultItems =
        targetsScalaTestClassesRulesResolver.getItemsForTargets(
            scalaTestClassesParams.getTargets());

    ScalaTestClassesResult scalaTestClassesResult = new ScalaTestClassesResult(resultItems);

    return Either.forRight(scalaTestClassesResult);
  }

  public CompletableFuture<ScalaMainClassesResult> buildTargetScalaMainClasses(
      ScalaMainClassesParams scalaMainClassesParams) {
    System.out.printf("DWH: Got buildTargetScalaMainClasses: %s%n", scalaMainClassesParams);
    // TODO(illicitonion): Populate
    return CompletableFuture.completedFuture(new ScalaMainClassesResult(new ArrayList<>()));
  }
}
