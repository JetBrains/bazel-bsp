package org.jetbrains.bsp.bazel.installationcontext.entities.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import io.vavr.control.Option;
import java.nio.file.Paths;
import org.jetbrains.bsp.bazel.executioncontext.api.entries.mappers.ProjectViewToExecutionContextEntityMapperException;
import org.jetbrains.bsp.bazel.installationcontext.entities.InstallationContextJavaPathEntity;
import org.jetbrains.bsp.bazel.projectview.model.ProjectView;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewJavaPathSection;
import org.junit.Before;
import org.junit.Test;

public class InstallationContextJavaPathEntityMapperTest {

  private InstallationContextJavaPathEntityMapper mapper;

  @Before
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
    assertThat(javaPathTry.isSuccess()).isTrue();
    var javaPath = javaPathTry.get();

    var expectedJavaPath = new InstallationContextJavaPathEntity(Paths.get("/path/to/java"));
    assertThat(javaPath).isEqualTo(expectedJavaPath);
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
    assertThat(javaPathTry.isSuccess()).isTrue();
    var javaPath = javaPathTry.get();

    var expectedJavaPath = new InstallationContextJavaPathEntity(Paths.get("/path/to/java"));
    assertThat(javaPath).isEqualTo(expectedJavaPath);
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
    assertThat(javaPathTry.isFailure()).isTrue();
    assertThat(javaPathTry.getCause().getClass())
        .isEqualTo(ProjectViewToExecutionContextEntityMapperException.class);
    assertThat(javaPathTry.getCause().getMessage())
        .isEqualTo(
            "Mapping project view into 'java path' failed! System property 'java.home' is not"
                + " specified.");
  }
}
