package org.jetbrains.bsp.bazel.projectview.parser.sections;

import com.google.common.base.Splitter;
import io.vavr.control.Try;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
abstract class ProjectViewListSectionParser<V, T extends ProjectViewListSection<V>>
    extends ProjectViewSectionParser<T> {

  private static final Logger log = LogManager.getLogger(ProjectViewListSectionParser.class);

  private static final String EXCLUDED_ENTRY_PREFIX = "-";
  private static final Pattern WHITESPACE_CHAR_REGEX = Pattern.compile("[ \n\t]+");

  protected ProjectViewListSectionParser(String sectionName) {
    super(sectionName);
  }

  @Override
  public Optional<T> parseOrDefault(ProjectViewRawSections rawSections, Optional<T> defaultValue) {
    var section = parseAllSectionsAndMerge(rawSections);

    logParseOrDefault(section, defaultValue);

    return section.or(() -> defaultValue);
  }

  private void logParseOrDefault(Optional<T> section, Optional<T> defaultValue) {
    if (section.isPresent()) {
      log.debug("Parsed '{}' section. Result:\n{}", sectionName, section);
    } else {
      log.debug("Returning default for '{}' section. Result:\n{}", sectionName, defaultValue);
    }
  }

  @Override
  public Optional<T> parse(ProjectViewRawSections rawSections) {
    var section = parseAllSectionsAndMerge(rawSections);

    logParse(section);

    return section;
  }

  private void logParse(Optional<T> section) {
    log.debug("Parsed '{}' section. Result:\n{}", sectionName, section);
  }

  private Optional<T> parseAllSectionsAndMerge(ProjectViewRawSections rawSections) {
    return rawSections
        .getAllWithName(sectionName)
        .map(this::parse)
        .map(Try::get)
        .flatMap(Optional::stream)
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
  protected Optional<T> parse(String sectionBody) {
    var allEntries = splitSectionEntries(sectionBody);
    var rawIncludedEntries = filterIncludedEntries(allEntries);
    var rawExcludedEntries = filterExcludedEntries(allEntries);

    var includedEntries = mapRawValues(rawIncludedEntries);
    var excludedEntries = mapRawValues(rawExcludedEntries);

    return createInstanceOrEmpty(includedEntries, excludedEntries);
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

  private List<V> mapRawValues(List<String> rawValues) {
    return rawValues.stream().map(this::mapRawValues).collect(Collectors.toList());
  }

  protected abstract V mapRawValues(String rawValue);

  private Optional<T> createInstanceOrEmpty(List<V> includedValues, List<V> excludedValues) {
    if (includedValues.isEmpty() && excludedValues.isEmpty()) {
      return Optional.empty();
    }

    return Optional.ofNullable(createInstance(includedValues, excludedValues));
  }

  protected abstract T createInstance(List<V> includedValues, List<V> excludedValues);
}
