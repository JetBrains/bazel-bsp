package org.jetbrains.bsp.bazel.executioncontext.api.entries.validators.predefined;

import io.vavr.control.Try;
import org.jetbrains.bsp.bazel.executioncontext.api.entries.validators.ProjectViewSectionVerboseValidator;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewSection;

public class ProjectViewSectionDummyVerboseValidator<T extends ProjectViewSection>
    implements ProjectViewSectionVerboseValidator<T> {

  @Override
  public Try<T> getValueOrFailureWithMessage(T section) {
    return Try.success(section);
  }
}
