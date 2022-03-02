package org.jetbrains.bsp.bazel.executioncontext.api.entries;

import java.util.List;

public abstract class ExecutionContextListEntry<T> {

  protected final List<T> includedValues;
  protected final List<T> excludedValues;

  protected ExecutionContextListEntry(List<T> includedValues, List<T> excludedValues) {
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
