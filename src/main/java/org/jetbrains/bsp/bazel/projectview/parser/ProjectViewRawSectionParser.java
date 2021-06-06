package org.jetbrains.bsp.bazel.projectview.parser;

import java.util.List;
import java.util.Optional;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewSection;
import org.jetbrains.bsp.bazel.projectview.parser.sections.ProjectViewSectionParser;
import org.jetbrains.bsp.bazel.projectview.parser.splitter.ProjectViewRawSection;

public class ProjectViewRawSectionParser<T extends ProjectViewSection> {

  private final ProjectViewSectionParser<T> parser;

  private ProjectViewRawSectionParser(ProjectViewSectionParser<T> parser) {
    this.parser = parser;
  }

  public static <T extends ProjectViewSection> ProjectViewRawSectionParser<T> forParser(
      ProjectViewSectionParser<T> parser) {
    return new ProjectViewRawSectionParser<>(parser);
  }

  public T parseRawSections(List<ProjectViewRawSection> rawSections) {
    return parseOptionalRawSections(rawSections)
        .orElseThrow(
            () ->
                new IllegalArgumentException(this.parser.sectionName() + " section is required!"));
  }

  public Optional<T> parseOptionalRawSections(List<ProjectViewRawSection> rawSections) {
    return rawSections.stream()
        .filter(this::isSectionParsable)
        .findFirst()
        .map(section -> parser.parse(section.getSectionBody()));
  }

  private boolean isSectionParsable(ProjectViewRawSection rawSection) {
    return parser.isSectionParsable(rawSection.getSectionHeader());
  }
}
