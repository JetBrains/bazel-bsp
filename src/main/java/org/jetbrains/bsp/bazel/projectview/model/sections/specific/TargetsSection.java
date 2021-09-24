package org.jetbrains.bsp.bazel.projectview.model.sections.specific;

import java.util.List;
import java.util.Objects;
import org.jetbrains.bsp.bazel.commons.ListUtils;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewSection;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewSectionHeader;

public class TargetsSection extends ProjectViewSection<TargetsSection> {

  private final List<String> includedTargets;
  private final List<String> excludedTargets;

  public TargetsSection(List<String> includedTargets, List<String> excludedTargets) {
    super(ProjectViewSectionHeader.TARGETS);
    this.includedTargets = includedTargets;
    this.excludedTargets = excludedTargets;
  }

  @Override
  public TargetsSection merge(TargetsSection otherSection) {
    List<String> mergedIncludedTargets =
        ListUtils.concat(includedTargets, otherSection.includedTargets);
    List<String> mergedExcludedTargets =
        ListUtils.concat(excludedTargets, otherSection.excludedTargets);

    return new TargetsSection(mergedIncludedTargets, mergedExcludedTargets);
  }

  public List<String> getIncludedTargets() {
    return includedTargets;
  }

  public List<String> getExcludedTargets() {
    return excludedTargets;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TargetsSection that = (TargetsSection) o;
    return Objects.equals(includedTargets, that.includedTargets)
        && Objects.equals(excludedTargets, that.excludedTargets);
  }

  @Override
  public int hashCode() {
    return Objects.hash(includedTargets, excludedTargets);
  }

  @Override
  public String toString() {
    return "TargetsSection{"
        + "includedTargets="
        + includedTargets
        + ", excludedTargets="
        + excludedTargets
        + '}';
  }
}
