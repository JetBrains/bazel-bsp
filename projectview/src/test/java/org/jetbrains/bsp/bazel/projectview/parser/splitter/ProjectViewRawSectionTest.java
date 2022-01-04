package org.jetbrains.bsp.bazel.projectview.parser.splitter;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ProjectViewRawSectionTest {

  @Test
  public void shouldReturnFalseForComparisonWithAnotherName() {
    // given
    ProjectViewRawSection section = new ProjectViewRawSection("name", "body");

    // when
    boolean result = section.compareByName("anothername");

    // then
    assertFalse(result);
  }

  @Test
  public void shouldReturnTrueForComparisonWithTheSameName() {

    // given
    ProjectViewRawSection section = new ProjectViewRawSection("name", "body");

    // when
    boolean result = section.compareByName("name");

    // then
    assertTrue(result);
  }
}
