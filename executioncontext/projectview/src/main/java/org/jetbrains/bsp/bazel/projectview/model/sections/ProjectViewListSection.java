package org.jetbrains.bsp.bazel.projectview.model.sections;

import io.vavr.collection.List;
import java.util.Objects;
import org.apache.commons.collections4.CollectionUtils;

public abstract class ProjectViewListSection<T> extends ProjectViewSection {

  protected final List<T> includedValues;
  protected final List<T> excludedValues;

  protected ProjectViewListSection(String sectionName) {
    super(sectionName);
    this.includedValues = List.of();
    this.excludedValues = List.of();
  }

  protected ProjectViewListSection(
      String sectionName, List<T> includedValues, List<T> excludedValues) {
    super(sectionName);
    this.includedValues = includedValues;
    this.excludedValues = excludedValues;
  }

  public List<T> getIncludedValues() {
    return includedValues;
  }

  public List<T> getExcludedValues() {
    return excludedValues;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ProjectViewListSection)) return false;
    ProjectViewListSection<?> that = (ProjectViewListSection<?>) o;
    return CollectionUtils.isEqualCollection(
            includedValues.toJavaList(), that.includedValues.toJavaList())
        && CollectionUtils.isEqualCollection(
            excludedValues.toJavaList(), that.excludedValues.toJavaList())
        && sectionName.equals(that.sectionName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(includedValues, excludedValues, sectionName);
  }
}
