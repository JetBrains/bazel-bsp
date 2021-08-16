package org.jetbrains.bsp.bazel.projectview.parser.splitter;

class ProjectViewSectionHeaderPosition {

  private final int startIndex;
  private final int endIndex;

  private final String header;

  ProjectViewSectionHeaderPosition(int startIndex, int endIndex, String header) {
    this.startIndex = startIndex;
    this.endIndex = endIndex;
    this.header = header;
  }

  int getStartIndex() {
    return startIndex;
  }

  int getEndIndex() {
    return endIndex;
  }

  String getHeader() {
    return header;
  }
}
