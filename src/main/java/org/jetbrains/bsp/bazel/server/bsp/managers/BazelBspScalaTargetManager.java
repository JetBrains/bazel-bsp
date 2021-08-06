package org.jetbrains.bsp.bazel.server.bsp.managers;

import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import ch.epfl.scala.bsp4j.ScalaBuildTarget;
import ch.epfl.scala.bsp4j.ScalaPlatform;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.bsp.bazel.commons.Lazy;

public class BazelBspScalaTargetManager extends Lazy<ScalaBuildTarget> {
  private static final Logger LOGGER = LogManager.getLogger(BazelBspScalaTargetManager.class);

  public static final String SCALA_LIBRARY =
      "@io_bazel_rules_scala_scala_library//:io_bazel_rules_scala_scala_library";
  public static final String SCALA_REFLECT =
      "@io_bazel_rules_scala_scala_reflect//:io_bazel_rules_scala_scala_reflect";
  public static final String SCALA_COMPILER =
      "@io_bazel_rules_scala_scala_compiler//:io_bazel_rules_scala_scala_compiler";
  public static final String SCALA_COMPILER_ASPECT =
      "@//.bazelbsp:aspects.bzl%scala_compiler_classpath_aspect";
  public static final String SCALA_COMPILER_OUTPUT_GROUP = "scala_compiler_classpath_files";
  private final BazelBspAspectsManager bazelBspAspectsManager;

  public BazelBspScalaTargetManager(BazelBspAspectsManager bazelBspAspectsManager) {
    this.bazelBspAspectsManager = bazelBspAspectsManager;
  }

  protected Optional<ScalaBuildTarget> getScalaBuildTarget() {
    List<BuildTargetIdentifier> targets =
        ImmutableList.of(
            new BuildTargetIdentifier(SCALA_LIBRARY),
            new BuildTargetIdentifier(SCALA_REFLECT),
            new BuildTargetIdentifier(SCALA_COMPILER));
    List<String> classpath =
        bazelBspAspectsManager.fetchPathsFromOutputGroup(
            targets, SCALA_COMPILER_ASPECT, SCALA_COMPILER_OUTPUT_GROUP);

    List<String> scalaVersions =
        classpath.stream()
            .filter(uri -> uri.contains("scala-library"))
            .collect(Collectors.toList());

    if (scalaVersions.size() != 1) {
      LOGGER.error("Scala versions size different than one: " + scalaVersions.size());
      return Optional.empty();
    }

    String scalaVersion =
        scalaVersions
            .get(0)
            .substring(
                scalaVersions.get(0).indexOf("scala-library-") + 14,
                scalaVersions.get(0).indexOf(".jar"));
    ScalaBuildTarget scalaBuildTarget =
        new ScalaBuildTarget(
            "org.scala-lang",
            scalaVersion,
            scalaVersion.substring(0, scalaVersion.lastIndexOf(".")),
            ScalaPlatform.JVM,
            classpath);

    return Optional.of(scalaBuildTarget);
  }

  @Override
  protected Supplier<Optional<ScalaBuildTarget>> calculateValue() {
    return this::getScalaBuildTarget;
  }
}
