package org.jetbrains.bsp.bazel.executioncontext.api.entries.mappers;

/** <code>ProjectViewToExecutionContextEntityMapper</code> mapping failed? Throw it. */
public class ProjectViewToExecutionContextEntityMapperException extends Exception {

  public ProjectViewToExecutionContextEntityMapperException(String entityName, String message) {
    super("Mapping project view into '" + entityName + "' failed! " + message);
  }
}
