package org.jetbrains.bsp.bazel.commons;

import java.util.Optional;
import java.util.function.Supplier;

public abstract class Lazy<T> {
  private Optional<Optional<T>> value = Optional.empty();

  protected abstract Supplier<Optional<T>> calculateValue();

  public Optional<T> getValue() {
    if (!value.isPresent()) {
      value = Optional.of(calculateValue().get());
    }

    return value.get();
  }

  public void recalculateValue() {
    value = Optional.of(calculateValue().get());
  }
}
