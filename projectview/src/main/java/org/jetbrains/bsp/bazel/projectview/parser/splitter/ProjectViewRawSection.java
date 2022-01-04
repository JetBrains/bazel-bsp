package org.jetbrains.bsp.bazel.projectview.parser.splitter;

import java.util.Objects;

public class ProjectViewRawSection {

  private final String sectionName;
  private final String sectionBody;

  public ProjectViewRawSection(String sectionName, String sectionBody) {
    this.sectionName = sectionName;
    this.sectionBody = sectionBody;
  }

  public boolean compareByName(String thatSectionName) {
    return sectionName.equals(thatSectionName);
  }

  public String getSectionName() {
    return sectionName;
  }

  public String getSectionBody() {
    return sectionBody;
  }

  @Override
  public String toString() {
    return "ProjectViewRawSection{" +
        "sectionName='" + sectionName + '\'' +
        ", sectionBody='" + sectionBody + '\'' +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ProjectViewRawSection)) return false;
    ProjectViewRawSection that = (ProjectViewRawSection) o;
    return sectionName.equals(that.sectionName) && sectionBody.equals(that.sectionBody);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sectionName, sectionBody);
  }
}
