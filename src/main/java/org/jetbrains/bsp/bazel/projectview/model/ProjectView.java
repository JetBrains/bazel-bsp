package org.jetbrains.bsp.bazel.projectview.model;

import org.jetbrains.bsp.bazel.projectview.model.sections.specific.DirectoriesSection;
import org.jetbrains.bsp.bazel.projectview.model.sections.specific.TargetsSection;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Representation of the project view file
 *
 * @link https://ij.bazel.build/docs/project-views.html
 */
public class ProjectView {

  private final DirectoriesSection directories;
  private final TargetsSection targets;

  private ProjectView(DirectoriesSection directories, TargetsSection targets) {
    this.directories = directories;
    this.targets = targets;
  }

  public static ProjectView.Builder builder() {
    return new Builder();
  }

  public ProjectView merge(ProjectView projectView) {
    return ProjectView.builder()
        .directories(directories.merge(projectView.directories))
        .targets(targets.merge(projectView.targets))
        .build();
  }

  public DirectoriesSection getDirectories() {
    return directories;
  }

  public TargetsSection getTargets() {
    return targets;
  }

  public static class Builder {

    private DirectoriesSection directories;
    private TargetsSection targets;

    public Builder directories(DirectoriesSection directories) {
      this.directories = directories;
      return this;
    }

    public Builder targets(TargetsSection target) {
      this.targets = target;
      return this;
    }

    public ProjectView build() {
      assertRequiredFields();

      return new ProjectView(directories, targets);
    }

    private void assertRequiredFields() {
      if (directories == null) {
        throw new IllegalStateException("directories section is required!");
      }

      if (targets == null) {
        throw new IllegalStateException("targets section is required!");
      }
    }
  }
}
