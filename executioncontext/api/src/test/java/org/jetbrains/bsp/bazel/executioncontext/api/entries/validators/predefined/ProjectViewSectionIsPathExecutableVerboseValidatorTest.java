package org.jetbrains.bsp.bazel.executioncontext.api.entries.validators.predefined;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.jetbrains.bsp.bazel.executioncontext.api.entries.validators.ProjectViewSectionVerboseValidatorException;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewSingletonSection;
import org.junit.Before;
import org.junit.Test;

public class ProjectViewSectionIsPathExecutableVerboseValidatorTest {

  private ProjectViewSectionIsPathExecutableVerboseValidator<ProjectViewSingletonSection<Path>>
      validator;

  @Before
  public void beforeEach() {
    // given
    this.validator = new ProjectViewSectionIsPathExecutableVerboseValidator<>("section_name") {};
  }

  @Test
  public void shouldReturnFailureIfFileDoesntExist() {
    // given
    var notExistingFile = Paths.get("/does/not/exist");
    var section = new ProjectViewSingletonSection<>("section_name", notExistingFile) {};

    // when
    var result = validator.getValueOrFailureWithMessage(section);

    // then
    assertTrue(result.isFailure());
    assertEquals(ProjectViewSectionVerboseValidatorException.class, result.getCause().getClass());
    assertEquals(
        "'section_name' section validation failed! File under '/does/not/exist' does not exist.",
        result.getCause().getMessage());
  }

  @Test
  public void shouldReturnFailureIfFileIsntExecutable() throws IOException {
    // given
    var notExecutableFile = File.createTempFile("not", "executable");
    notExecutableFile.deleteOnExit();

    var section = new ProjectViewSingletonSection<>("section_name", notExecutableFile.toPath()) {};

    // when
    var result = validator.getValueOrFailureWithMessage(section);

    // then
    assertTrue(result.isFailure());
    assertEquals(ProjectViewSectionVerboseValidatorException.class, result.getCause().getClass());
    assertEquals(
        "'section_name' section validation failed! File under '"
            + notExecutableFile.getPath()
            + "' is not executable.",
        result.getCause().getMessage());
  }

  @Test
  public void shouldReturnSuccessIfFileIsExecutable() throws IOException {
    // given
    var executableFile = File.createTempFile("not", "executable");
    executableFile.setExecutable(true);
    executableFile.deleteOnExit();

    var section = new ProjectViewSingletonSection<>("section_name", executableFile.toPath()) {};

    // when
    var result = validator.getValueOrFailureWithMessage(section);

    // then
    assertTrue(result.isSuccess());
    assertEquals(section, result.get());
  }
}
