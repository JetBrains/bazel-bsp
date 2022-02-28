package org.jetbrains.bsp.bazel.executioncontext.api.entries.validators.predefined;

import io.vavr.control.Try;
import java.io.File;
import java.nio.file.Path;
import org.jetbrains.bsp.bazel.executioncontext.api.entries.validators.ProjectViewSectionVerboseValidatorException;
import org.jetbrains.bsp.bazel.executioncontext.api.entries.validators.ProjectViewSingletonSectionVerboseValidator;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewSingletonSection;

public abstract class ProjectViewSectionIsPathExecutableVerboseValidator<
        T extends ProjectViewSingletonSection<Path>>
    extends ProjectViewSingletonSectionVerboseValidator<Path, T> {

  public ProjectViewSectionIsPathExecutableVerboseValidator(String sectionName) {
    super(sectionName);
  }

  @Override
  public Try<T> getValueOrFailureWithMessage(T section) {
    var file = getFileUnderPath(section);

    if (!file.exists()) {
      var exceptionMessage = "File under '" + section.getValue() + "' does not exist.";
      return Try.failure(
          new ProjectViewSectionVerboseValidatorException(sectionName, exceptionMessage));
    }

    if (!file.canExecute()) {
      var exceptionMessage = "File under '" + section.getValue() + "' is not executable.";
      return Try.failure(
          new ProjectViewSectionVerboseValidatorException(sectionName, exceptionMessage));
    }

    return Try.success(section);
  }

  private File getFileUnderPath(T section) {
    return section.getValue().toFile();
  }
}
