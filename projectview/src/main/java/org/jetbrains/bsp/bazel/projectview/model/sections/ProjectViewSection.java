package org.jetbrains.bsp.bazel.projectview.model.sections;

public abstract class ProjectViewSection<T extends ProjectViewSection<T>> {

  protected final ProjectViewSectionHeader header;

  protected ProjectViewSection(ProjectViewSectionHeader header) {
    this.header = header;
  }

  public abstract T merge(T otherSection);
}
