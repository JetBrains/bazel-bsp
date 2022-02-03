package org.jetbrains.bsp.bazel.projectview.model.sections;

import java.util.Objects;

public abstract class ProjectViewSingletonSection extends ProjectViewSection {

  protected final String value;

  protected ProjectViewSingletonSection(String sectionName, String value) {
    super(sectionName);
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return "ProjectViewSingletonSection{" +
        "value='" + value + '\'' +
        "} " + super.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ProjectViewSingletonSection)) return false;
    ProjectViewSingletonSection that = (ProjectViewSingletonSection) o;
    return value.equals(that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }
}
