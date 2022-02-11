package org.jetbrains.bsp.bazel.projectview.parser.sections;

import io.vavr.control.Try;
import org.jetbrains.bsp.bazel.projectview.parser.splitter.ProjectViewRawSection;
import org.jetbrains.bsp.bazel.projectview.parser.splitter.ProjectViewRawSections;

abstract class ProjectViewSectionParser<T> {

  protected final String sectionName;

  protected ProjectViewSectionParser(String sectionName) {
    this.sectionName = sectionName;
  }

  public abstract T parse(ProjectViewRawSections rawSections);

  public abstract T parseOrDefault(ProjectViewRawSections rawSections, T defaultValue);

  public Try<T> parse(ProjectViewRawSection rawSection) {
    return getSectionBodyOrFailureIfNameIsWrong(rawSection).map(this::parse);
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

  protected abstract T parse(String sectionBody);
}
