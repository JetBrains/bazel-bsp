package org.jetbrains.bsp.bazel.projectview.model.sections;

import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import io.vavr.collection.List;

public class ProjectViewTargetsSection
    extends ProjectViewExcludableListSection<BuildTargetIdentifier> {

  public static final String SECTION_NAME = "targets";

  public ProjectViewTargetsSection(
      List<BuildTargetIdentifier> includedValues, List<BuildTargetIdentifier> excludedValues) {
    super(SECTION_NAME, includedValues, excludedValues);
  }

  @Override
  public String toString() {
    return "ProjectViewTargetsSection{"
        + "includedValues="
        + values
        + ", excludedValues="
        + excludedValues
        + "} ";
  }
}
