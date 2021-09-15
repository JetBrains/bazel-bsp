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
import java.util.List;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseError;
import org.jetbrains.bsp.bazel.bazelrunner.BazelRunner;
import org.jetbrains.bsp.bazel.bazelrunner.data.BazelData;
import org.jetbrains.bsp.bazel.commons.Constants;
import org.jetbrains.bsp.bazel.server.bsp.managers.BazelBspCompilationManager;
import org.jetbrains.bsp.bazel.server.bsp.managers.BazelBspQueryManager;
import org.jetbrains.bsp.bazel.server.bsp.resolvers.TargetRulesResolver;
import org.jetbrains.bsp.bazel.server.bsp.resolvers.TargetsLanguageOptionsResolver;
import org.jetbrains.bsp.bazel.server.bsp.utils.BuildRuleAttributeExtractor;

public class ScalaBuildServerService {

  private static final Logger LOGGER = LogManager.getLogger(JavaBuildServerService.class);

  private static final String SCALA_COMPILER_OPTIONS_ATTR_NAME = "scalacopts";
  private static final List<String> SCALA_LANGUAGES_IDS =
      ImmutableList.of(Constants.SCALAC, Constants.JAVAC);

  private static final List<String> SCALA_TEST_RULE_CLASS_NAMES =
      ImmutableList.of("scala_test", "scala_junit_test");

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
    return SCALA_TEST_RULE_CLASS_NAMES.contains(rule.getRuleClass());
  }

  private ScalaTestClassesItem mapRuleToTestClassesItem(Build.Rule rule) {
    BuildTargetIdentifier target = new BuildTargetIdentifier(rule.getName());
    List<String> classes =
        BuildRuleAttributeExtractor.extract(rule, Constants.SCALA_TEST_MAIN_CLASSES_ATTRIBUTE_NAME);

    return new ScalaTestClassesItem(target, classes);
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
        BuildRuleAttributeExtractor.extract(rule, Constants.JVM_FLAGS_ATTR_NAME);
    List<String> mainClassesNames =
        BuildRuleAttributeExtractor.extract(rule, Constants.MAIN_CLASS_ATTR_NAME);
    List<String> arguments = BuildRuleAttributeExtractor.extract(rule, Constants.ARGS_ATTR_NAME);
    return mainClassesNames.stream()
        .map(mainClassName -> new ScalaMainClass(mainClassName, arguments, targetOptions))
        .collect(Collectors.toList());
  }
}
