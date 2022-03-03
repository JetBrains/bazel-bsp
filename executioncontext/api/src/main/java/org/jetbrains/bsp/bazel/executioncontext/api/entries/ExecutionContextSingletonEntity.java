package org.jetbrains.bsp.bazel.executioncontext.api.entries;

public abstract class ExecutionContextSingletonEntity<T> extends ExecutionContextEntity {

  private final T value;

  protected ExecutionContextSingletonEntity(T value) {
    this.value = value;
  }

  public T getValue() {
    return value;
  }
}
