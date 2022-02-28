package org.jetbrains.bsp.bazel.executioncontext.api.entries.validators.predefined;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewSection;
import org.junit.Before;
import org.junit.Test;

public class ProjectViewSectionDummyVerboseValidatorTest {

  private final ProjectViewSection dummyProjectViewSection =
      new ProjectViewSection("dummy_name") {};

  private ProjectViewSectionDummyVerboseValidator<ProjectViewSection> validator;

  @Before
  public void beforeEach() {
    // given
    this.validator = new ProjectViewSectionDummyVerboseValidator<>("dummy_name") {};
  }

  @Test
  public void shouldReturnSuccessWithElement() {
    // when
    var result = validator.getValueOrFailureWithMessage(dummyProjectViewSection);

    // then
    assertTrue(result.isSuccess());
    assertEquals(dummyProjectViewSection, result.get());
  }
}
