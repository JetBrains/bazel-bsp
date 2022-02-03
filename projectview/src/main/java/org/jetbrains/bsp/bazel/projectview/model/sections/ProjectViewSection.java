package org.jetbrains.bsp.bazel.projectview.model.sections;

public abstract class ProjectViewSection {

  private final String sectionName;

  protected ProjectViewSection(String sectionName) {
    this.sectionName = sectionName;
  }

  public String getSectionName() {
    return sectionName;
  }
}
