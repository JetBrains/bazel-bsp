package org.jetbrains.bsp.bazel.projectview.parser.sections;

import io.vavr.control.Try;
import java.util.Optional;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewSingletonSection;
import org.jetbrains.bsp.bazel.projectview.parser.splitter.ProjectViewRawSections;

/**
 * Implementation of single value section parser.
 *
 * <p>It takes a raw section and trims the content.
 *
 * @param <T> type of parsed single value section
 */
abstract class ProjectViewSingletonSectionParser<T extends ProjectViewSingletonSection>
    extends ProjectViewSectionParser<Optional<T>> {

  protected ProjectViewSingletonSectionParser(String sectionName) {
    super(sectionName);
  }

  @Override
  public Optional<T> parseOrDefault(ProjectViewRawSections rawSections, Optional<T> defaultValue) {
    return parse(rawSections).or(() -> defaultValue);
  }

  @Override
  public Optional<T> parse(ProjectViewRawSections rawSections) {
    return rawSections.getLastSectionWithName(sectionName).map(this::parse).flatMap(Try::get);
  }

  @Override
  protected Optional<T> parse(String sectionBody) {
    return Optional.of(sectionBody.strip())
        .filter(body -> !body.isEmpty())
        .map(this::createInstance);
  }

  protected abstract T createInstance(String value);
}
