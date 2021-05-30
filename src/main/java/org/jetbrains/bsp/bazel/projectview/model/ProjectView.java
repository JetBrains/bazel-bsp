package org.jetbrains.bsp.bazel.projectview.model;

import org.jetbrains.bsp.bazel.projectview.model.sections.specific.DirectoriesSection;
import org.jetbrains.bsp.bazel.projectview.model.sections.specific.TargetsSection;

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
