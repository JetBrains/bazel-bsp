package org.jetbrains.bsp.bazel.executioncontext.api.entries;

public abstract class ExecutionContextSingletonEntry<T> {

  private final T value;

  protected ExecutionContextSingletonEntry(T value) {
    this.value = value;
  }

  public T getValue() {
    return value;
  }
}
