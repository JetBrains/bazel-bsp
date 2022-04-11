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
 * <p>It takes a raw section and search for entries - they are split by whitespaces, no entry is
 * excluded even if it starts with '-'.
 *
 * @param <T> type of parsed list section
 */
abstract class ProjectViewListSectionParser<V, T extends ProjectViewListSection<V>>
    extends ProjectViewSectionParser<T> {

  private static final Logger log = LogManager.getLogger(ProjectViewListSectionParser.class);

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

  protected T concatSectionsItems(T section1, T section2) {
    var includedItems = section1.getValues().appendAll(section2.getValues());

    return createInstance(includedItems);
  }

  @Override
  protected Option<T> parse(String sectionBody) {
    var allEntries = splitSectionEntries(sectionBody);

    return parse(allEntries);
  }

  private List<String> splitSectionEntries(String sectionBody) {
    var elements = WHITESPACE_CHAR_REGEX.split(sectionBody);

    return List.of(elements).filter(s -> !s.isBlank());
  }

  protected Option<T> parse(List<String> allEntries) {
    var values = allEntries.map(this::mapRawValues);

    return createInstanceOrEmpty(values);
  }

  protected abstract V mapRawValues(String rawValue);

  private Option<T> createInstanceOrEmpty(List<V> values) {
    var isAnyValuePresent = !values.isEmpty();

    return Option.when(isAnyValuePresent, createInstance(values));
  }

  protected abstract T createInstance(List<V> values);
}
