package org.jetbrains.bsp.bazel.projectview.parser.sections;

import io.vavr.control.Option;
import io.vavr.control.Try;
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

  public abstract Option<T> parseOrDefault(
      ProjectViewRawSections rawSections, Option<T> defaultValue);

  public abstract Option<T> parse(ProjectViewRawSections rawSections);

  public Try<Option<T>> parse(ProjectViewRawSection rawSection) {
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

  protected abstract Option<T> parse(String sectionBody);
}
