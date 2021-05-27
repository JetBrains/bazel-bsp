package org.jetbrains.bsp.bazel.projectview.model.sections;

public abstract class ProjectViewSection {

  protected final ProjectViewSectionHeader header;

  protected ProjectViewSection(ProjectViewSectionHeader header) {
    this.header = header;
  }
}
