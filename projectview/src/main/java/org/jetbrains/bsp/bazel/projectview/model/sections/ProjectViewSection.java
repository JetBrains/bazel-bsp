package org.jetbrains.bsp.bazel.projectview.model.sections;

abstract class ProjectViewSection {

  protected final String sectionName;

  protected ProjectViewSection(String sectionName) {
    this.sectionName = sectionName;
  }

  public String getSectionName() {
    return sectionName;
  }
}
