package org.jetbrains.bsp.bazel.projectview.parser.sections;

import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewSingletonSection;

public abstract class ProjectViewSingletonSectionParser<T extends ProjectViewSingletonSection> extends ProjectViewSectionParser<T> {

  protected ProjectViewSingletonSectionParser(String sectionName) {
    super(sectionName);
  }
}
