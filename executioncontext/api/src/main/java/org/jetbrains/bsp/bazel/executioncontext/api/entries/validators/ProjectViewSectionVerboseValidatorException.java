package org.jetbrains.bsp.bazel.executioncontext.api.entries.validators;

public class ProjectViewSectionVerboseValidatorException extends IllegalArgumentException {

  public ProjectViewSectionVerboseValidatorException(String sectionName, String message) {
    super("'" + sectionName + "' section validation failed! " + message);
  }
}
