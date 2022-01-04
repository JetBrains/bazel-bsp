package org.jetbrains.bsp.bazel.projectview.parser.sections;

import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewSection;
import org.jetbrains.bsp.bazel.projectview.parser.splitter.ProjectViewRawSection;
import org.jetbrains.bsp.bazel.projectview.parser.splitter.ProjectViewRawSections;

public abstract class ProjectViewSectionParser<T extends ProjectViewSection> {

  protected final String sectionName;

  protected ProjectViewSectionParser(String sectionName) {
    this.sectionName = sectionName;
  }

  public abstract T parse(ProjectViewRawSections rawSections);

  public abstract T parseOrDefault(ProjectViewRawSections rawSections, T defaultValue);

  public T parse(ProjectViewRawSection rawSection) throws IllegalArgumentException {
    assertSectionName(rawSection);

    return parse(rawSection.getSectionBody());
  }

  private void assertSectionName(ProjectViewRawSection rawSection) throws IllegalArgumentException {
    if (!rawSection.compareByName(sectionName)) {
      String exceptionMessage =
          String.format(
              "Project view parsing failed! Expected '%s' section name, got '%s'!",
              sectionName, rawSection.getSectionName());
      throw new IllegalArgumentException(exceptionMessage);
    }
  }

  protected abstract T parse(String sectionBody);
}
