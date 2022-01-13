package org.jetbrains.bsp.bazel.projectview.parser.sections;

import com.google.common.collect.ImmutableList;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBazelPathSection;
import org.jetbrains.bsp.bazel.projectview.parser.splitter.ProjectViewRawSection;
import org.jetbrains.bsp.bazel.projectview.parser.splitter.ProjectViewRawSections;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

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
    ProjectViewRawSection rawSection = new ProjectViewRawSection("wrongsection", "value");

    // when
    parser.parse(rawSection);

    // then
    // throw an exception
  }

  @Test
  public void shouldReturnEmptyForEmptySectionBody() {
    // given
    ProjectViewRawSection rawSection = new ProjectViewRawSection("bazel_path", "");

    // when
    Optional<ProjectViewBazelPathSection> section = parser.parse(rawSection);

    // then
    assertFalse(section.isPresent());
  }

  @Test
  public void shouldReturnSectionWithTrimmedValue() {
    // given
    ProjectViewRawSection rawSection = new ProjectViewRawSection("bazel_path", "  value");

    // when
    Optional<ProjectViewBazelPathSection> section = parser.parse(rawSection);

    // then
    ProjectViewBazelPathSection expectedSection = new ProjectViewBazelPathSection("value");
    assertEquals(expectedSection, section.get());
  }

  @Test
  public void shouldReturnSectionWithTrimmedValueWithSpaces() {
    // given
    ProjectViewRawSection rawSection =
        new ProjectViewRawSection("bazel_path", "  value with space 123 \t\n");

    // when
    Optional<ProjectViewBazelPathSection> section = parser.parse(rawSection);

    // then
    ProjectViewBazelPathSection expectedSection =
        new ProjectViewBazelPathSection("value with space 123");
    assertEquals(expectedSection, section.get());
  }

  // ProjectViewBazelPathSection parse(rawSections)

  @Test
  public void shouldReturnLastSectionWithoutExplicitDefault() {
    // given
    ProjectViewRawSection rawSection1 = new ProjectViewRawSection("anotersection1", "value1");
    ProjectViewRawSection rawSection2 =
        new ProjectViewRawSection("bazel_path", "  path1/to/bin/bazel\n");
    ProjectViewRawSection rawSection3 = new ProjectViewRawSection("anotersection2", "\tvalue2\n");
    ProjectViewRawSection rawSection4 =
        new ProjectViewRawSection("bazel_path", "    path2/to/bin/bazel\n  ");
    ProjectViewRawSection rawSection5 = new ProjectViewRawSection("anotersection3", "\tvalue3\n");

    ProjectViewRawSections rawSections =
        new ProjectViewRawSections(
            ImmutableList.of(rawSection1, rawSection2, rawSection3, rawSection4, rawSection5));

    // when
    Optional<ProjectViewBazelPathSection> section = parser.parse(rawSections);

    // then
    ProjectViewBazelPathSection expectedSection =
        new ProjectViewBazelPathSection("path2/to/bin/bazel");
    assertEquals(expectedSection, section.get());
  }

  @Test
  public void shouldReturnEmptyIfSectionDoesntExist() {
    // given
    ProjectViewRawSection rawSection1 = new ProjectViewRawSection("anotersection1", "value1");
    ProjectViewRawSection rawSection2 = new ProjectViewRawSection("anotersection2", "  value2");
    ProjectViewRawSection rawSection3 = new ProjectViewRawSection("anotersection3", "\tvalue3\n");

    ProjectViewRawSections rawSections =
        new ProjectViewRawSections(ImmutableList.of(rawSection1, rawSection2, rawSection3));

    // when
    Optional<ProjectViewBazelPathSection> section = parser.parse(rawSections);

    // then
    assertFalse(section.isPresent());
  }

  // ProjectViewBazelPathSection parseOrDefault(rawSections, defaultValue)

  @Test
  public void shouldReturnLastSection() {
    // given
    ProjectViewRawSection rawSection1 = new ProjectViewRawSection("anotersection1", "value1");
    ProjectViewRawSection rawSection2 =
        new ProjectViewRawSection("bazel_path", "  path1/to/bin/bazel\n");
    ProjectViewRawSection rawSection3 = new ProjectViewRawSection("anotersection2", "\tvalue2\n");
    ProjectViewRawSection rawSection4 =
        new ProjectViewRawSection("bazel_path", "    path2/to/bin/bazel\n  \t");
    ProjectViewRawSection rawSection5 = new ProjectViewRawSection("anotersection3", "\tvalue3\n");

    ProjectViewRawSections rawSections =
        new ProjectViewRawSections(
            ImmutableList.of(rawSection1, rawSection2, rawSection3, rawSection4, rawSection5));

    ProjectViewBazelPathSection defaultBazelPathSection =
        new ProjectViewBazelPathSection("default_value");

    // when
    Optional<ProjectViewBazelPathSection> section =
        parser.parseOrDefault(rawSections, Optional.of(defaultBazelPathSection));

    // then
    ProjectViewBazelPathSection expectedSection =
        new ProjectViewBazelPathSection("path2/to/bin/bazel");
    assertEquals(expectedSection, section.get());
  }

  @Test
  public void shouldReturnDefaultValueIfSectionDoesntExist() {
    // given
    ProjectViewRawSection rawSection1 = new ProjectViewRawSection("anotersection1", "value1");
    ProjectViewRawSection rawSection2 = new ProjectViewRawSection("anotersection2", "  value2");
    ProjectViewRawSection rawSection3 = new ProjectViewRawSection("anotersection3", "\tvalue3\n");

    ProjectViewRawSections rawSections =
        new ProjectViewRawSections(ImmutableList.of(rawSection1, rawSection2, rawSection3));

    ProjectViewBazelPathSection defaultBazelPathSection =
        new ProjectViewBazelPathSection("default_value");

    // when
    Optional<ProjectViewBazelPathSection> section =
        parser.parseOrDefault(rawSections, Optional.of(defaultBazelPathSection));

    // then
    ProjectViewBazelPathSection expectedBazelPathSection =
        new ProjectViewBazelPathSection("default_value");
    assertEquals(expectedBazelPathSection, section.get());
  }

  @Test
  public void shouldReturnEmptyDefaultValueIfSectionDoesntExist() {
    // given
    ProjectViewRawSection rawSection1 = new ProjectViewRawSection("anotersection1", "value1");
    ProjectViewRawSection rawSection2 = new ProjectViewRawSection("anotersection2", "  value2");
    ProjectViewRawSection rawSection3 = new ProjectViewRawSection("anotersection3", "\tvalue3\n");

    ProjectViewRawSections rawSections =
        new ProjectViewRawSections(ImmutableList.of(rawSection1, rawSection2, rawSection3));

    // when
    Optional<ProjectViewBazelPathSection> section =
        parser.parseOrDefault(rawSections, Optional.empty());

    // then
    assertFalse(section.isPresent());
  }
}
