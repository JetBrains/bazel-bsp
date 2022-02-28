package org.jetbrains.bsp.bazel.projectview.model.sections;

import java.util.Objects;

public abstract class ProjectViewSingletonSection<T> extends ProjectViewSection {

  protected final T value;

  protected ProjectViewSingletonSection(String sectionName, T value) {
    super(sectionName);
    this.value = value;
  }

  public T getValue() {
    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ProjectViewSingletonSection)) return false;
    ProjectViewSingletonSection<?> that = (ProjectViewSingletonSection<?>) o;
    return value.equals(that.value) && sectionName.equals(that.sectionName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value, sectionName);
  }
}
