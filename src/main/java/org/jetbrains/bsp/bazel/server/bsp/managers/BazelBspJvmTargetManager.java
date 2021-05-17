package org.jetbrains.bsp.bazel.server.bsp.managers;

import ch.epfl.scala.bsp4j.JvmBuildTarget;
import com.google.common.collect.Lists;
import com.google.devtools.build.lib.query2.proto.proto2api.Build;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.jetbrains.bsp.bazel.commons.Lazy;
import org.jetbrains.bsp.bazel.commons.Uri;
import org.jetbrains.bsp.bazel.server.bazel.BazelProcess;
import org.jetbrains.bsp.bazel.server.bazel.BazelRunner;
import org.jetbrains.bsp.bazel.server.bazel.params.BazelRunnerFlag;
import org.jetbrains.bsp.bazel.server.bsp.resolvers.QueryResolver;

public class BazelBspJvmTargetManager extends Lazy<String> {
  public static final String FETCH_JAVA_VERSION_ASPECT =
      "@//.bazelbsp:aspects.bzl%fetch_java_target_version";
  public static final String BAZEL_JDK_CURRENT_JAVA_TOOLCHAIN =
      "@bazel_tools//tools/jdk:current_java_toolchain";
  private final BazelRunner bazelRunner;
  private final BazelBspAspectsManager bazelBspAspectsManager;

  public BazelBspJvmTargetManager(
      BazelRunner bazelRunner, BazelBspAspectsManager bazelBspAspectsManager) {
    this.bazelRunner = bazelRunner;
    this.bazelBspAspectsManager = bazelBspAspectsManager;
  }

  public JvmBuildTarget getJVMBuildTarget(Build.Rule rule) {
    Optional<String> javaHomePath = getJavaPath(rule);
    Optional<String> javaVersion = getJavaVersion();
    return new JvmBuildTarget(javaHomePath.orElse(null), javaVersion.orElse(null));
  }

  private Optional<String> getJavaPath(Build.Rule rule) {
    List<String> traversingPath = Lists.newArrayList("$jvm", "$java_runtime", ":alias", "actual");

    return traverseDependency(rule, traversingPath)
        .map(Build.Rule::getLocation)
        .map(location -> location.substring(0, location.indexOf("/BUILD")))
        .map(path -> Uri.fromAbsolutePath(path).toString());
  }

  private Optional<Build.Rule> traverseDependency(
      Build.Rule startingRule, List<String> attributesToTraverse) {
    Build.Rule currentRule = startingRule;

    for (String attributeToTraverse : attributesToTraverse) {
      Optional<Build.Rule> rule =
          currentRule.getAttributeList().stream()
              .filter(
                  attribute ->
                      attribute.getName().equals(attributeToTraverse) && attribute.hasStringValue())
              .findFirst()
              .flatMap(this::getTargetFromAttribute)
              .map(Build.Target::getRule);

      if (!rule.isPresent()) {
        return Optional.empty();
      }

      currentRule = rule.get();
    }

    return Optional.of(currentRule);
  }

  private Optional<Build.Target> getTargetFromAttribute(Build.Attribute attribute) {
    BazelProcess processResult =
        bazelRunner
            .commandBuilder()
            .query()
            .withFlag(BazelRunnerFlag.OUTPUT_PROTO)
            .withArgument(attribute.getStringValue())
            .executeBazelBesCommand();

    return QueryResolver.getQueryResultForProcess(processResult).getTargetList().stream()
        .findFirst();
  }

  private Optional<String> getJavaVersion() {
    return bazelBspAspectsManager
        .fetchLinesFromAspect(BAZEL_JDK_CURRENT_JAVA_TOOLCHAIN, FETCH_JAVA_VERSION_ASPECT)
        .filter(
            parts ->
                parts.size() == 3
                    && parts.get(0).equals(BazelBspAspectsManager.DEBUG_MESSAGE)
                    && parts.get(1).contains(BazelBspAspectsManager.ASPECT_LOCATION)
                    && parts.get(2).chars().allMatch(Character::isDigit))
        .map(parts -> parts.get(2))
        .findFirst();
  }

  @Override
  protected Supplier<Optional<String>> calculateValue() {
    return this::getJavaVersion;
  }
}
