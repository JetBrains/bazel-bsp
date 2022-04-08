package org.jetbrains.bsp.bazel.installationcontext.entities.mappers;

import io.vavr.control.Option;
import io.vavr.control.Try;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.jetbrains.bsp.bazel.executioncontext.api.entries.mappers.ProjectViewToExecutionContextEntityMapper;
import org.jetbrains.bsp.bazel.executioncontext.api.entries.mappers.ProjectViewToExecutionContextEntityMapperException;
import org.jetbrains.bsp.bazel.installationcontext.entities.InstallationContextJavaPathEntity;
import org.jetbrains.bsp.bazel.projectview.model.ProjectView;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewJavaPathSection;

public class InstallationContextJavaPathEntityMapper
    implements ProjectViewToExecutionContextEntityMapper<InstallationContextJavaPathEntity> {

  private static final String NAME = "java path";

  private static final String JAVA_HOME_PROPERTY_KEY = "java.home";

  @Override
  public Try<InstallationContextJavaPathEntity> map(ProjectView projectView) {
    return projectView
        .getJavaPath()
        .map(this::map)
        .orElse(fromSystemProperty())
        .toTry(
            () ->
                new ProjectViewToExecutionContextEntityMapperException(
                    NAME, "System property '" + JAVA_HOME_PROPERTY_KEY + "' is not specified."));
  }

  private InstallationContextJavaPathEntity map(ProjectViewJavaPathSection javaPathSection) {
    return new InstallationContextJavaPathEntity(javaPathSection.getValue());
  }

  private Option<InstallationContextJavaPathEntity> fromSystemProperty() {
    return Option.of(System.getProperty(JAVA_HOME_PROPERTY_KEY))
        .map(Paths::get)
        .map(this::appendJavaBinary)
        .map(this::map);
  }

  private Path appendJavaBinary(Path javaHome) {
    return javaHome.resolve("bin/java");
  }

  private InstallationContextJavaPathEntity map(Path rawJavaPath) {
    return new InstallationContextJavaPathEntity(rawJavaPath);
  }
}
