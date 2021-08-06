package org.jetbrains.bsp.bazel.server.bsp.managers;

import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.bsp.bazel.commons.Constants;
import org.jetbrains.bsp.bazel.commons.Uri;
import org.jetbrains.bsp.bazel.server.bazel.BazelRunner;
import org.jetbrains.bsp.bazel.server.bazel.params.BazelRunnerFlag;
import org.jetbrains.bsp.bazel.server.bep.BepServer;

public class BazelBspAspectsManager {

  public static final String DEBUG_MESSAGE = "DEBUG:";
  public static final String ASPECT_LOCATION = ".bazelbsp/aspects.bzl";
  private final BazelBspCompilationManager bazelBspCompilationManager;
  private final BazelRunner bazelRunner;
  private BepServer bepServer;

  public BazelBspAspectsManager(
      BazelBspCompilationManager bazelBspCompilationManager, BazelRunner bazelRunner) {
    this.bazelBspCompilationManager = bazelBspCompilationManager;
    this.bazelRunner = bazelRunner;
  }

  public List<String> fetchPathsFromOutputGroup(
      List<BuildTargetIdentifier> targets, String aspect, String outputGroup) {
    String aspectFlag = String.format("--aspects=%s", aspect);
    String outputGroupFlag = String.format("--output_groups=%s", outputGroup);
    bazelBspCompilationManager.buildTargetsWithBep(
        targets, ImmutableList.of(aspectFlag, outputGroupFlag));
    return bepServer
        .getOutputGroupPaths()
        .getOrDefault(Constants.SCALA_COMPILER_CLASSPATH_FILES, Collections.emptySet())
        .stream()
        .map(Uri::toString)
        .collect(Collectors.toList());
  }

  public Stream<List<String>> fetchLinesFromAspect(String target, String aspect) {
    List<String> lines =
        bazelRunner
            .commandBuilder()
            .build()
            .withFlag(BazelRunnerFlag.NOBUILD)
            .withFlag(BazelRunnerFlag.ASPECTS, aspect)
            .withArgument(target)
            .executeBazelCommand()
            .getStderr();

    return lines.stream().map(line -> Splitter.on(" ").splitToList(line));
  }

  public void setBepServer(BepServer bepServer) {
    this.bepServer = bepServer;
  }
}
