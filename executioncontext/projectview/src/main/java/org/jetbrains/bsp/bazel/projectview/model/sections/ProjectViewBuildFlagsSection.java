package org.jetbrains.bsp.bazel.projectview.model.sections;

import io.vavr.collection.List;

public class ProjectViewBuildFlagsSection extends ProjectViewListSection<String> {

  public static final String SECTION_NAME = "build_flags";

  public ProjectViewBuildFlagsSection(List<String> values) {
    super(SECTION_NAME, values);
  }

  @Override
  public String toString() {
    return "ProjectViewBuildFlagsSection{" + "values=" + values + '}';
  }
}
