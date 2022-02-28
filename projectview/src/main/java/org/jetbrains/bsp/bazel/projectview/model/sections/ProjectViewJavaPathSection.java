package org.jetbrains.bsp.bazel.projectview.model.sections;

import java.nio.file.Path;

public class ProjectViewJavaPathSection extends ProjectViewSingletonSection<Path> {

  public static final String SECTION_NAME = "java_path";

  public ProjectViewJavaPathSection(Path value) {
    super(SECTION_NAME, value);
  }

  @Override
  public String toString() {
    return "ProjectViewJavaPathSection{" + "value=" + value + "} ";
  }
}
