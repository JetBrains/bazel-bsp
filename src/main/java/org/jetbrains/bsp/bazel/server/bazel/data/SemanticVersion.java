package org.jetbrains.bsp.bazel.server.bazel.data;

public class SemanticVersion implements Comparable<SemanticVersion> {

  private static final String SEMANTIC_VERSION_REGEX = "[0-9]+(\\.[0-9]+)*";
  private static final int MAJOR_VERSION_LOCATION = 0;
  private static final int MINOR_VERSION_LOCATION = 1;
  private static final int PATCH_VERSION_LOCATION = 1;

  private final String version;
  private final String[] versionParts;

  public SemanticVersion(String version) {
    if (version == null) {
      throw new IllegalArgumentException("Version can not be null");
    }
    if (!version.matches(SEMANTIC_VERSION_REGEX)) {
      throw new IllegalArgumentException("Invalid version format");
    }

    String[] parts = version.split("\\.");
    if (parts.length != 3) {
      throw new IllegalArgumentException("Invalid number of version parts");
    }

    this.version = version;
    this.versionParts = parts;
  }

  public final int getMajorVersion() {
    return Integer.parseInt(versionParts[MAJOR_VERSION_LOCATION]);
  }

  public final int getMinorVersion() {
    return Integer.parseInt(versionParts[MINOR_VERSION_LOCATION]);
  }

  public final int getPatchVersion() {
    return Integer.parseInt(versionParts[PATCH_VERSION_LOCATION]);
  }

  static SemanticVersion fromReleaseData(String version) {
    return new SemanticVersion(version.split(" ")[1]);
  }

  @Override
  public int compareTo(SemanticVersion other) {
    if (other == null) return 1;
    int thisMajorVersion = this.getMajorVersion();
    int thatMajorVersion = other.getMajorVersion();
    if (thisMajorVersion < thatMajorVersion) return -1;
    if (thisMajorVersion > thatMajorVersion) return 1;

    int thisMinorVersion = this.getMinorVersion();
    int thatMinorVersion = other.getMinorVersion();
    if (thisMinorVersion < thatMinorVersion) return -1;
    if (thisMinorVersion > thatMinorVersion) return 1;

    int thisPatchVersion = this.getPatchVersion();
    int thatPatchVersion = other.getPatchVersion();
    if (thisPatchVersion < thatPatchVersion) return -1;
    if (thisPatchVersion > thatPatchVersion) return 1;

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
