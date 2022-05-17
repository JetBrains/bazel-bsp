package org.jetbrains.bsp.bazel.server.sync;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.protobuf.TextFormat;
import io.vavr.API;
import io.vavr.collection.Array;
import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import io.vavr.collection.Map;
import io.vavr.collection.Set;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Function;
import org.jetbrains.bsp.bazel.info.BspTargetInfo.TargetInfo;
import org.jetbrains.bsp.bazel.logger.BspClientLogger;
import org.jetbrains.bsp.bazel.server.bep.BepOutput;
import org.jetbrains.bsp.bazel.server.bsp.managers.BazelBspAspectsManager;
import org.jetbrains.bsp.bazel.server.sync.model.Project;
import org.jetbrains.bsp.bazel.workspacecontext.WorkspaceContext;
import org.jetbrains.bsp.bazel.workspacecontext.WorkspaceContextProvider;

/** Responsible for querying bazel and constructing Project instance */
public class ProjectResolver {
  private static final String ASPECT_NAME = "bsp_target_info_aspect";
  private static final String BSP_INFO_OUTPUT_GROUP = "bsp-target-info-transitive-deps";
  private static final String ARTIFACTS_OUTPUT_GROUP = "bsp-ide-resolve-transitive-deps";

  private final BazelBspAspectsManager bazelBspAspectsManager;
  private final WorkspaceContextProvider workspaceContextProvider;
  private final BazelProjectMapper bazelProjectMapper;
  private final BspClientLogger logger;

  public ProjectResolver(
      BazelBspAspectsManager bazelBspAspectsManager,
      WorkspaceContextProvider workspaceContextProvider,
      BazelProjectMapper bazelProjectMapper,
      BspClientLogger logger) {
    this.bazelBspAspectsManager = bazelBspAspectsManager;
    this.workspaceContextProvider = workspaceContextProvider;
    this.bazelProjectMapper = bazelProjectMapper;
    this.logger = logger;
  }

  public Project resolve() {
    var workspaceContext =
        logger.timed(
            "Reading project view adn creating workspace context",
            workspaceContextProvider::currentWorkspaceContext);
    var bepOutput =
        logger.timed(
            "Building project with aspect", () -> buildProjectWithAspect(workspaceContext));
    var aspectOutputs =
        logger.timed(
            "Reading aspect output paths",
            () -> bepOutput.filesByOutputGroupNameTransitive(BSP_INFO_OUTPUT_GROUP));
    var rootTargets = HashSet.ofAll(bepOutput.rootTargets());
    var targets =
        logger.timed("Parsing aspect outputs", () -> readTargetMapFromAspectOutputs(aspectOutputs));
    var model =
        logger.timed(
            "Mapping to internal model",
            () -> bazelProjectMapper.createProject(targets, rootTargets, workspaceContext));
    return model;
  }

  private BepOutput buildProjectWithAspect(WorkspaceContext workspaceContext) {
    return bazelBspAspectsManager.fetchFilesFromOutputGroups(
        workspaceContext.getTargets(),
        ASPECT_NAME,
        Array.of(BSP_INFO_OUTPUT_GROUP, ARTIFACTS_OUTPUT_GROUP));
  }

  private Map<String, TargetInfo> readTargetMapFromAspectOutputs(Set<URI> files) {
    return files
        .toJavaParallelStream()
        .map(API.unchecked(this::readTargetInfoFromFile))
        .collect(HashMap.collector(TargetInfo::getId, Function.identity()));
  }

  private TargetInfo readTargetInfoFromFile(URI uri) throws IOException {
    var builder = TargetInfo.newBuilder();
    var parser = TextFormat.Parser.newBuilder().setAllowUnknownFields(true).build();
    parser.merge(Files.newBufferedReader(Paths.get(uri), UTF_8), builder);
    return builder.build();
  }
}
