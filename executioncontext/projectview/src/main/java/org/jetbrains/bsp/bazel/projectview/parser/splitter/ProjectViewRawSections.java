package org.jetbrains.bsp.bazel.projectview.parser.splitter;

import io.vavr.collection.List;
import io.vavr.control.Option;
import java.util.Objects;
import org.apache.commons.collections4.CollectionUtils;

public class ProjectViewRawSections {

  private final List<ProjectViewRawSection> sections;

  public ProjectViewRawSections(List<ProjectViewRawSection> sections) {
    this.sections = sections;
  }

  public Option<ProjectViewRawSection> getLastSectionWithName(String sectionName) {
    return sections.findLast(section -> section.hasName(sectionName));
  }

  public List<ProjectViewRawSection> getAllWithName(String sectionName) {
    return sections.filter(section -> section.hasName(sectionName));
  }

  @Override
  public String toString() {
    return "ProjectViewRawSections{" + "sections=" + sections + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ProjectViewRawSections)) return false;
    ProjectViewRawSections sections1 = (ProjectViewRawSections) o;
    return CollectionUtils.isEqualCollection(
        sections.toJavaList(), sections1.sections.toJavaList());
  }

  @Override
  public int hashCode() {
    return Objects.hash(sections);
  }
}
