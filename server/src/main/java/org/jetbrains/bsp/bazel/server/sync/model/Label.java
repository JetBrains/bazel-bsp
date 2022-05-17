package org.jetbrains.bsp.bazel.server.sync.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

public class Label {
  private final String value;

  public Label(@JsonProperty("value") String value) {
    this.value = value;
  }

  public static Label from(String value) {
    return new Label(value);
  }

  public String getValue() {
    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Label that = (Label) o;
    return Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }

  @Override
  public String toString() {
    return value;
  }
}
