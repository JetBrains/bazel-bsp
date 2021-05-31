package org.jetbrains.bsp.bazel.server.bsp.config;

import com.google.common.collect.ImmutableList;
import java.nio.file.Paths;
import java.util.Arrays;
import org.jetbrains.bsp.bazel.projectview.model.ProjectView;
import org.jetbrains.bsp.bazel.projectview.model.ProjectViewProvider;
import org.jetbrains.bsp.bazel.projectview.model.sections.specific.DirectoriesSection;
import org.jetbrains.bsp.bazel.projectview.model.sections.specific.TargetsSection;

public class ServerArgsProjectViewProvider implements ProjectViewProvider {

  private final String targets;

  public ServerArgsProjectViewProvider(String targets) {
    this.targets = targets;
  }

  @Override
  public ProjectView create() {
    DirectoriesSection directoriesSection =
        new DirectoriesSection(ImmutableList.of(Paths.get(".")), ImmutableList.of());
    TargetsSection targetsSection =
        new TargetsSection(Arrays.asList(targets.split(",")), ImmutableList.of());

    return ProjectView.builder().directories(directoriesSection).targets(targetsSection).build();
  }
}
