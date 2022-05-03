package org.jetbrains.bsp.bazel.workspacecontext.entries;

import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import io.vavr.collection.List;
import java.util.Objects;
import org.apache.commons.collections4.CollectionUtils;
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
    return CollectionUtils.isEqualCollection(
            includedValues.toJavaList(), that.includedValues.toJavaList())
        && CollectionUtils.isEqualCollection(
            excludedValues.toJavaList(), that.excludedValues.toJavaList());
  }

  @Override
  public int hashCode() {
    return Objects.hash(includedValues, excludedValues);
  }
}
