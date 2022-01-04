package org.jetbrains.bsp.bazel.projectview.model.sections;

import com.google.common.collect.ImmutableList;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Objects;

public abstract class ProjectViewListSection extends ProjectViewSection {

  private final List<String> includedValues;
  private final List<String> excludedValues;

  protected ProjectViewListSection(String sectionName) {
    super(sectionName);
    this.includedValues = ImmutableList.of();
    this.excludedValues = ImmutableList.of();
  }

  protected ProjectViewListSection(
      String sectionName, List<String> includedValues, List<String> excludedValues) {
    super(sectionName);
    this.includedValues = includedValues;
    this.excludedValues = excludedValues;
  }

  public List<String> getIncludedValues() {
    return includedValues;
  }

  public List<String> getExcludedValues() {
    return excludedValues;
  }

  @Override
  public String toString() {
    return "ProjectViewListSection{"
        + "includedValues="
        + includedValues
        + ", excludedValues="
        + excludedValues
        + "} "
        + super.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ProjectViewListSection)) return false;
    ProjectViewListSection that = (ProjectViewListSection) o;
    return CollectionUtils.isEqualCollection(getIncludedValues(), that.getIncludedValues())
        && CollectionUtils.isEqualCollection(getExcludedValues(), that.getExcludedValues())
        && getSectionName().equals(that.getSectionName());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getIncludedValues(), getExcludedValues(), getSectionName());
  }
}
