package org.jetbrains.bsp.bazel.projectview.parser.splitter;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import org.apache.commons.collections4.CollectionUtils;

public class ProjectViewRawSections {

  private final List<ProjectViewRawSection> sections;

  public ProjectViewRawSections(List<ProjectViewRawSection> sections) {
    this.sections = sections;
  }

  public Optional<ProjectViewRawSection> getLastSectionWithName(String sectionName) {
    return getAllWithName(sectionName).reduce((first, second) -> second);
  }

  public Stream<ProjectViewRawSection> getAllWithName(String sectionName) {
    return sections.stream().filter(section -> section.hasName(sectionName));
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
    return CollectionUtils.isEqualCollection(sections, sections1.sections);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sections);
  }
}
