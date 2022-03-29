package org.jetbrains.bsp.bazel.projectview.parser.splitter;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProjectViewRawSectionTest {

  public static Stream<Arguments> data() {
    return Stream.of(
        Arguments.of(
            // given
            new ProjectViewRawSection("name", "body"),
            "anothername",
            // then
            false),
        Arguments.of(
            // given
            new ProjectViewRawSection("name", "body"),
            "name",
            // then
            true));
  }

  @ParameterizedTest(name = "{index}: {0}.shouldCompareByName({1}) should equals {2}")
  @MethodSource("data")
  public void shouldCompareByName(ProjectViewRawSection section, String nameToCompare, boolean expectedComparisonResult) {
    // when
    var result = section.hasName(nameToCompare);

    // then
    assertEquals(expectedComparisonResult, result);
  }
}
