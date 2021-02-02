package org.jetbrains.bsp.bazel.server.bsp.services;

import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import ch.epfl.scala.bsp4j.ScalaMainClass;
import ch.epfl.scala.bsp4j.ScalaMainClassesItem;
import ch.epfl.scala.bsp4j.ScalaMainClassesParams;
import ch.epfl.scala.bsp4j.ScalaMainClassesResult;
import ch.epfl.scala.bsp4j.ScalaTestClassesParams;
import ch.epfl.scala.bsp4j.ScalaTestClassesResult;
import ch.epfl.scala.bsp4j.ScalacOptionsItem;
import ch.epfl.scala.bsp4j.ScalacOptionsParams;
import ch.epfl.scala.bsp4j.ScalacOptionsResult;
import com.google.common.collect.ImmutableList;
import com.google.devtools.build.lib.query2.proto.proto2api.Build;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseError;
import org.jetbrains.bsp.bazel.commons.Constants;
import org.jetbrains.bsp.bazel.server.bazel.BazelRunner;
import org.jetbrains.bsp.bazel.server.bazel.data.BazelData;
import org.jetbrains.bsp.bazel.server.bsp.resolvers.TargetRulesResolver;
import org.jetbrains.bsp.bazel.server.bsp.resolvers.TargetsLanguageOptionsResolver;
import org.jetbrains.bsp.bazel.server.bsp.resolvers.TargetsUtils;

public class ScalaBuildServerService {

  private static final String SCALA_COMPILER_OPTIONS_ATTR_NAME = "scalacopts";
  private static final List<String> SCALA_LANGUAGES_IDS =
      ImmutableList.of(Constants.SCALAC, Constants.JAVAC);

  private final TargetsLanguageOptionsResolver<ScalacOptionsItem> targetsLanguageOptionsResolver;
  private final TargetRulesResolver<ScalaMainClassesItem> mainClassesItemTargetRulesResolver;

  public ScalaBuildServerService(BazelData bazelData, BazelRunner bazelRunner) {
    this.targetsLanguageOptionsResolver =
        TargetsLanguageOptionsResolver.<ScalacOptionsItem>builder()
            .bazelData(bazelData)
            .bazelRunner(bazelRunner)
            .compilerOptionsName(SCALA_COMPILER_OPTIONS_ATTR_NAME)
            .languagesIds(SCALA_LANGUAGES_IDS)
            .resultItemsCollector(ScalacOptionsItem::new)
            .build();
    this.mainClassesItemTargetRulesResolver =
        TargetRulesResolver.withBazelRunnerAndMapper(bazelRunner, this::mapRuleToMainClassesItem);
  }

  public Either<ResponseError, ScalacOptionsResult> buildTargetScalacOptions(
      ScalacOptionsParams scalacOptionsParams) {

    List<ScalacOptionsItem> resultItems =
        targetsLanguageOptionsResolver.getResultItemsForTargets(scalacOptionsParams.getTargets());

    ScalacOptionsResult scalacOptionsResult = new ScalacOptionsResult(resultItems);
    return Either.forRight(scalacOptionsResult);
  }

  public CompletableFuture<ScalaTestClassesResult> buildTargetScalaTestClasses(
      ScalaTestClassesParams scalaTestClassesParams) {
    System.out.printf("DWH: Got buildTargetScalaTestClasses: %s%n", scalaTestClassesParams);
    // TODO(illicitonion): Populate
    return CompletableFuture.completedFuture(new ScalaTestClassesResult(new ArrayList<>()));
  }

  public Either<ResponseError, ScalaMainClassesResult> buildTargetScalaMainClasses(
      ScalaMainClassesParams scalaMainClassesParams) {

    List<ScalaMainClassesItem> resultItems =
        mainClassesItemTargetRulesResolver.getItemsForTargets(scalaMainClassesParams.getTargets());

    ScalaMainClassesResult result = new ScalaMainClassesResult(resultItems);

    return Either.forRight(result);
  }

  private ScalaMainClassesItem mapRuleToMainClassesItem(Build.Rule rule) {
    BuildTargetIdentifier targetId = new BuildTargetIdentifier(rule.getName());
    List<ScalaMainClass> mainClasses = collectMainClasses(rule);
    return new ScalaMainClassesItem(targetId, mainClasses);
  }

  private List<ScalaMainClass> collectMainClasses(Build.Rule rule) {
    List<String> targetOptions =
        collectAttributesFromStringListValues(rule, Constants.JVM_FLAGS_ATTR_NAME);
    List<String> mainClassesNames =
        collectAttributesFromStringValues(rule, Constants.MAIN_CLASS_ATTR_NAME);
    List<String> arguments = collectAttributesFromStringListValues(rule, Constants.ARGS_ATTR_NAME);
    return mainClassesNames.stream()
        .map(mainClassName -> new ScalaMainClass(mainClassName, arguments, targetOptions))
        .collect(Collectors.toList());
  }

  private List<String> collectAttributesFromStringListValues(Build.Rule rule, String attrName) {
    return getAttribute(rule, attrName)
        .flatMap(attr -> attr.getStringListValueList().stream())
        .collect(Collectors.toList());
  }

  private List<String> collectAttributesFromStringValues(Build.Rule rule, String attrName) {
    return getAttribute(rule, attrName)
        .map(Build.Attribute::getStringValue)
        .collect(Collectors.toList());
  }

  private Stream<Build.Attribute> getAttribute(Build.Rule rule, String name) {
    return rule.getAttributeList().stream()
        .filter(attr -> TargetsUtils.isAttributeSpecifiedAndHasGivenName(attr, name));
  }
}
