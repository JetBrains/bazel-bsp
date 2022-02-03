package org.jetbrains.bsp.bazel.projectview.parser.sections;

import java.util.Optional;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewSingletonSection;
import org.jetbrains.bsp.bazel.projectview.parser.splitter.ProjectViewRawSections;

public abstract class ProjectViewSingletonSectionParser<T extends ProjectViewSingletonSection>
    extends ProjectViewSectionParser<Optional<T>> {

  protected ProjectViewSingletonSectionParser(String sectionName) {
    super(sectionName);
  }

  @Override
  public Optional<T> parseOrDefault(ProjectViewRawSections rawSections, Optional<T> defaultValue) {
    return parse(rawSections).map(Optional::of).orElse(defaultValue);
  }

  @Override
  public Optional<T> parse(ProjectViewRawSections rawSections) {
    return rawSections.getLastSectionWithName(sectionName).flatMap(this::parse);
  }

  @Override
  protected Optional<T> parse(String sectionBody) {
    return Optional.of(sectionBody.trim()).filter(body -> !body.isEmpty()).map(this::instanceOf);
  }

  protected abstract T instanceOf(String value);
}
