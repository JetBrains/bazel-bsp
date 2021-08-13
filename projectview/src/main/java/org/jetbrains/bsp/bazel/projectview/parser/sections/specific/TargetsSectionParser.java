package org.jetbrains.bsp.bazel.projectview.parser.sections.specific;

import java.util.List;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewSectionHeader;
import org.jetbrains.bsp.bazel.projectview.model.sections.specific.TargetsSection;
import org.jetbrains.bsp.bazel.projectview.parser.sections.ProjectViewListSectionParser;

public class TargetsSectionParser extends ProjectViewListSectionParser<TargetsSection> {

  public TargetsSectionParser() {
    super(ProjectViewSectionHeader.TARGETS, true);
  }

  @Override
  public TargetsSection parse(String sectionBody) {
    List<String> includedEntries = parseIncludedEntries(sectionBody);
    List<String> excludedEntries = parseExcludedEntries(sectionBody);

    return new TargetsSection(includedEntries, excludedEntries);
  }
}
