package org.jetbrains.bsp.bazel.installationcontext.entities.mappers;

import io.vavr.control.Option;
import java.nio.file.Paths;
import org.jetbrains.bsp.bazel.executioncontext.api.entries.mappers.ProjectViewToExecutionContextEntityMapperException;
import org.jetbrains.bsp.bazel.installationcontext.entities.InstallationContextJavaPathEntity;
import org.jetbrains.bsp.bazel.projectview.model.ProjectView;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewJavaPathSection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InstallationContextJavaPathEntityMapperTest {

  private InstallationContextJavaPathEntityMapper mapper;

  @BeforeEach
  public void beforeEach() {
    // given
    this.mapper = new InstallationContextJavaPathEntityMapper();
  }

  @Test
  public void shouldReturnSuccessWithJavaPathFromProjectViewIfJavaPathIsSpecifiedInProjectView() {
    // given
    var projectView =
        ProjectView.builder()
            .javaPath(Option.of(new ProjectViewJavaPathSection(Paths.get("/path/to/java"))))
            .build()
            .get();

    // when
    var javaPathTry = mapper.map(projectView);

    // then
    assertTrue(javaPathTry.isSuccess());
    var javaPath = javaPathTry.get();

    var expectedJavaPath = new InstallationContextJavaPathEntity(Paths.get("/path/to/java"));
    assertEquals(expectedJavaPath, javaPath);
  }

  @Test
  public void
      shouldReturnSuccessWithJavaPathFromSystemPropertyIfJavaPathIsNotSpecifiedInProjectView() {
    // given
    var projectView = ProjectView.builder().javaPath(Option.none()).build().get();

    System.setProperty("java.home", "/path/to/java");

    // when
    var javaPathTry = mapper.map(projectView);

    // then
    assertTrue(javaPathTry.isSuccess());
    var javaPath = javaPathTry.get();

    var expectedJavaPath = new InstallationContextJavaPathEntity(Paths.get("/path/to/java"));
    assertEquals(expectedJavaPath, javaPath);
  }

  @Test
  public void
      shouldReturnFailureIfJavaPathIsNotSpecifiedInProjectViewAndItIsImpossibleToObtainItFromSystemPropererties() {
    // given
    var projectView = ProjectView.builder().javaPath(Option.none()).build().get();

    System.clearProperty("java.home");

    // when
    var javaPathTry = mapper.map(projectView);

    // then
    assertTrue(javaPathTry.isFailure());
    assertEquals(
        ProjectViewToExecutionContextEntityMapperException.class,
        javaPathTry.getCause().getClass());

    var expectedMessage =
        "Mapping project view into 'java path' failed! System property 'java.home' is not"
            + " specified.";
    assertEquals(expectedMessage, javaPathTry.getCause().getMessage());
  }
}
