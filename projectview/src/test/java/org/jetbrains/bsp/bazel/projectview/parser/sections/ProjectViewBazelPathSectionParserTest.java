package org.jetbrains.bsp.bazel.projectview.parser.sections;

import com.google.common.collect.ImmutableList;
import java.util.Optional;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBazelPathSection;
import org.jetbrains.bsp.bazel.projectview.parser.splitter.ProjectViewRawSection;
import org.jetbrains.bsp.bazel.projectview.parser.splitter.ProjectViewRawSections;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import org.junit.Before;
import org.junit.Test;

public class ProjectViewBazelPathSectionParserTest {

  private ProjectViewBazelPathSectionParser parser;

  @Before
  public void before() {
    // given
    this.parser = new ProjectViewBazelPathSectionParser();
  }

  // ProjectViewBazelPathSection parse(rawSection)

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowIllegalArgumentExceptionForWrongSectionName() {
    // given
    var rawSection = new ProjectViewRawSection("wrongsection", "value");

    // when
    parser.parse(rawSection);

    // then
    // throw an exception
  }

  @Test
  public void shouldReturnEmptyForEmptySectionBody() {
    // given
    var rawSection = new ProjectViewRawSection("bazel_path", "");

    // when
    var section = parser.parse(rawSection);

    // then
    assertFalse(section.isPresent());
  }

  @Test
  public void shouldReturnSectionWithTrimmedValue() {
    // given
    var rawSection = new ProjectViewRawSection("bazel_path", "  value");

    // when
    var section = parser.parse(rawSection);

    // then
    var expectedSection = new ProjectViewBazelPathSection("value");
    assertEquals(expectedSection, section.get());
  }

  @Test
  public void shouldReturnSectionWithTrimmedValueWithSpaces() {
    // given
    var rawSection = new ProjectViewRawSection("bazel_path", "  value with space 123 \t\n");

    // when
    var section = parser.parse(rawSection);

    // then
    var expectedSection = new ProjectViewBazelPathSection("value with space 123");
    assertEquals(expectedSection, section.get());
  }

  // ProjectViewBazelPathSection parse(rawSections)

  @Test
  public void shouldReturnLastSectionWithoutExplicitDefault() {
    // given
    var rawSection1 = new ProjectViewRawSection("anotersection1", "value1");
    var rawSection2 = new ProjectViewRawSection("bazel_path", "  path1/to/bin/bazel\n");
    var rawSection3 = new ProjectViewRawSection("anotersection2", "\tvalue2\n");
    var rawSection4 = new ProjectViewRawSection("bazel_path", "    path2/to/bin/bazel\n  ");
    var rawSection5 = new ProjectViewRawSection("anotersection3", "\tvalue3\n");

    var rawSections =
        new ProjectViewRawSections(
            ImmutableList.of(rawSection1, rawSection2, rawSection3, rawSection4, rawSection5));

    // when
    var section = parser.parse(rawSections);

    // then
    var expectedSection = new ProjectViewBazelPathSection("path2/to/bin/bazel");
    assertEquals(expectedSection, section.get());
  }

  @Test
  public void shouldReturnEmptyIfSectionDoesntExist() {
    // given
    var rawSection1 = new ProjectViewRawSection("anotersection1", "value1");
    var rawSection2 = new ProjectViewRawSection("anotersection2", "  value2");
    var rawSection3 = new ProjectViewRawSection("anotersection3", "\tvalue3\n");

    var rawSections =
        new ProjectViewRawSections(ImmutableList.of(rawSection1, rawSection2, rawSection3));

    // when
    var section = parser.parse(rawSections);

    // then
    assertFalse(section.isPresent());
  }

  // ProjectViewBazelPathSection parseOrDefault(rawSections, defaultValue)

  @Test
  public void shouldReturnLastSection() {
    // given
    var rawSection1 = new ProjectViewRawSection("anotersection1", "value1");
    var rawSection2 = new ProjectViewRawSection("bazel_path", "  path1/to/bin/bazel\n");
    var rawSection3 = new ProjectViewRawSection("anotersection2", "\tvalue2\n");
    var rawSection4 = new ProjectViewRawSection("bazel_path", "    path2/to/bin/bazel\n  \t");
    var rawSection5 = new ProjectViewRawSection("anotersection3", "\tvalue3\n");

    var rawSections =
        new ProjectViewRawSections(
            ImmutableList.of(rawSection1, rawSection2, rawSection3, rawSection4, rawSection5));

    var defaultBazelPathSection = new ProjectViewBazelPathSection("default_value");

    // when
    var section = parser.parseOrDefault(rawSections, Optional.of(defaultBazelPathSection));

    // then
    var expectedSection = new ProjectViewBazelPathSection("path2/to/bin/bazel");
    assertEquals(expectedSection, section.get());
  }

  @Test
  public void shouldReturnDefaultValueIfSectionDoesntExist() {
    // given
    var rawSection1 = new ProjectViewRawSection("anotersection1", "value1");
    var rawSection2 = new ProjectViewRawSection("anotersection2", "  value2");
    var rawSection3 = new ProjectViewRawSection("anotersection3", "\tvalue3\n");

    var rawSections =
        new ProjectViewRawSections(ImmutableList.of(rawSection1, rawSection2, rawSection3));

    var defaultBazelPathSection = new ProjectViewBazelPathSection("default_value");

    // when
    var section = parser.parseOrDefault(rawSections, Optional.of(defaultBazelPathSection));

    // then
    var expectedBazelPathSection = new ProjectViewBazelPathSection("default_value");
    assertEquals(expectedBazelPathSection, section.get());
  }

  @Test
  public void shouldReturnEmptyDefaultValueIfSectionDoesntExist() {
    // given
    var rawSection1 = new ProjectViewRawSection("anotersection1", "value1");
    var rawSection2 = new ProjectViewRawSection("anotersection2", "  value2");
    var rawSection3 = new ProjectViewRawSection("anotersection3", "\tvalue3\n");

    var rawSections =
        new ProjectViewRawSections(ImmutableList.of(rawSection1, rawSection2, rawSection3));

    // when
    var section = parser.parseOrDefault(rawSections, Optional.empty());

    // then
    assertFalse(section.isPresent());
  }
}
