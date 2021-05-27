package org.jetbrains.bsp.bazel.projectview.parser.splitter;

import java.util.Objects;

public class ProjectViewRawSection {

  private final String sectionHeader;
  private final String sectionBody;

  public ProjectViewRawSection(String sectionHeader, String sectionBody) {
    this.sectionHeader = sectionHeader;
    this.sectionBody = sectionBody;
  }

  public String getSectionHeader() {
    return sectionHeader;
  }

  public String getSectionBody() {
    return sectionBody;
  }

  @Override
  public String toString() {
    return "ProjectViewRawSection{"
        + "sectionHeader='"
        + sectionHeader
        + '\''
        + ", sectionBody='"
        + sectionBody
        + '\''
        + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ProjectViewRawSection that = (ProjectViewRawSection) o;
    return Objects.equals(sectionHeader, that.sectionHeader)
        && Objects.equals(sectionBody, that.sectionBody);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sectionHeader, sectionBody);
  }
}
