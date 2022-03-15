package org.jetbrains.bsp.bazel.projectview.model.sections;

import io.vavr.collection.List;
import java.util.Objects;
import org.apache.commons.collections4.CollectionUtils;

public abstract class ProjectViewListSection<T> extends ProjectViewSection {

  protected final List<T> values;

  protected ProjectViewListSection(String sectionName, List<T> values) {
    super(sectionName);
    this.values = values;
  }

  public List<T> getValues() {
    return values;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ProjectViewListSection)) return false;
    ProjectViewListSection<?> that = (ProjectViewListSection<?>) o;
    return CollectionUtils.isEqualCollection(values.toJavaList(), that.values.toJavaList())
        && sectionName.equals(that.sectionName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(values, sectionName);
  }
}
