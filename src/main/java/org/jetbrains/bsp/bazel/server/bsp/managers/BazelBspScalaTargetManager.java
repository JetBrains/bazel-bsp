package org.jetbrains.bsp.bazel.server.bsp.managers;

import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import ch.epfl.scala.bsp4j.ScalaBuildTarget;
import ch.epfl.scala.bsp4j.ScalaPlatform;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
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
  public static final List<BuildTargetIdentifier> SCALA_TARGETS =
      ImmutableList.of(
          new BuildTargetIdentifier(SCALA_LIBRARY),
          new BuildTargetIdentifier(SCALA_REFLECT),
          new BuildTargetIdentifier(SCALA_COMPILER));

  public BazelBspScalaTargetManager(BazelBspAspectsManager bazelBspAspectsManager) {
    this.bazelBspAspectsManager = bazelBspAspectsManager;
  }

  protected Optional<ScalaBuildTarget> getScalaBuildTarget() {
    List<String> classpath =
        bazelBspAspectsManager.fetchPathsFromOutputGroup(
            SCALA_TARGETS, SCALA_COMPILER_ASPECT, SCALA_COMPILER_OUTPUT_GROUP);

    return classpath.stream()
        .filter(uri -> uri.contains("scala-library"))
        .findFirst()
        .map(
            scalaLibrary -> {
              String scalaVersion =
                  scalaLibrary.substring(
                      scalaLibrary.indexOf("scala-library-") + 14, scalaLibrary.indexOf(".jar"));
              return new ScalaBuildTarget(
                  "org.scala-lang",
                  scalaVersion,
                  scalaVersion.substring(0, scalaVersion.lastIndexOf(".")),
                  ScalaPlatform.JVM,
                  classpath);
            });
  }

  @Override
  protected Supplier<Optional<ScalaBuildTarget>> calculateValue() {
    return this::getScalaBuildTarget;
  }
}
