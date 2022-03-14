package org.jetbrains.bsp.bazel.server.bsp.managers;

import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.stream.Stream;
import org.jetbrains.bsp.bazel.bazelrunner.BazelRunner;
import org.jetbrains.bsp.bazel.bazelrunner.params.BazelRunnerFlag;
import org.jetbrains.bsp.bazel.server.bep.BepOutput;
import org.jetbrains.bsp.bazel.server.bsp.utils.InternalAspectsResolver;

public class BazelBspAspectsManager {

  public static final String DEBUG_MESSAGE = "DEBUG:";
  private final BazelBspCompilationManager bazelBspCompilationManager;
  private final BazelRunner bazelRunner;
  private final InternalAspectsResolver aspectsResolver;

  public BazelBspAspectsManager(
      BazelBspCompilationManager bazelBspCompilationManager,
      BazelRunner bazelRunner,
      InternalAspectsResolver aspectResolver) {
    this.bazelBspCompilationManager = bazelBspCompilationManager;
    this.bazelRunner = bazelRunner;
    this.aspectsResolver = aspectResolver;
  }

  public BepOutput fetchFilesFromOutputGroup(
      List<BuildTargetIdentifier> targets, String aspect, String outputGroup) {
    String aspectFlag = String.format("--aspects=%s", aspectsResolver.resolveLabel(aspect));
    String outputGroupFlag = String.format("--output_groups=%s", outputGroup);
    var result =
        bazelBspCompilationManager.buildTargetsWithBep(
            targets, ImmutableList.of(aspectFlag, outputGroupFlag, "--keep_going"));
    return result.bepOutput();
  }

  public Stream<String> fetchLinesFromAspect(String target, String aspect) {
    return fetchLinesFromAspect(target, aspect, false);
  }

  public Stream<String> fetchLinesFromAspect(String target, String aspect, boolean build) {
    var builder =
        bazelRunner
            .commandBuilder()
            .build()
            .withFlag(BazelRunnerFlag.ASPECTS, aspectsResolver.resolveLabel(aspect))
            .withArgument(target);

    if (!build) {
      builder.withFlag(BazelRunnerFlag.NOBUILD);
    }

    List<String> lines = builder.executeBazelCommand().getStderr();

    return lines.stream()
        .map(line -> Splitter.on(" ").splitToList(line))
        .filter(
            parts ->
                parts.size() == 3
                    && parts.get(0).equals(DEBUG_MESSAGE)
                    && parts.get(1).contains(aspectsResolver.getAspectOutputIndicator()))
        .map(parts -> parts.get(2));
  }
}
