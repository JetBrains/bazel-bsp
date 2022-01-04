package org.jetbrains.bsp.bazel.projectview.parser.sections;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import org.jetbrains.bsp.bazel.commons.ListUtils;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewListSection;
import org.jetbrains.bsp.bazel.projectview.parser.splitter.ProjectViewRawSections;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public abstract class ProjectViewListSectionParser<T extends ProjectViewListSection>
    extends ProjectViewSectionParser<T> {

  private static final String EXCLUDED_ENTRY_PREFIX = "-";
  private static final Pattern WHITESPACE_CHAR_REGEX = Pattern.compile("[ \n\t]+");

  protected ProjectViewListSectionParser(String sectionName) {
    super(sectionName);
  }

  @Override
  public T parse(ProjectViewRawSections rawSections) {
    return parseAllSectionsAndMerge(rawSections)
        .orElse(instanceOf(ImmutableList.of(), ImmutableList.of()));
  }

  @Override
  public T parseOrDefault(ProjectViewRawSections rawSections, T defaultValue) {
    return parseAllSectionsAndMerge(rawSections).orElse(defaultValue);
  }

  private Optional<T> parseAllSectionsAndMerge(ProjectViewRawSections rawSections) {
    return rawSections.getAllWithName(sectionName).stream()
        .map(this::parse)
        .reduce(this::concatSectionsItems);
  }

  private T concatSectionsItems(T section1, T section2) {
    List<String> includedItems =
        ListUtils.concat(section1.getIncludedValues(), section2.getIncludedValues());
    List<String> excludedItems =
        ListUtils.concat(section1.getExcludedValues(), section2.getExcludedValues());

    return instanceOf(includedItems, excludedItems);
  }

  @Override
  protected T parse(String sectionBody) {
    List<String> allEntries = splitSectionEntries(sectionBody);
    List<String> includedEntries = filterIncludedEntries(allEntries);
    List<String> excludedEntries = filterExcludedEntries(allEntries);

    return instanceOf(includedEntries, excludedEntries);
  }

  private List<String> splitSectionEntries(String sectionBody) {
    return Splitter.on(WHITESPACE_CHAR_REGEX).omitEmptyStrings().splitToList(sectionBody);
  }

  private List<String> filterIncludedEntries(List<String> entries) {
    return entries.stream().filter(entry -> !isExcluded(entry)).collect(Collectors.toList());
  }

  private List<String> filterExcludedEntries(List<String> entries) {
    return entries.stream()
        .filter(this::isExcluded)
        .map(this::removeExcludedEntryPrefix)
        .collect(Collectors.toList());
  }

  private String removeExcludedEntryPrefix(String excludedEntry) {
    return excludedEntry.substring(1);
  }

  private boolean isExcluded(String entry) {
    return entry.startsWith(EXCLUDED_ENTRY_PREFIX);
  }

  protected abstract T instanceOf(List<String> includedValues, List<String> excludedValues);
}
