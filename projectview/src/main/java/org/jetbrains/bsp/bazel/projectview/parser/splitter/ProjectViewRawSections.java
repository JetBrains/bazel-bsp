package org.jetbrains.bsp.bazel.projectview.parser.splitter;

import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class ProjectViewRawSections {

  private final List<ProjectViewRawSection> sections;

  public ProjectViewRawSections(List<ProjectViewRawSection> sections) {
    this.sections = sections;
  }

  public Optional<ProjectViewRawSection> getLastSectionWithName(String sectionName) {
    return getAllWithName(sectionName).stream()
        .reduce((first, second) -> second);
  }

  public List<ProjectViewRawSection> getAllWithName(String sectionName) {
    return sections.stream()
        .filter(section -> section.compareByName(sectionName))
        .collect(Collectors.toList());
  }

  @Override
  public String toString() {
    return "ProjectViewRawSections{" +
        "sections=" + sections +
        '}';
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
