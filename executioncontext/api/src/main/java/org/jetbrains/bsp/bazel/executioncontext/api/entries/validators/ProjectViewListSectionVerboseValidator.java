package org.jetbrains.bsp.bazel.executioncontext.api.entries.validators;

import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewListSection;

public abstract class ProjectViewListSectionVerboseValidator<V, T extends ProjectViewListSection<V>>
    extends ProjectViewSectionVerboseValidator<T> {

  protected ProjectViewListSectionVerboseValidator(String sectionName) {
    super(sectionName);
  }
}
