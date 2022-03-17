package org.jetbrains.bsp.bazel.executioncontext.api.entries;

import java.util.Objects;

/**
 * Base single-value <code>ExecutionContext</code> entity class - you need to extend it if you want
 * to create your single-value entity.
 */
public abstract class ExecutionContextSingletonEntity<T> extends ExecutionContextEntity {

  protected final T value;

  protected ExecutionContextSingletonEntity(T value) {
    this.value = value;
  }

  public T getValue() {
    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ExecutionContextSingletonEntity<?> that = (ExecutionContextSingletonEntity<?>) o;
    return value.equals(that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }
}
