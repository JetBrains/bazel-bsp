package org.jetbrains.bsp.bazel.projectview.model.sections;

import java.util.List;
import java.util.Objects;
import org.apache.commons.collections4.CollectionUtils;

public abstract class ProjectViewListSection extends ProjectViewSection {

  protected final List<String> includedValues;
  protected final List<String> excludedValues;

  protected ProjectViewListSection(String sectionName) {
    super(sectionName);
    this.includedValues = List.of();
    this.excludedValues = List.of();
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
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ProjectViewListSection)) return false;
    ProjectViewListSection that = (ProjectViewListSection) o;
    return CollectionUtils.isEqualCollection(includedValues, that.includedValues)
        && CollectionUtils.isEqualCollection(excludedValues, that.excludedValues)
        && sectionName.equals(that.sectionName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(includedValues, excludedValues, sectionName);
  }
}
