package org.jetbrains.bsp.bazel.projectview.model.sections;

public class ProjectViewBazelPathSection extends ProjectViewSingletonSection {

  public static final String SECTION_NAME = "bazel_path";

  public ProjectViewBazelPathSection(String value) {
    super(SECTION_NAME, value);
  }

  @Override
  public String toString() {
    return "ProjectViewBazelPathSection{" + "value=" + value + "} ";
  }
}
