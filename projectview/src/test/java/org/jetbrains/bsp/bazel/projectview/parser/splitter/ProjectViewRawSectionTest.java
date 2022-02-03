package org.jetbrains.bsp.bazel.projectview.parser.splitter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class ProjectViewRawSectionTest {

  @Test
  public void shouldReturnFalseForComparisonWithAnotherName() {
    // given
    var section = new ProjectViewRawSection("name", "body");

    // when
    var result = section.compareByName("anothername");

    // then
    assertFalse(result);
  }

  @Test
  public void shouldReturnTrueForComparisonWithTheSameName() {
    // given
    var section = new ProjectViewRawSection("name", "body");

    // when
    var result = section.compareByName("name");

    // then
    assertTrue(result);
  }
}
