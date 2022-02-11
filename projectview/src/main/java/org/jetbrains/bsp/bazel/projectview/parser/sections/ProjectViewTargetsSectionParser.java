package org.jetbrains.bsp.bazel.projectview.parser.sections;

import java.util.List;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewTargetsSection;

public class ProjectViewTargetsSectionParser
    extends ProjectViewListSectionParser<ProjectViewTargetsSection> {

  public ProjectViewTargetsSectionParser() {
    super(ProjectViewTargetsSection.SECTION_NAME);
  }

  @Override
  protected ProjectViewTargetsSection createInstance(
      List<String> includedValues, List<String> excludedValues) {
    return new ProjectViewTargetsSection(includedValues, excludedValues);
  }
}
