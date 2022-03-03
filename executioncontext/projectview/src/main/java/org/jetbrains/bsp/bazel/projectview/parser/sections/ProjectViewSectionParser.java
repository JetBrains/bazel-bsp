package org.jetbrains.bsp.bazel.projectview.parser.sections;

import io.vavr.control.Try;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewSection;
import org.jetbrains.bsp.bazel.projectview.parser.splitter.ProjectViewRawSection;
import org.jetbrains.bsp.bazel.projectview.parser.splitter.ProjectViewRawSections;

abstract class ProjectViewSectionParser<T extends ProjectViewSection> {

  private static final Logger log = LogManager.getLogger(ProjectViewSectionParser.class);

  protected final String sectionName;

  protected ProjectViewSectionParser(String sectionName) {
    this.sectionName = sectionName;
  }

  public abstract Optional<T> parseOrDefault(
      ProjectViewRawSections rawSections, Optional<T> defaultValue);

  public abstract Optional<T> parse(ProjectViewRawSections rawSections);

  public Try<Optional<T>> parse(ProjectViewRawSection rawSection) {
    return getSectionBodyOrFailureIfNameIsWrong(rawSection)
        .onFailure(
            exception ->
                log.error(
                    "Failed to parse section with '{}'. Expected name: '{}'. Parsing failed!",
                    rawSection.getSectionName(),
                    sectionName,
                    exception))
        .map(this::parse);
  }

  private Try<String> getSectionBodyOrFailureIfNameIsWrong(ProjectViewRawSection rawSection) {
    if (!rawSection.hasName(sectionName)) {
      var exceptionMessage =
          "Project view parsing failed! Expected '"
              + sectionName
              + "' section name, got '"
              + rawSection.getSectionName()
              + "'!";
      return Try.failure(new IllegalArgumentException(exceptionMessage));
    }

    return Try.success(rawSection.getSectionBody());
  }

  protected abstract Optional<T> parse(String sectionBody);
}
