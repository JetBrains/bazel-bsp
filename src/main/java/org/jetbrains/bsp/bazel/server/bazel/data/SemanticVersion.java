package org.jetbrains.bsp.bazel.server.bazel.data;

import java.util.Comparator;

public class SemanticVersion implements Comparable<SemanticVersion> {

  private static final String SEMANTIC_VERSION_REGEX = "^([0-9]+)\\.([0-9]+)\\.([0-9]+)";
  private static final int MAJOR_VERSION_LOCATION = 0;
  private static final int MINOR_VERSION_LOCATION = 1;
  private static final int PATCH_VERSION_LOCATION = 2;
  private static final Comparator<SemanticVersion> comparator =
      Comparator.nullsLast(
          Comparator.comparing(SemanticVersion::getMajorVersion)
              .thenComparing(SemanticVersion::getMinorVersion)
              .thenComparing(SemanticVersion::getPatchVersion));

  private final String version;
  private final int majorVersion;
  private final int minorVersion;
  private final int patchVersion;

  public SemanticVersion(String version) {
    if (version == null) {
      throw new IllegalArgumentException("Version can't be null");
    }
    if (!version.matches(SEMANTIC_VERSION_REGEX)) {
      throw new IllegalArgumentException("Invalid version format");
    }

    String[] parts = version.split("\\.");

    this.version = version;
    this.majorVersion = Integer.parseInt(parts[MAJOR_VERSION_LOCATION]);
    this.minorVersion = Integer.parseInt(parts[MINOR_VERSION_LOCATION]);
    this.patchVersion = Integer.parseInt(parts[PATCH_VERSION_LOCATION]);
  }

  public int getMajorVersion() {
    return majorVersion;
  }

  public int getMinorVersion() {
    return minorVersion;
  }

  public int getPatchVersion() {
    return patchVersion;
  }

  static SemanticVersion fromReleaseData(String version) {
    return new SemanticVersion(version.split(" ")[1]);
  }

  @Override
  public int compareTo(SemanticVersion other) {
    return comparator.compare(this, other);
  }

  @Override
  public int hashCode() {
    return version.hashCode();
  }

  @Override
  public boolean equals(Object that) {
    if (this == that) {
      return true;
    }
    if (that == null) {
      return false;
    }
    if (this.getClass() != that.getClass()) {
      return false;
    }
    return this.compareTo((SemanticVersion) that) == 0;
  }
}
