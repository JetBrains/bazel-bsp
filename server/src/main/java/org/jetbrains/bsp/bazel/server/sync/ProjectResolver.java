package org.jetbrains.bsp.bazel.server.sync;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.protobuf.TextFormat;
import io.vavr.API;
import io.vavr.collection.HashSet;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Set;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Function;
import org.jetbrains.bsp.bazel.info.BspTargetInfo.TargetInfo;
import org.jetbrains.bsp.bazel.logger.BspClientLogger;
import org.jetbrains.bsp.bazel.projectview.model.ProjectView;
import org.jetbrains.bsp.bazel.server.bep.BepOutput;
import org.jetbrains.bsp.bazel.server.bsp.config.ProjectViewProvider;
import org.jetbrains.bsp.bazel.server.bsp.managers.BazelBspAspectsManager;
import org.jetbrains.bsp.bazel.server.sync.model.Project;

/** Responsible for querying bazel and constructing Project instance */
public class ProjectResolver {
  private static final String ASPECT_NAME = "bsp_target_info_aspect";
  private static final String BSP_INFO_OUTPUT_GROUP = "bsp-target-info-transitive-deps";
  private static final String ARTIFACTS_OUTPUT_GROUP = "bsp-ide-resolve-transitive-deps";

  private final BazelBspAspectsManager bazelBspAspectsManager;
  private final ProjectViewProvider projectViewProvider;
  private final BazelProjectMapper bazelProjectMapper;
  private final BspClientLogger logger;

  public ProjectResolver(
      BazelBspAspectsManager bazelBspAspectsManager,
      ProjectViewProvider projectViewProvider,
      BazelProjectMapper bazelProjectMapper,
      BspClientLogger logger) {
    this.bazelBspAspectsManager = bazelBspAspectsManager;
    this.projectViewProvider = projectViewProvider;
    this.bazelProjectMapper = bazelProjectMapper;
    this.logger = logger;
  }

  public Project resolve() {
    var projectView = logger.timed("Reading project view", projectViewProvider::currentProjectView);
    var bepOutput =
        logger.timed("Building project with aspect", () -> buildProjectWithAspect(projectView));
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
            () -> bazelProjectMapper.createProject(targets, rootTargets, projectView));
    return model;
  }

  private BepOutput buildProjectWithAspect(ProjectView projectView) {
    return bazelBspAspectsManager.fetchFilesFromOutputGroups(
        projectView.targetSpecs(),
        ASPECT_NAME,
        List.of(BSP_INFO_OUTPUT_GROUP, ARTIFACTS_OUTPUT_GROUP));
  }

  private Map<String, TargetInfo> readTargetMapFromAspectOutputs(Set<URI> files) {
    return files
        .map(API.unchecked(this::readTargetInfoFromFile))
        .toMap(TargetInfo::getId, Function.identity());
  }

  private TargetInfo readTargetInfoFromFile(URI uri) throws IOException {
    var builder = TargetInfo.newBuilder();
    var parser = TextFormat.Parser.newBuilder().setAllowUnknownFields(true).build();
    parser.merge(Files.readString(Paths.get(uri), UTF_8), builder);
    return builder.build();
  }
}
