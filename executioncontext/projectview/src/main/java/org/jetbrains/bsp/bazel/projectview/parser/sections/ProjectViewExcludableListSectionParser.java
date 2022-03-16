package org.jetbrains.bsp.bazel.projectview.parser.sections;

import io.vavr.collection.List;
import io.vavr.control.Option;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewExcludableListSection;

/**
 * Implementation of excludable list section parser.
 *
 * <p>It takes a raw section and search for entries - they are split by whitespaces, if entry starts
 * with '-' -- this entry is excluded, otherwise included.
 *
 * @param <T> type of parsed list section
 */
abstract class ProjectViewExcludableListSectionParser<
        V, T extends ProjectViewExcludableListSection<V>>
    extends ProjectViewListSectionParser<V, T> {

  private static final String EXCLUDED_ENTRY_PREFIX = "-";

  protected ProjectViewExcludableListSectionParser(String sectionName) {
    super(sectionName);
  }

  @Override
  protected T concatSectionsItems(T section1, T section2) {
    var includedItems = section1.getValues().appendAll(section2.getValues());
    var excludedItems = section1.getExcludedValues().appendAll(section2.getExcludedValues());

    return createInstance(includedItems, excludedItems);
  }

  @Override
  protected Option<T> parse(List<String> allEntries) {
    var rawIncludedEntries = filterIncludedEntries(allEntries);
    var rawExcludedEntries = filterExcludedEntries(allEntries);

    var includedEntries = rawIncludedEntries.map(this::mapRawValues);
    var excludedEntries = rawExcludedEntries.map(this::mapRawValues);

    return createInstanceOrEmpty(includedEntries, excludedEntries);
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

  @Override
  protected T createInstance(List<V> values) {
    return createInstance(values, List.of());
  }

  protected abstract T createInstance(List<V> includedValues, List<V> excludedValues);
}
