package org.jetbrains.bsp.bazel.projectview.parser.sections;

import com.google.common.base.Splitter;
import io.vavr.control.Try;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jetbrains.bsp.bazel.commons.ListUtils;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewListSection;
import org.jetbrains.bsp.bazel.projectview.parser.splitter.ProjectViewRawSections;

/**
 * Implementation of list section parser.
 *
 * <p>It takes a raw section and search for entries - they are split by whitespaces, if entry starts
 * with '-' -- this entry is excluded, otherwise included.
 *
 * @param <T> type of parsed list section
 */
abstract class ProjectViewListSectionParser<T extends ProjectViewListSection>
    extends ProjectViewSectionParser<T> {

  private static final String EXCLUDED_ENTRY_PREFIX = "-";
  private static final Pattern WHITESPACE_CHAR_REGEX = Pattern.compile("[ \n\t]+");

  protected ProjectViewListSectionParser(String sectionName) {
    super(sectionName);
  }

  @Override
  public T parse(ProjectViewRawSections rawSections) {
    return parseAllSectionsAndMerge(rawSections).orElse(createInstance(List.of(), List.of()));
  }

  @Override
  public T parseOrDefault(ProjectViewRawSections rawSections, T defaultValue) {
    return parseAllSectionsAndMerge(rawSections).orElse(defaultValue);
  }

  private Optional<T> parseAllSectionsAndMerge(ProjectViewRawSections rawSections) {
    return rawSections
        .getAllWithName(sectionName)
        .map(this::parse)
        .map(Try::get)
        .reduce(this::concatSectionsItems);
  }

  private T concatSectionsItems(T section1, T section2) {
    var includedItems =
        ListUtils.concat(section1.getIncludedValues(), section2.getIncludedValues());
    var excludedItems =
        ListUtils.concat(section1.getExcludedValues(), section2.getExcludedValues());

    return createInstance(includedItems, excludedItems);
  }

  @Override
  protected T parse(String sectionBody) {
    var allEntries = splitSectionEntries(sectionBody);
    var includedEntries = filterIncludedEntries(allEntries);
    var excludedEntries = filterExcludedEntries(allEntries);

    return createInstance(includedEntries, excludedEntries);
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

  protected abstract T createInstance(List<String> includedValues, List<String> excludedValues);
}
