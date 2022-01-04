package org.jetbrains.bsp.bazel.projectview.model.sections;

public abstract class ProjectViewSingletonSection extends ProjectViewSection {

  private final String value;

  protected ProjectViewSingletonSection(String sectionName, String value) {
    super(sectionName);
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
