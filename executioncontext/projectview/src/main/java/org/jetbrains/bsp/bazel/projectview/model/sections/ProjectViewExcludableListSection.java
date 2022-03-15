package org.jetbrains.bsp.bazel.projectview.model.sections;

import io.vavr.collection.List;
import java.util.Objects;
import org.apache.commons.collections4.CollectionUtils;

public abstract class ProjectViewExcludableListSection<T> extends ProjectViewListSection<T> {

  protected final List<T> excludedValues;

  protected ProjectViewExcludableListSection(
      String sectionName, List<T> includedValues, List<T> excludedValues) {
    super(sectionName, includedValues);
    this.excludedValues = excludedValues;
  }

  public List<T> getExcludedValues() {
    return excludedValues;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ProjectViewExcludableListSection)) return false;
    ProjectViewExcludableListSection<?> that = (ProjectViewExcludableListSection<?>) o;
    return CollectionUtils.isEqualCollection(values.toJavaList(), that.values.toJavaList())
        && CollectionUtils.isEqualCollection(
            excludedValues.toJavaList(), that.excludedValues.toJavaList())
        && sectionName.equals(that.sectionName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(values, excludedValues, sectionName);
  }
}
