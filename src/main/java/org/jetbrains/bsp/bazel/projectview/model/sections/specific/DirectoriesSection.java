package org.jetbrains.bsp.bazel.projectview.model.sections.specific;

import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewSection;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewSectionHeader;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class DirectoriesSection extends ProjectViewSection {

  private final List<Path> includedDirectories;
  private final List<Path> excludedDirectories;

  public DirectoriesSection(List<Path> includedDirectories, List<Path> excludedDirectories) {
    super(ProjectViewSectionHeader.DIRECTORIES);
    this.includedDirectories = includedDirectories;
    this.excludedDirectories = excludedDirectories;
  }

  public List<Path> getIncludedDirectories() {
    return includedDirectories;
  }

  public List<Path> getExcludedDirectories() {
    return excludedDirectories;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DirectoriesSection that = (DirectoriesSection) o;
    return Objects.equals(includedDirectories, that.includedDirectories) && Objects.equals(excludedDirectories, that.excludedDirectories);
  }

  @Override
  public int hashCode() {
    return Objects.hash(includedDirectories, excludedDirectories);
  }
}
