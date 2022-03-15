package org.jetbrains.bsp.bazel.projectview.parser.splitter;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class ProjectViewRawSectionTest {

  private final ProjectViewRawSection section;
  private final String nameToCompare;
  private final boolean expectedComparisonResult;

  public ProjectViewRawSectionTest(
      ProjectViewRawSection section, String nameToCompare, boolean expectedComparisonResult) {
    this.section = section;
    this.nameToCompare = nameToCompare;
    this.expectedComparisonResult = expectedComparisonResult;
  }

  @Parameters(name = "{index}: {0}.shouldCompareByName({1}) should equals {2}")
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          {
            // given
            new ProjectViewRawSection("name", "body"),
            "anothername",
            // then
            false
          },
          {
            // given
            new ProjectViewRawSection("name", "body"),
            "name",
            // then
            true
          }
        });
  }

  @Test
  public void shouldCompareByName() {
    // when
    var result = section.hasName(nameToCompare);

    // then
    assertEquals(expectedComparisonResult, result);
  }
}
