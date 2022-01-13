package org.jetbrains.bsp.bazel.projectview.model.sections;

import java.util.List;

public class ProjectViewTargetsSection extends ProjectViewListSection {

  public static final String SECTION_NAME = "targets";

  public ProjectViewTargetsSection() {
    super(SECTION_NAME);
  }

  public ProjectViewTargetsSection(List<String> includedValues, List<String> excludedValues) {
    super(SECTION_NAME, includedValues, excludedValues);
  }

  @Override
  public String toString() {
    return "ProjectViewTargetsSection{"
        + "includedValues="
        + includedValues
        + ", excludedValues="
        + excludedValues
        + "} ";
  }
}
