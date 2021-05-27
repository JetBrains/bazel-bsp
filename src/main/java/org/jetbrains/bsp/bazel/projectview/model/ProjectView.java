package org.jetbrains.bsp.bazel.projectview.model;

import org.jetbrains.bsp.bazel.projectview.model.sections.specific.DirectoriesSection;
import org.jetbrains.bsp.bazel.projectview.model.sections.specific.TargetsSection;

import java.util.Optional;

public class ProjectView {

  private final Optional<DirectoriesSection> directories;
  private final Optional<TargetsSection> targets;

  private ProjectView(Optional<DirectoriesSection> directories, Optional<TargetsSection> targets) {
    this.directories = directories;
    this.targets = targets;
  }

  public static ProjectView.Builder builder() {
    return new Builder();
  }

  public Optional<DirectoriesSection> getDirectories() {
    return directories;
  }

  public Optional<TargetsSection> getTargets() {
    return targets;
  }

  public static class Builder {

    private Optional<DirectoriesSection> directories = Optional.empty();
    private Optional<TargetsSection> targets = Optional.empty();

    public Builder directories(Optional<DirectoriesSection> directories) {
      this.directories = directories;
      return this;
    }

    public Builder targets(Optional<TargetsSection> target) {
      this.targets = target;
      return this;
    }

    public ProjectView build() {
      return new ProjectView(directories, targets);
    }
  }
}
