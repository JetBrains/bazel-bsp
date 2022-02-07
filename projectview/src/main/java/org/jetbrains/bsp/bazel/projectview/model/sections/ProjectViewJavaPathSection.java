package org.jetbrains.bsp.bazel.projectview.model.sections;

public class ProjectViewJavaPathSection extends ProjectViewSingletonSection {

  public static final String SECTION_NAME = "java_path";

  public ProjectViewJavaPathSection(String value) {
    super(SECTION_NAME, value);
  }

  @Override
  public String toString() {
    return "ProjectViewJavaPathSection{" + "value=" + value + "} ";
  }
}
