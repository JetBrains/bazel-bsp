package org.jetbrains.bsp.bazel.server.bsp.managers;

import io.vavr.collection.List;
import org.jetbrains.bsp.bazel.bazelrunner.params.BazelFlag;
import org.jetbrains.bsp.bazel.projectview.model.TargetSpecs;
import org.jetbrains.bsp.bazel.server.bep.BepOutput;
import org.jetbrains.bsp.bazel.server.bsp.utils.InternalAspectsResolver;

public class BazelBspAspectsManager {

  private final BazelBspCompilationManager bazelBspCompilationManager;
  private final InternalAspectsResolver aspectsResolver;

  public BazelBspAspectsManager(
      BazelBspCompilationManager bazelBspCompilationManager,
      InternalAspectsResolver aspectResolver) {
    this.bazelBspCompilationManager = bazelBspCompilationManager;
    this.aspectsResolver = aspectResolver;
  }

  public BepOutput fetchFilesFromOutputGroups(
      TargetSpecs targetSpecs, String aspect, List<String> outputGroups) {
    var result =
        bazelBspCompilationManager.buildTargetsWithBep(
            targetSpecs,
            List.of(
                BazelFlag.aspect(aspectsResolver.resolveLabel(aspect)),
                BazelFlag.outputGroups(outputGroups),
                BazelFlag.keepGoing(),
                BazelFlag.color(true)));
    return result.bepOutput();
  }
}
