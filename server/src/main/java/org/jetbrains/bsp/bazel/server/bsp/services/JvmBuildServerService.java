package org.jetbrains.bsp.bazel.server.bsp.services;

import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import ch.epfl.scala.bsp4j.JvmEnvironmentItem;
import ch.epfl.scala.bsp4j.JvmRunEnvironmentParams;
import ch.epfl.scala.bsp4j.JvmRunEnvironmentResult;
import ch.epfl.scala.bsp4j.JvmTestEnvironmentParams;
import ch.epfl.scala.bsp4j.JvmTestEnvironmentResult;
import com.google.common.collect.ImmutableList;
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
import org.jetbrains.bsp.bazel.server.bsp.resolvers.TargetsLanguageOptionsResolver;

public class JvmBuildServerService {

  private static final Logger LOGGER = LogManager.getLogger(JvmBuildServerService.class);
  private static final ImmutableList<String> JVM_LANGUAGES_IDS =
      ImmutableList.of(Constants.JAVAC, Constants.KOTLINC, Constants.SCALAC);

  private final TargetsLanguageOptionsResolver<JvmEnvironmentItem> targetsLanguageOptionsResolver;

  public JvmBuildServerService(BazelData bazelData, BazelRunner bazelRunner) {
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
                        target, classpath, options, workspaceRoot, environmentVariables))
            .build();
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
    return targetsLanguageOptionsResolver.getResultItemsForTargets(targets).stream()
        .distinct()
        .collect(Collectors.toList());
  }
}
