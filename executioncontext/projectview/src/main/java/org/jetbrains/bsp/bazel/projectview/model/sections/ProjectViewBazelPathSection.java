package org.jetbrains.bsp.bazel.projectview.model.sections;

import java.nio.file.Path;

public class ProjectViewBazelPathSection extends ProjectViewSingletonSection<Path> {

  public static final String SECTION_NAME = "bazel_path";

  public ProjectViewBazelPathSection(Path value) {
    super(SECTION_NAME, value);
  }

  @Override
  public String toString() {
    return "ProjectViewBazelPathSection{" + "value=" + value + "} ";
  }
}
