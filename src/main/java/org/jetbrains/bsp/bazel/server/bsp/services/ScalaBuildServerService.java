package org.jetbrains.bsp.bazel.server.bsp.services;

import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import ch.epfl.scala.bsp4j.ScalaMainClass;
import ch.epfl.scala.bsp4j.ScalaMainClassesItem;
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
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseError;
import org.jetbrains.bsp.bazel.commons.Constants;
import org.jetbrains.bsp.bazel.server.bazel.BazelRunner;
import org.jetbrains.bsp.bazel.server.bazel.data.BazelData;
import org.jetbrains.bsp.bazel.server.bsp.managers.BazelBspCompilationManager;
import org.jetbrains.bsp.bazel.server.bsp.managers.BazelBspQueryManager;
import org.jetbrains.bsp.bazel.server.bsp.resolvers.TargetRulesResolver;
import org.jetbrains.bsp.bazel.server.bsp.resolvers.TargetsLanguageOptionsResolver;
import org.jetbrains.bsp.bazel.server.bsp.resolvers.TargetsUtils;

public class ScalaBuildServerService {

  private static final Logger LOGGER = LogManager.getLogger(JavaBuildServerService.class);

  private static final String SCALA_COMPILER_OPTIONS_ATTR_NAME = "scalacopts";
  private static final List<String> SCALA_LANGUAGES_IDS =
      ImmutableList.of(Constants.SCALAC, Constants.JAVAC);

  private static final String SCALA_TEST_RULE_CLASS_NAME = "scala_test";

  private final TargetsLanguageOptionsResolver<ScalacOptionsItem> targetsLanguageOptionsResolver;
  private final TargetRulesResolver<ScalaMainClassesItem> targetsScalaMainClassesRulesResolver;
  private final TargetRulesResolver<ScalaTestClassesItem> targetsScalaTestClassesRulesResolver;
  private final BazelBspCompilationManager bazelBspCompilationManager;
  private final BazelBspQueryManager bazelBspQueryManager;

  public ScalaBuildServerService(
      BazelData bazelData,
      BazelRunner bazelRunner,
      BazelBspCompilationManager bazelBspCompilationManager,
      BazelBspQueryManager bazelBspQueryManager) {
    this.bazelBspCompilationManager = bazelBspCompilationManager;
    this.bazelBspQueryManager = bazelBspQueryManager;
    this.targetsLanguageOptionsResolver =
        TargetsLanguageOptionsResolver.<ScalacOptionsItem>builder()
            .bazelData(bazelData)
            .bazelRunner(bazelRunner)
            .compilerOptionsName(SCALA_COMPILER_OPTIONS_ATTR_NAME)
            .languagesIds(SCALA_LANGUAGES_IDS)
            .resultItemsCollector(ScalacOptionsItem::new)
            .build();

    this.targetsScalaMainClassesRulesResolver =
        TargetRulesResolver.withBazelRunnerAndMapper(bazelRunner, this::mapRuleToMainClassesItem);

    this.targetsScalaTestClassesRulesResolver =
        TargetRulesResolver.withBazelRunnerAndFilterAndMapper(
            bazelRunner, this::isScalaTestRule, this::mapRuleToTestClassesItem);
  }

  private boolean isScalaTestRule(Build.Rule rule) {
    return rule.getRuleClass().equals(SCALA_TEST_RULE_CLASS_NAME);
  }

  private ScalaTestClassesItem mapRuleToTestClassesItem(Build.Rule rule) {
    BuildTargetIdentifier target = new BuildTargetIdentifier(rule.getName());
    List<String> classes = getTestMainClasses(rule);

    return new ScalaTestClassesItem(target, classes);
  }

  private List<String> getTestMainClasses(Build.Rule rule) {
    return getAttribute(rule, Constants.SCALA_TEST_MAIN_CLASSES_ATTRIBUTE_NAME)
        .map(Attribute::getStringValue)
        .collect(Collectors.toList());
  }

  public Either<ResponseError, ScalacOptionsResult> buildTargetScalacOptions(
      ScalacOptionsParams scalacOptionsParams) {
    LOGGER.info("buildTargetScalacOptions call with param: {}", scalacOptionsParams);

    // Make sure dependencies are cached
    List<BuildTargetIdentifier> dependenciesTargets =
        bazelBspQueryManager.getTargetIdentifiersForDependencies(scalacOptionsParams.getTargets());
    bazelBspCompilationManager.buildTargetsWithBep(dependenciesTargets, ImmutableList.of());

    List<ScalacOptionsItem> resultItems =
        targetsLanguageOptionsResolver.getResultItemsForTargets(scalacOptionsParams.getTargets());

    ScalacOptionsResult scalacOptionsResult = new ScalacOptionsResult(resultItems);
    return Either.forRight(scalacOptionsResult);
  }

  public Either<ResponseError, ScalaTestClassesResult> buildTargetScalaTestClasses(
      ScalaTestClassesParams scalaTestClassesParams) {
    LOGGER.info("buildTargetScalaTestClasses call with param: {}", scalaTestClassesParams);

    List<ScalaTestClassesItem> resultItems =
        targetsScalaTestClassesRulesResolver.getItemsForTargets(
            scalaTestClassesParams.getTargets());

    ScalaTestClassesResult scalaTestClassesResult = new ScalaTestClassesResult(resultItems);

    return Either.forRight(scalaTestClassesResult);
  }

  public Either<ResponseError, ScalaMainClassesResult> buildTargetScalaMainClasses(
      ScalaMainClassesParams scalaMainClassesParams) {
    LOGGER.info("buildTargetScalaMainClasses call with param: {}", scalaMainClassesParams);

    List<ScalaMainClassesItem> resultItems =
        targetsScalaMainClassesRulesResolver.getItemsForTargets(
            scalaMainClassesParams.getTargets());

    ScalaMainClassesResult result =
        new ScalaMainClassesResult(
            resultItems.stream()
                .filter(item -> !item.getClasses().isEmpty())
                .collect(Collectors.toList()));

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
