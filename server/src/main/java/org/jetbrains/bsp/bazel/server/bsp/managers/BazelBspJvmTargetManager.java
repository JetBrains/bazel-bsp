package org.jetbrains.bsp.bazel.server.bsp.managers;

import ch.epfl.scala.bsp4j.JvmBuildTarget;
import com.google.common.collect.Lists;
import com.google.devtools.build.lib.query2.proto.proto2api.Build;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.jetbrains.bsp.bazel.bazelrunner.BazelProcess;
import org.jetbrains.bsp.bazel.bazelrunner.BazelRunner;
import org.jetbrains.bsp.bazel.bazelrunner.data.BazelData;
import org.jetbrains.bsp.bazel.bazelrunner.params.BazelRunnerFlag;
import org.jetbrains.bsp.bazel.commons.Lazy;
import org.jetbrains.bsp.bazel.commons.Uri;
import org.jetbrains.bsp.bazel.server.bsp.resolvers.QueryResolver;

public class BazelBspJvmTargetManager extends Lazy<String> {
  public static final String FETCH_JAVA_VERSION_ASPECT = "fetch_java_target_version";
  public static final String FETCH_JAVA_HOME_ASPECT = "fetch_java_target_home";
  public static final String BAZEL_JDK_CURRENT_JAVA_TOOLCHAIN =
      "@bazel_tools//tools/jdk:current_java_toolchain";
  public static final String BAZEL_JDK_CURRENT_JAVA_RUNTIME =
      "@bazel_tools//tools/jdk:current_java_runtime";
  private final BazelRunner bazelRunner;
  private final BazelData bazelData;
  private final BazelBspAspectsManager bazelBspAspectsManager;

  public BazelBspJvmTargetManager(
      BazelRunner bazelRunner, BazelData bazelData, BazelBspAspectsManager bazelBspAspectsManager) {
    this.bazelRunner = bazelRunner;
    this.bazelData = bazelData;
    this.bazelBspAspectsManager = bazelBspAspectsManager;
  }

  public JvmBuildTarget getJVMBuildTarget(Build.Rule rule) {
    Optional<String> javaVersion = getJavaVersion();
    Optional<String> javaHomePath = getJavaHomePath(rule);
    return new JvmBuildTarget(javaHomePath.orElse(null), javaVersion.orElse(null));
  }

  private Optional<String> getJavaHomePath(Build.Rule rule) {
    if (bazelData.getVersion().getMajorVersion() < 5) {
      return getJavaPathForBazelLessThan5(rule);
    }

    return getJavaPathForBazel5();
  }

  private Optional<String> getJavaPathForBazelLessThan5(Build.Rule rule) {
    List<String> traversingPath = Lists.newArrayList("$jvm", "$java_runtime", ":alias", "actual");

    return traverseDependency(rule, traversingPath)
        .map(Build.Rule::getLocation)
        .map(location -> location.substring(0, location.indexOf("/BUILD")))
        .map(path -> Uri.fromAbsolutePath(path).toString());
  }

  private Optional<String> getJavaPathForBazel5() {
    return bazelBspAspectsManager
        .fetchLinesFromAspect(BAZEL_JDK_CURRENT_JAVA_RUNTIME, FETCH_JAVA_HOME_ASPECT)
        .findFirst()
        .map(path -> Uri.fromExecPath("exec-root://" + path, bazelData.getExecRoot()).toString());
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

      if (rule.isEmpty()) {
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
        .filter(line -> line.chars().allMatch(Character::isDigit))
        .findFirst();
  }

  @Override
  protected Supplier<Optional<String>> calculateValue() {
    return this::getJavaVersion;
  }
}
