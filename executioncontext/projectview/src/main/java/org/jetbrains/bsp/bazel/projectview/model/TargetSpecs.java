package org.jetbrains.bsp.bazel.projectview.model;

import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import io.vavr.collection.List;
import io.vavr.collection.Traversable;
import java.util.Objects;

public class TargetSpecs {
  private final List<BuildTargetIdentifier> included;
  private final List<BuildTargetIdentifier> excluded;

  public TargetSpecs(List<BuildTargetIdentifier> included, List<BuildTargetIdentifier> excluded) {
    this.included = included;
    this.excluded = excluded;
  }

  public static TargetSpecs empty() {
    return new TargetSpecs(List.empty(), List.empty());
  }

  public static TargetSpecs of(Traversable<BuildTargetIdentifier> included) {
    return new TargetSpecs(included.toList(), List.empty());
  }

  public List<BuildTargetIdentifier> included() {
    return included;
  }

  public List<BuildTargetIdentifier> excluded() {
    return excluded;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TargetSpecs that = (TargetSpecs) o;
    return Objects.equals(included, that.included) && Objects.equals(excluded, that.excluded);
  }

  @Override
  public int hashCode() {
    return Objects.hash(included, excluded);
  }
}
