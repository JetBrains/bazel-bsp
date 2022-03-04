package org.jetbrains.bsp.bazel.executioncontext.api.entries;

import io.vavr.collection.List;

/**
 * Base list-based <code>ExecutionContext</code> entity class - you need to extend it if you want to
 * create your list-based entity.
 */
public abstract class ExecutionContextListEntity<T> extends ExecutionContextEntity {

  protected final List<T> includedValues;
  protected final List<T> excludedValues;

  protected ExecutionContextListEntity(List<T> includedValues, List<T> excludedValues) {
    this.includedValues = includedValues;
    this.excludedValues = excludedValues;
  }

  public List<T> getIncludedValues() {
    return includedValues;
  }

  public List<T> getExcludedValues() {
    return excludedValues;
  }
}
