package org.jetbrains.bsp.bazel.server.bsp.managers;

import ch.epfl.scala.bsp4j.ScalaBuildTarget;
import ch.epfl.scala.bsp4j.ScalaPlatform;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.bsp.bazel.bazelrunner.data.BazelData;
import org.jetbrains.bsp.bazel.bazelrunner.data.BazelProcessResult;
import org.jetbrains.bsp.bazel.commons.Uri;

public class BazelBspScalaTargetManager {
  private static final Logger LOGGER = LogManager.getLogger(BazelBspScalaTargetManager.class);

  private final BazelBspAspectsManager bazelBspAspectsManager;
  private final BazelData bazelData;

  public BazelBspScalaTargetManager(
      BazelBspAspectsManager bazelBspAspectsManager, BazelData bazelData) {
    this.bazelBspAspectsManager = bazelBspAspectsManager;
    this.bazelData = bazelData;
  }

  public Optional<ScalaBuildTarget> getScalaBuildTarget(String target) {

    BazelProcessResult aspectResult =
        bazelBspAspectsManager.fetchResultFromAspect(target, "print_runfiles");
    List<String> runfilePaths =
        aspectResult.getStderr().stream()
            .filter(line -> line.contains("[file_path]"))
            .collect(Collectors.toList());

    List<String> scalaVersions =
        runfilePaths.stream()
            .filter(line -> line.contains("scala-library"))
            .collect(Collectors.toList());

    if (scalaVersions.size() != 1) {
      LOGGER.error("Scala versions size different than one: " + scalaVersions.size());
      return Optional.empty();
    }

    List<String> scalaRunfiles =
        runfilePaths.stream()
            .map(line -> line.replaceFirst("^.*\\[file_path\\]", ""))
            .filter(x -> x.matches(".*scala-(?:library|compiler|reflect).*"))
            .map(path -> Uri.fromWorkspacePath(path, bazelData.getWorkspaceRoot()))
            .map(Uri::toString)
            .collect(Collectors.toList());

    Optional<String> scalaCompilerHack =
        scalaRunfiles.stream()
            .filter(x -> x.contains("scala-reflect"))
            .map(
                x ->
                    x.replace("scala-reflect", "scala-compiler")
                        .replace("scala_reflect", "scala_compiler"))
            .findFirst();

    ImmutableList.Builder<String> classpath = ImmutableList.<String>builder().addAll(scalaRunfiles);
    scalaCompilerHack.ifPresent(classpath::add);

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
            classpath.build());

    return Optional.of(scalaBuildTarget);
  }
}
