package org.jetbrains.bsp.bazel.projectview.parser.sections;

import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewSection;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewSectionHeader;

public abstract class ProjectViewListSectionParser<T extends ProjectViewSection>
    extends ProjectViewSectionParser<T> {

  private static final String EXCLUDED_ENTRY_PREFIX = "-";

  private final boolean exclusionary;

  protected ProjectViewListSectionParser(
      ProjectViewSectionHeader sectionHeader, boolean exclusionary) {
    super(sectionHeader);
    this.exclusionary = exclusionary;
  }

  protected List<String> parseIncludedEntries(String sectionBody) {
    List<String> entries = splitSectionEntries(sectionBody);
    List<String> includedEntries = getIncludedEntries(entries);

    return getValueIfExclusionaryOrElse(includedEntries, entries);
  }

  private List<String> getIncludedEntries(List<String> entries) {
    return entries.stream().filter(entry -> !isExcluded(entry)).collect(Collectors.toList());
  }

  protected List<String> parseExcludedEntries(String sectionBody) {
    List<String> entries = splitSectionEntries(sectionBody);
    List<String> excludedEntries = getExcludedEntries(entries);

    return getValueIfExclusionaryOrElse(excludedEntries, entries);
  }

  private List<String> getExcludedEntries(List<String> entries) {
    return entries.stream()
        .filter(this::isExcluded)
        .map(entry -> entry.substring(1))
        .collect(Collectors.toList());
  }

  private List<String> getValueIfExclusionaryOrElse(List<String> value, List<String> elseValue) {
    if (exclusionary) {
      return value;
    } else {
      return elseValue;
    }
  }

  private boolean isExcluded(String entry) {
    return entry.startsWith(EXCLUDED_ENTRY_PREFIX);
  }
}
