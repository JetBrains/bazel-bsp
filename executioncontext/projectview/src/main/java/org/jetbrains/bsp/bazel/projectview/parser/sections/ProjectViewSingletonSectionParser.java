package org.jetbrains.bsp.bazel.projectview.parser.sections;

import io.vavr.control.Option;
import io.vavr.control.Try;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewSingletonSection;
import org.jetbrains.bsp.bazel.projectview.parser.splitter.ProjectViewRawSections;

/**
 * Implementation of single value section parser.
 *
 * <p>It takes a raw section and trims the content.
 *
 * @param <T> type of parsed single value section
 */
abstract class ProjectViewSingletonSectionParser<V, T extends ProjectViewSingletonSection<V>>
    extends ProjectViewSectionParser<T> {

  private static final Logger log = LogManager.getLogger(ProjectViewSingletonSectionParser.class);

  protected ProjectViewSingletonSectionParser(String sectionName) {
    super(sectionName);
  }

  @Override
  public Option<T> parseOrDefault(ProjectViewRawSections rawSections, Option<T> defaultValue) {
    var section = parse(rawSections);

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
    var section =
        rawSections.getLastSectionWithName(sectionName).map(this::parse).flatMap(Try::get);

    logParse(section);

    return section;
  }

  private void logParse(Option<T> section) {
    log.debug("Parsed '{}' section. Result:\n{}", sectionName, section);
  }

  @Override
  protected Option<T> parse(String sectionBody) {
    return Option.of(sectionBody.strip())
        .filter(body -> !body.isEmpty())
        .map(this::mapRawValue)
        .map(this::createInstance);
  }

  protected abstract V mapRawValue(String rawValue);

  protected abstract T createInstance(V value);
}
