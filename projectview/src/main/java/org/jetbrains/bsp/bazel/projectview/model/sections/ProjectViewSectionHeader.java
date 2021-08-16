package org.jetbrains.bsp.bazel.projectview.model.sections;

public enum ProjectViewSectionHeader {
  DIRECTORIES("directories"),
  TARGETS("targets");

  private final String name;

  ProjectViewSectionHeader(String value) {
    name = value;
  }

  public String toString() {
    return name;
  }
}
