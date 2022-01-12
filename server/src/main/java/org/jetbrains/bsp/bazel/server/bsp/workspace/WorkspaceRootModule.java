package org.jetbrains.bsp.bazel.server.bsp.workspace;

import ch.epfl.scala.bsp4j.BuildTarget;
import ch.epfl.scala.bsp4j.BuildTargetCapabilities;
import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import ch.epfl.scala.bsp4j.ResourcesItem;
import ch.epfl.scala.bsp4j.ResourcesResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import org.jetbrains.bsp.bazel.bazelrunner.data.BazelData;

public class WorkspaceRootModule {
  private static final String MODULE_NAME = "bsp-workspace-root";

  public static final Predicate<String> IGNORE_WORKSPACE_ROOT = (s) -> !s.equals(MODULE_NAME);

  public static void addToBuildTargetResources(
      List<BuildTargetIdentifier> targets, BazelData bazelData, ResourcesResult result) {

    var rootModuleTarget =
        targets.stream().filter(id -> Objects.equals(id.getUri(), MODULE_NAME)).findAny();

    rootModuleTarget.ifPresent(
        id -> {
          var uri = Paths.get(bazelData.getWorkspaceRoot()).toUri().toString();
          var resourcesItem = new ResourcesItem(id, List.of(uri));
          result.getItems().add(resourcesItem);
        });
  }

  public static void addToBuildTargets(
      BazelData bazelData, Path bspProjectRoot, List<BuildTarget> targets) {

    var workspaceRoot = Paths.get(bazelData.getWorkspaceRoot()).toAbsolutePath();

    if (workspaceRoot.equals(bspProjectRoot)) {
      return;
    }

    BuildTarget buildTarget =
        new BuildTarget(
            new BuildTargetIdentifier(MODULE_NAME),
            List.of(),
            List.of(),
            List.of(),
            new BuildTargetCapabilities());

    buildTarget.setBaseDirectory(workspaceRoot.toUri().toString());

    var workspaceName = workspaceRoot.getFileName().toString();
    buildTarget.setDisplayName(workspaceName);

    targets.add(buildTarget);
  }
}
