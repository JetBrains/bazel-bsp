package org.jetbrains.bsp.bazel.projectview.parser.sections;

import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import io.vavr.collection.List;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewTargetsSection;

public class ProjectViewTargetsSectionParser
    extends ProjectViewListSectionParser<BuildTargetIdentifier, ProjectViewTargetsSection> {

  public ProjectViewTargetsSectionParser() {
    super(ProjectViewTargetsSection.SECTION_NAME);
  }

  @Override
  protected BuildTargetIdentifier mapRawValues(String rawValue) {
    return new BuildTargetIdentifier(rawValue);
  }

  @Override
  protected ProjectViewTargetsSection createInstance(
      List<BuildTargetIdentifier> includedValues, List<BuildTargetIdentifier> excludedValues) {
    return new ProjectViewTargetsSection(includedValues, excludedValues);
  }
}
