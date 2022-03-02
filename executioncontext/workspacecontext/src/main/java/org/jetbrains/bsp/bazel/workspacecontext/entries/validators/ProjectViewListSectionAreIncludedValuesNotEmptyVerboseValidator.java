package org.jetbrains.bsp.bazel.workspacecontext.entries.validators;

import io.vavr.control.Try;
import org.jetbrains.bsp.bazel.executioncontext.api.entries.validators.ProjectViewListSectionVerboseValidator;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewListSection;

public class ProjectViewListSectionAreIncludedValuesNotEmptyVerboseValidator<
        V, T extends ProjectViewListSection<V>>
    extends ProjectViewListSectionVerboseValidator<V, T> {

  protected ProjectViewListSectionAreIncludedValuesNotEmptyVerboseValidator(String sectionName) {
    super(sectionName);
  }

  @Override
  public Try<T> getValueOrFailureWithMessage(T section) {
    return null;
  }
}
