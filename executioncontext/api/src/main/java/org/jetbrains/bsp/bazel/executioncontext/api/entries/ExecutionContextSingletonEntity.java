package org.jetbrains.bsp.bazel.executioncontext.api.entries;

/**
 * Base single-value <code>ExecutionContext</code> entity class - you need to extend it if you want
 * to create your single-value entity.
 */
public abstract class ExecutionContextSingletonEntity<T> extends ExecutionContextEntity {

  private final T value;

  protected ExecutionContextSingletonEntity(T value) {
    this.value = value;
  }

  public T getValue() {
    return value;
  }
}
