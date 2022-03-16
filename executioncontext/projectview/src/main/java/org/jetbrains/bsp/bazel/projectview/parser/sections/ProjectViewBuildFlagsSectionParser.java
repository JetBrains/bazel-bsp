package org.jetbrains.bsp.bazel.projectview.parser.sections;

import io.vavr.collection.List;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBuildFlagsSection;

public class ProjectViewBuildFlagsSectionParser
    extends ProjectViewListSectionParser<String, ProjectViewBuildFlagsSection> {

  public ProjectViewBuildFlagsSectionParser() {
    super(ProjectViewBuildFlagsSection.SECTION_NAME);
  }

  @Override
  protected String mapRawValues(String rawValue) {
    return rawValue;
  }

  @Override
  protected ProjectViewBuildFlagsSection createInstance(List<String> values) {
    return new ProjectViewBuildFlagsSection(values);
  }
}
