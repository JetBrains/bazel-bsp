package org.jetbrains.bsp.bazel.projectview.parser.sections;

import io.vavr.control.Try;
import java.util.Optional;
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
  public Optional<T> parseOrDefault(ProjectViewRawSections rawSections, Optional<T> defaultValue) {
    var section = parse(rawSections);

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
    var section =
        rawSections.getLastSectionWithName(sectionName).map(this::parse).flatMap(Try::get);

    logParse(section);

    return section;
  }

  private void logParse(Optional<T> section) {
    log.debug("Parsed '{}' section. Result:\n{}", sectionName, section);
  }

  @Override
  protected Optional<T> parse(String sectionBody) {
    return Optional.of(sectionBody.strip())
        .filter(body -> !body.isEmpty())
        .map(this::mapRawValue)
        .map(this::createInstance);
  }

  protected abstract V mapRawValue(String rawValue);

  protected abstract T createInstance(V value);
}
