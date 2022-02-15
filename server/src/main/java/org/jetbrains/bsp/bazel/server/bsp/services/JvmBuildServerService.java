package org.jetbrains.bsp.bazel.server.bsp.services;

import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import ch.epfl.scala.bsp4j.JvmEnvironmentItem;
import ch.epfl.scala.bsp4j.JvmRunEnvironmentParams;
import ch.epfl.scala.bsp4j.JvmRunEnvironmentResult;
import ch.epfl.scala.bsp4j.JvmTestEnvironmentParams;
import ch.epfl.scala.bsp4j.JvmTestEnvironmentResult;
import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseError;
import org.jetbrains.bsp.bazel.bazelrunner.BazelRunner;
import org.jetbrains.bsp.bazel.bazelrunner.data.BazelData;
import org.jetbrains.bsp.bazel.commons.Constants;
import org.jetbrains.bsp.bazel.server.bsp.managers.BazelBspAspectsManager;
import org.jetbrains.bsp.bazel.server.bsp.resolvers.TargetsLanguageOptionsResolver;

public class JvmBuildServerService {

  private static final Logger LOGGER = LogManager.getLogger(JvmBuildServerService.class);
  private static final ImmutableList<String> JVM_LANGUAGES_IDS =
      ImmutableList.of(Constants.JAVAC, Constants.KOTLINC, Constants.SCALAC);
  public static final String JAVA_RUNTIME_CLASSPATH_ASPECT = "java_runtime_classpath_aspect";

  private final TargetsLanguageOptionsResolver<JvmEnvironmentItem> targetsLanguageOptionsResolver;
  private final BazelBspAspectsManager bazelBspAspectsManager;

  public JvmBuildServerService(
      BazelData bazelData, BazelRunner bazelRunner, BazelBspAspectsManager bazelBspAspectsManager) {
    Map<String, String> environmentVariables = System.getenv();
    String workspaceRoot = bazelData.getWorkspaceRoot();
    this.targetsLanguageOptionsResolver =
        TargetsLanguageOptionsResolver.<JvmEnvironmentItem>builder()
            .bazelData(bazelData)
            .bazelRunner(bazelRunner)
            .compilerOptionsName(Constants.JVM_FLAGS_ATTR_NAME)
            .languagesIds(JVM_LANGUAGES_IDS)
            .resultItemsCollector(
                (target, options, classpath, classDirectory) ->
                    new JvmEnvironmentItem(
                        target,
                        Collections.emptyList(),
                        options,
                        workspaceRoot,
                        environmentVariables))
            .build();
    this.bazelBspAspectsManager = bazelBspAspectsManager;
  }

  public Either<ResponseError, JvmRunEnvironmentResult> jvmRunEnvironment(
      JvmRunEnvironmentParams params) {
    LOGGER.info("jvmRunEnvironment call with param: {}", params);
    List<JvmEnvironmentItem> items = getJvmEnvironmentItems(params.getTargets());
    return Either.forRight(new JvmRunEnvironmentResult(items));
  }

  public Either<ResponseError, JvmTestEnvironmentResult> jvmTestEnvironment(
      JvmTestEnvironmentParams params) {
    LOGGER.info("jvmTestEnvironment call with param: {}", params);
    List<JvmEnvironmentItem> items = getJvmEnvironmentItems(params.getTargets());
    return Either.forRight(new JvmTestEnvironmentResult(items));
  }

  private List<JvmEnvironmentItem> getJvmEnvironmentItems(List<BuildTargetIdentifier> targets) {
    var items =
        targetsLanguageOptionsResolver.getResultItemsForTargets(targets).stream()
            .distinct()
            .collect(Collectors.toList());
    items.forEach(item -> item.setClasspath(fetchClasspath(item.getTarget())));
    return items;
  }

  private List<String> fetchClasspath(BuildTargetIdentifier target) {
    return bazelBspAspectsManager.fetchPathsFromOutputGroup(
        List.of(target),
        JAVA_RUNTIME_CLASSPATH_ASPECT,
        Constants.JAVA_RUNTIME_CLASSPATH_ASPECT_OUTPUT_GROUP);
  }
}
