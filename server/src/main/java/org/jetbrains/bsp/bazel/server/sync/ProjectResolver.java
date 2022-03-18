package org.jetbrains.bsp.bazel.server.sync;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.protobuf.TextFormat;
import io.vavr.API;
import io.vavr.collection.HashSet;
import io.vavr.collection.Map;
import io.vavr.collection.Set;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Function;
import org.jetbrains.bsp.bazel.info.BspTargetInfo.TargetInfo;
import org.jetbrains.bsp.bazel.projectview.model.ProjectView;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewListSection;
import org.jetbrains.bsp.bazel.server.bep.BepOutput;
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

  public ProjectResolver(
      BazelBspAspectsManager bazelBspAspectsManager,
      ProjectViewProvider projectViewProvider,
      BazelProjectMapper bazelProjectMapper) {
    this.bazelBspAspectsManager = bazelBspAspectsManager;
    this.projectViewProvider = projectViewProvider;
    this.bazelProjectMapper = bazelProjectMapper;
  }

  public Project resolve() {
    var projectView = projectViewProvider.current();
    var bepOutput = buildProjectWithAspect(projectView);
    var aspectOutputs = bepOutput.filesByOutputGroupNameTransitive(BSP_INFO_OUTPUT_GROUP);
    var rootTargets = bepOutput.rootTargets();
    var targets = readTargetMapFromAspectOutputs(aspectOutputs);
    return bazelProjectMapper.createProject(targets, HashSet.ofAll(rootTargets), projectView);
  }

  private BepOutput buildProjectWithAspect(ProjectView projectView) {
    var includedTargetRoots =
        projectView.getTargets().toList().flatMap(ProjectViewListSection::getIncludedValues);
    var excludedTargetRoots =
        projectView.getTargets().toList().flatMap(ProjectViewListSection::getExcludedValues);
    return bazelBspAspectsManager.fetchFilesFromOutputGroup(
        includedTargetRoots.asJava(),
        excludedTargetRoots.asJava(),
        ASPECT_NAME,
        BSP_INFO_OUTPUT_GROUP + "," + ARTIFACTS_OUTPUT_GROUP);
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
