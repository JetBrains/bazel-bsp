package org.jetbrains.bsp.bazel.projectview.parser.sections;

import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewTargetsSection;

import java.util.List;

public class ProjectViewTargetsSectionParser extends ProjectViewListSectionParser<ProjectViewTargetsSection> {

  public ProjectViewTargetsSectionParser() {
    super(ProjectViewTargetsSection.SECTION_NAME);
  }

  @Override
  protected ProjectViewTargetsSection instanceOf(List<String> includedValues, List<String> excludedValues) {
    return new ProjectViewTargetsSection(includedValues, excludedValues);
  }
}
