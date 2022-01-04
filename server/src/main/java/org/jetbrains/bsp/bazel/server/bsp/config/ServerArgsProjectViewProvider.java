package org.jetbrains.bsp.bazel.server.bsp.config;

import com.google.common.collect.ImmutableList;
import org.jetbrains.bsp.bazel.projectview.model.ProjectView;
import org.jetbrains.bsp.bazel.projectview.model.ProjectViewProvider;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewTargetsSection;

import java.util.Arrays;

public class ServerArgsProjectViewProvider implements ProjectViewProvider {

  private final String targets;

  public ServerArgsProjectViewProvider(String targets) {
    this.targets = targets;
  }

  @Override
  public ProjectView create() {
    ProjectViewTargetsSection targetsSection =
        new ProjectViewTargetsSection(Arrays.asList(targets.split(",")), ImmutableList.of());

    return ProjectView.builder()
        .targets(targetsSection)
        .build();
  }
}
