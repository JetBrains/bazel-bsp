package org.jetbrains.bsp.bazel.executioncontext.api.entries.mappers;

public class ProjectViewToExecutionContextEntityMapperException extends Exception {

  public ProjectViewToExecutionContextEntityMapperException(String entityName, String message) {
    super("Mapping project view into '" + entityName + "' failed! " + message);
  }
}
