package org.jetbrains.bsp.bazel.workspacecontext.entries;

import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import java.util.List;
import java.util.Objects;
import org.jetbrains.bsp.bazel.executioncontext.api.entries.ExecutionContextListEntity;

public class ExecutionContextTargetsEntity
    extends ExecutionContextListEntity<BuildTargetIdentifier> {

  public ExecutionContextTargetsEntity(
      List<BuildTargetIdentifier> includedValues, List<BuildTargetIdentifier> excludedValues) {
    super(includedValues, excludedValues);
  }

  @Override
  public String toString() {
    return "ExecutionContextTargetsEntity{"
        + "includedValues="
        + includedValues
        + ", excludedValues="
        + excludedValues
        + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ExecutionContextTargetsEntity that = (ExecutionContextTargetsEntity) o;
    return includedValues.equals(that.includedValues) && excludedValues.equals(that.excludedValues);
  }

  @Override
  public int hashCode() {
    return Objects.hash(includedValues, excludedValues);
  }
}
