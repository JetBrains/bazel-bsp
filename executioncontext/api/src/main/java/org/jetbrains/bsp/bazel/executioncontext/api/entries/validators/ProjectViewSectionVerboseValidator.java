package org.jetbrains.bsp.bazel.executioncontext.api.entries.validators;

import io.vavr.control.Try;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewSection;

public interface ProjectViewSectionVerboseValidator<T extends ProjectViewSection> {

  Try<T> getValueOrFailureWithMessage(T section);
}
