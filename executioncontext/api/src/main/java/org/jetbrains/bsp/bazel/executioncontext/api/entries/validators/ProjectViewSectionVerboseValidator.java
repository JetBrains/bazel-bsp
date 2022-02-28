package org.jetbrains.bsp.bazel.executioncontext.api.entries.validators;

import io.vavr.control.Try;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewSection;

public abstract class ProjectViewSectionVerboseValidator<T extends ProjectViewSection> {

  protected String sectionName;

  protected ProjectViewSectionVerboseValidator(String sectionName) {
    this.sectionName = sectionName;
  }

  public abstract Try<T> getValueOrFailureWithMessage(T section);
}
