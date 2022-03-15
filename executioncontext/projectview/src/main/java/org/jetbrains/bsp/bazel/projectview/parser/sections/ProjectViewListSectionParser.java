package org.jetbrains.bsp.bazel.projectview.parser.sections;

import io.vavr.collection.List;
import io.vavr.control.Option;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
  public Option<T> parseOrDefault(ProjectViewRawSections rawSections, Option<T> defaultValue) {
    var section = parseAllSectionsAndMerge(rawSections);

    logParseOrDefault(section, defaultValue);

    return section.orElse(defaultValue);
  }

  private void logParseOrDefault(Option<T> section, Option<T> defaultValue) {
    if (section.isDefined()) {
      log.debug("Parsed '{}' section. Result:\n{}", sectionName, section);
    } else {
      log.debug("Returning default for '{}' section. Result:\n{}", sectionName, defaultValue);
    }
  }

  @Override
  public Option<T> parse(ProjectViewRawSections rawSections) {
    var section = parseAllSectionsAndMerge(rawSections);

    logParse(section);

    return section;
  }

  private void logParse(Option<T> section) {
    log.debug("Parsed '{}' section. Result:\n{}", sectionName, section);
  }

  private Option<T> parseAllSectionsAndMerge(ProjectViewRawSections rawSections) {
    return rawSections
        .getAllWithName(sectionName)
        .flatMap(this::parse)
        .flatMap(x -> x)
        .reduceOption(this::concatSectionsItems);
  }

  private T concatSectionsItems(T section1, T section2) {
    var includedItems = section1.getIncludedValues().appendAll(section2.getIncludedValues());
    var excludedItems = section1.getExcludedValues().appendAll(section2.getExcludedValues());

    return createInstance(includedItems, excludedItems);
  }

  @Override
  protected Option<T> parse(String sectionBody) {
    var allEntries = splitSectionEntries(sectionBody);
    var rawIncludedEntries = filterIncludedEntries(allEntries);
    var rawExcludedEntries = filterExcludedEntries(allEntries);

    var includedEntries = rawIncludedEntries.map(this::mapRawValues);
    var excludedEntries = rawExcludedEntries.map(this::mapRawValues);

    return createInstanceOrEmpty(includedEntries, excludedEntries);
  }

  private List<String> splitSectionEntries(String sectionBody) {
    var elements = WHITESPACE_CHAR_REGEX.split(sectionBody);

    return List.of(elements).filter(s -> !s.isBlank());
  }

  private List<String> filterIncludedEntries(List<String> entries) {
    return entries.filter(entry -> !isExcluded(entry));
  }

  private List<String> filterExcludedEntries(List<String> entries) {
    return entries.filter(this::isExcluded).map(this::removeExcludedEntryPrefix);
  }

  private String removeExcludedEntryPrefix(String excludedEntry) {
    return excludedEntry.substring(1);
  }

  private boolean isExcluded(String entry) {
    return entry.startsWith(EXCLUDED_ENTRY_PREFIX);
  }

  protected abstract V mapRawValues(String rawValue);

  private Option<T> createInstanceOrEmpty(List<V> includedValues, List<V> excludedValues) {
    var areBothListsEmpty = includedValues.isEmpty() && excludedValues.isEmpty();
    var isAnyValuePresent = !areBothListsEmpty;

    return Option.when(isAnyValuePresent, createInstance(includedValues, excludedValues));
  }

  protected abstract T createInstance(List<V> includedValues, List<V> excludedValues);
}
