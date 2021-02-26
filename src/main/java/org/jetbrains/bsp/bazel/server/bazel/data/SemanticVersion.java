package org.jetbrains.bsp.bazel.server.bazel.data;

public class SemanticVersion implements Comparable<SemanticVersion> {

  private final String version;

  public final String get() {
    return this.version;
  }

  public SemanticVersion(String version) {
    if (version == null) throw new IllegalArgumentException("Version can not be null");
    if (!version.matches("[0-9]+(\\.[0-9]+)*"))
      throw new IllegalArgumentException("Invalid version format");
    this.version = version;
  }

  @Override
  public int compareTo(SemanticVersion other) {
    if (other == null) return 1;
    String[] thisParts = this.get().split("\\.");
    String[] thatParts = other.get().split("\\.");
    int length = Math.max(thisParts.length, thatParts.length);
    for (int i = 0; i < length; i++) {
      int thisPart = i < thisParts.length ? Integer.parseInt(thisParts[i]) : 0;
      int thatPart = i < thatParts.length ? Integer.parseInt(thatParts[i]) : 0;
      if (thisPart < thatPart) return -1;
      if (thisPart > thatPart) return 1;
    }
    return 0;
  }

  @Override
  public int hashCode() {
    return version.hashCode();
  }

  @Override
  public boolean equals(Object that) {
    if (this == that) return true;
    if (that == null) return false;
    if (this.getClass() != that.getClass()) return false;
    return this.compareTo((SemanticVersion) that) == 0;
  }
}
