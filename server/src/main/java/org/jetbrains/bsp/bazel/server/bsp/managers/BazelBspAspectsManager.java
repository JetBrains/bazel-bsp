package org.jetbrains.bsp.bazel.server.bsp.managers;

import io.vavr.collection.Array;
import java.util.List;
import org.jetbrains.bsp.bazel.bazelrunner.params.BazelFlag;
import org.jetbrains.bsp.bazel.commons.Constants;
import org.jetbrains.bsp.bazel.server.bep.BepOutput;
import org.jetbrains.bsp.bazel.server.bsp.utils.InternalAspectsResolver;
import org.jetbrains.bsp.bazel.workspacecontext.TargetsSpec;

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
      TargetsSpec targetSpecs, String aspect, List<String> outputGroups, String originId) {
    var result =
        bazelBspCompilationManager.buildTargetsWithBep(
            targetSpecs,
            Array.of(
                BazelFlag.repositoryOverride(
                    Constants.ASPECT_REPOSITORY, aspectsResolver.getBazelBspRoot()),
                BazelFlag.aspect(aspectsResolver.resolveLabel(aspect)),
                BazelFlag.outputGroups(outputGroups),
                BazelFlag.keepGoing(),
                BazelFlag.color(true),
                BazelFlag.buildManualTests()),
            originId);
    return result.bepOutput();
  }
}
