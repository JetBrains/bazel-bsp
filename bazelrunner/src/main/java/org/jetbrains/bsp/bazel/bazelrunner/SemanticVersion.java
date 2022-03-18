package org.jetbrains.bsp.bazel.bazelrunner;

import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SemanticVersion implements Comparable<SemanticVersion> {

  private static final String SEMANTIC_VERSION_REGEX =
      "^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$";
  private static final int MAJOR_VERSION_GROUP_ID = 1;
  private static final int MINOR_VERSION_GROUP_ID = 2;
  private static final int PATCH_VERSION_GROUP_ID = 3;
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

    Pattern pattern = Pattern.compile(SEMANTIC_VERSION_REGEX);
    Matcher matcher = pattern.matcher(version);

    if (!matcher.matches()) {
      throw new IllegalArgumentException("Invalid version format");
    }

    this.version = version;
    this.majorVersion = Integer.parseInt(matcher.group(MAJOR_VERSION_GROUP_ID));
    this.minorVersion = Integer.parseInt(matcher.group(MINOR_VERSION_GROUP_ID));
    this.patchVersion = Integer.parseInt(matcher.group(PATCH_VERSION_GROUP_ID));
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

  public static SemanticVersion fromReleaseData(String version) {
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
