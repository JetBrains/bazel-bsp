package org.jetbrains.bsp.bazel.server.bsp.config;

import com.google.common.collect.ImmutableList;
import org.jetbrains.bsp.bazel.projectview.model.ProjectView;
import org.jetbrains.bsp.bazel.projectview.model.ProjectViewDefaultProvider;
import org.jetbrains.bsp.bazel.projectview.model.ProjectViewProvider;
import org.jetbrains.bsp.bazel.projectview.model.sections.specific.DirectoriesSection;
import org.jetbrains.bsp.bazel.projectview.model.sections.specific.TargetsSection;

import java.nio.file.Paths;
import java.util.Arrays;

public class ServerArgsProjectViewProvider implements ProjectViewProvider {

  private final String[] args;

  private final ProjectViewProvider defaultProvider;

  public ServerArgsProjectViewProvider(String[] args) {
    this.args = args;
    this.defaultProvider = new ProjectViewDefaultProvider();
  }

  @Override
  public ProjectView create() {
    boolean areTargetsProvided = args.length == 2;
    if (areTargetsProvided) {
      return createFromArgs(args[1]);
    }

    return defaultProvider.create();
  }

  private ProjectView createFromArgs(String targets) {
    DirectoriesSection directoriesSection =
        new DirectoriesSection(ImmutableList.of(Paths.get(".")), ImmutableList.of());
    TargetsSection targetsSection =
        new TargetsSection(Arrays.asList(targets.split(",")), ImmutableList.of());

    return ProjectView.builder()
        .directories(directoriesSection)
        .targets(targetsSection)
        .build();
  }
}
