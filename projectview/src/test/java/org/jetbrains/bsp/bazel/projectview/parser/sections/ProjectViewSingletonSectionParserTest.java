package org.jetbrains.bsp.bazel.projectview.parser.sections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBazelPathSection;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewDebuggerAddressSection;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewJavaPathSection;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewSingletonSection;
import org.jetbrains.bsp.bazel.projectview.parser.splitter.ProjectViewRawSection;
import org.jetbrains.bsp.bazel.projectview.parser.splitter.ProjectViewRawSections;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class ProjectViewSingletonSectionParserTest<T extends ProjectViewSingletonSection> {

  private final ProjectViewSingletonSectionParser<T> parser;
  private final Function<String, T> sectionConstructor;
  private final String sectionName;

  public ProjectViewSingletonSectionParserTest(
      ProjectViewSingletonSectionParser<T> parser, Function<String, T> sectionConstructor) {
    this.parser = parser;
    this.sectionConstructor = sectionConstructor;

    this.sectionName = parser.sectionName;
  }

  @Parameters(name = "{index}: ProjectViewSingletonSectionParserTest for {0}")
  public static Collection<Object[]> data() {
    return List.of(
        new Object[][] {
          {
            new ProjectViewBazelPathSectionParser(),
            (Function<String, ProjectViewSingletonSection>) ProjectViewBazelPathSection::new
          },
          {
            new ProjectViewDebuggerAddressSectionParser(),
            (Function<String, ProjectViewSingletonSection>) ProjectViewDebuggerAddressSection::new
          },
          {
            new ProjectViewJavaPathSectionParser(),
            (Function<String, ProjectViewSingletonSection>) ProjectViewJavaPathSection::new
          }
        });
  }

  // T parse(rawSection)

  @Test
  public void shouldReturnFailureForWrongSectionName() {
    // given
    var rawSection = new ProjectViewRawSection("wrongsection", "value");

    // when
    var sectionTry = parser.parse(rawSection);

    // then
    assertTrue(sectionTry.isFailure());

    assertEquals(
        "Project view parsing failed! Expected '"
            + sectionName
            + "' section name, got 'wrongsection'!",
        sectionTry.getCause().getMessage());
  }

  @Test
  public void shouldReturnEmptyForEmptySectionBody() {
    // given
    var rawSection = new ProjectViewRawSection(sectionName, "");

    // when
    var sectionTry = parser.parse(rawSection);

    // then
    assertTrue(sectionTry.isSuccess());
    var section = sectionTry.get();

    assertFalse(section.isPresent());
  }

  @Test
  public void shouldReturnSectionWithTrimmedValue() {
    // given
    var rawSection = new ProjectViewRawSection(sectionName, "  value");

    // when
    var sectionTry = parser.parse(rawSection);

    // then
    assertTrue(sectionTry.isSuccess());
    var section = sectionTry.get();

    var expectedSection = sectionConstructor.apply("value");
    assertEquals(expectedSection, section.get());
  }

  @Test
  public void shouldReturnSectionWithTrimmedValueWithSpaces() {
    // given
    var rawSection = new ProjectViewRawSection(sectionName, "  value with space 123 \t\n");

    // when
    var sectionTry = parser.parse(rawSection);

    // then
    assertTrue(sectionTry.isSuccess());
    var section = sectionTry.get();

    var expectedSection = sectionConstructor.apply("value with space 123");
    assertEquals(expectedSection, section.get());
  }

  // T parse(rawSections)

  @Test
  public void shouldReturnLastSectionWithoutExplicitDefault() {
    // given
    var rawSection1 = new ProjectViewRawSection("another_section1", "value1");
    var rawSection2 = new ProjectViewRawSection(sectionName, "  value2\n");
    var rawSection3 = new ProjectViewRawSection("another_section2", "\tvalue3\n");
    var rawSection4 = new ProjectViewRawSection(sectionName, "    value4\n  ");
    var rawSection5 = new ProjectViewRawSection("another_section3", "\tvalue5\n");

    var rawSections =
        new ProjectViewRawSections(
            List.of(rawSection1, rawSection2, rawSection3, rawSection4, rawSection5));

    // when
    var section = parser.parse(rawSections);

    // then
    var expectedSection = sectionConstructor.apply("value4");
    assertEquals(expectedSection, section.get());
  }

  @Test
  public void shouldReturnEmptyIfSectionDoesntExist() {
    // given
    var rawSection1 = new ProjectViewRawSection("another_section1", "value1");
    var rawSection2 = new ProjectViewRawSection("another_section2", "  value2");
    var rawSection3 = new ProjectViewRawSection("another_section3", "\tvalue3\n");

    var rawSections = new ProjectViewRawSections(List.of(rawSection1, rawSection2, rawSection3));

    // when
    var section = parser.parse(rawSections);

    // then
    assertFalse(section.isPresent());
  }

  // T parseOrDefault(rawSections, defaultValue)

  @Test
  public void shouldReturnLastSection() {
    // given
    var rawSection1 = new ProjectViewRawSection("another_section1", "value1");
    var rawSection2 = new ProjectViewRawSection(sectionName, "  value2\n");
    var rawSection3 = new ProjectViewRawSection("another_section2", "\tvalue3\n");
    var rawSection4 = new ProjectViewRawSection(sectionName, "    value4\n  \t");
    var rawSection5 = new ProjectViewRawSection("another_section3", "\tvalue5\n");

    var rawSections =
        new ProjectViewRawSections(
            List.of(rawSection1, rawSection2, rawSection3, rawSection4, rawSection5));

    var defaultBazelPathSection = sectionConstructor.apply("default_value");

    // when
    var section = parser.parseOrDefault(rawSections, Optional.of(defaultBazelPathSection));

    // then
    var expectedSection = sectionConstructor.apply("value4");
    assertEquals(expectedSection, section.get());
  }

  @Test
  public void shouldReturnDefaultValueIfSectionDoesntExist() {
    // given
    var rawSection1 = new ProjectViewRawSection("another_section1", "value1");
    var rawSection2 = new ProjectViewRawSection("another_section2", "  value2");
    var rawSection3 = new ProjectViewRawSection("another_section3", "\tvalue3\n");

    var rawSections = new ProjectViewRawSections(List.of(rawSection1, rawSection2, rawSection3));

    var defaultBazelPathSection = sectionConstructor.apply("default_value");

    // when
    var section = parser.parseOrDefault(rawSections, Optional.of(defaultBazelPathSection));

    // then
    var expectedBazelPathSection = sectionConstructor.apply("default_value");
    assertEquals(expectedBazelPathSection, section.get());
  }

  @Test
  public void shouldReturnEmptyDefaultValueIfSectionDoesntExist() {
    // given
    var rawSection1 = new ProjectViewRawSection("another_section1", "value1");
    var rawSection2 = new ProjectViewRawSection("another_section2", "  value2");
    var rawSection3 = new ProjectViewRawSection("another_section3", "\tvalue3\n");

    var rawSections = new ProjectViewRawSections(List.of(rawSection1, rawSection2, rawSection3));

    // when
    var section = parser.parseOrDefault(rawSections, Optional.empty());

    // then
    assertFalse(section.isPresent());
  }
}
