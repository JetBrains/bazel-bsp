package org.jetbrains.bsp.bazel.executioncontext.api.entries.validators;

import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewSingletonSection;

public abstract class ProjectViewSingletonSectionVerboseValidator<
        V, T extends ProjectViewSingletonSection<V>>
    extends ProjectViewSectionVerboseValidator<T> {

  protected ProjectViewSingletonSectionVerboseValidator(String sectionName) {
    super(sectionName);
  }
}
