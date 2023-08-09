package org.jetbrains.bsp.bazel.server.bsp.managers;

import io.vavr.collection.Array;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
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
      CancelChecker cancelChecker,
      TargetsSpec targetSpecs,
      String aspect,
      List<String> outputGroups) {
    if (targetSpecs.getValues().isEmpty()) return new BepOutput(new HashMap<>(), new HashMap<>(), new HashSet<>());
    return
        bazelBspCompilationManager
          .buildTargetsWithBep(
            cancelChecker,
            targetSpecs,
            Array.of(
                BazelFlag.repositoryOverride(
                    Constants.ASPECT_REPOSITORY, aspectsResolver.getBazelBspRoot()),
                BazelFlag.aspect(aspectsResolver.resolveLabel(aspect)),
                BazelFlag.outputGroups(outputGroups),
                BazelFlag.keepGoing(),
                BazelFlag.color(true),
                BazelFlag.buildManualTests(),
                BazelFlag.curses(false)
            ),
            null
          )
          .bepOutput();
  }
}
