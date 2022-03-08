package org.jetbrains.bsp.bazel.projectview.parser.sections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.net.HostAndPort;
import java.nio.file.Path;
import java.nio.file.Paths;
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
public class ProjectViewSingletonSectionParserTest<V, T extends ProjectViewSingletonSection<V>> {

  private final ProjectViewSingletonSectionParser<V, T> parser;

  private final Function<String, String> rawValueConstructor;

  private final Function<String, T> sectionConstructor;

  private final String sectionName;

  public ProjectViewSingletonSectionParserTest(
      ProjectViewSingletonSectionParser<V, T> parser,
      Function<String, String> rawValueConstructor,
      Function<V, T> sectionMapper,
      Function<String, V> elementMapper) {
    this.parser = parser;

    this.rawValueConstructor = rawValueConstructor;
    this.sectionConstructor =
        createSectionConstructor(rawValueConstructor, sectionMapper, elementMapper);

    this.sectionName = parser.sectionName;
  }

  private Function<String, T> createSectionConstructor(
      Function<String, String> rawValueConstructor,
      Function<V, T> sectionMapper,
      Function<String, V> elementMapper) {

    return (seed) -> sectionMapper.apply(elementMapper.apply(rawValueConstructor.apply(seed)));
  }

  @Parameters(name = "{index}: ProjectViewSingletonSectionParserTest for {0}")
  public static Collection<Object[]> data() {
    return List.of(
        new Object[][] {
          {
            new ProjectViewBazelPathSectionParser(),
            (Function<String, String>) (seed) -> "/path/to/bazel/" + seed,
            (Function<Path, ProjectViewBazelPathSection>) ProjectViewBazelPathSection::new,
            (Function<String, Path>) Paths::get
          },
          {
            new ProjectViewDebuggerAddressSectionParser(),
            (Function<String, String>) (seed) -> "host_" + seed + ":8080",
            (Function<HostAndPort, ProjectViewDebuggerAddressSection>)
                ProjectViewDebuggerAddressSection::new,
            (Function<String, HostAndPort>) HostAndPort::fromString
          },
          {
            new ProjectViewJavaPathSectionParser(),
            (Function<String, String>) (seed) -> "/path/to/java/" + seed,
            (Function<Path, ProjectViewJavaPathSection>) ProjectViewJavaPathSection::new,
            (Function<String, Path>) Paths::get
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

    assertTrue(section.isEmpty());
  }

  @Test
  public void shouldReturnSectionWithTrimmedValue() {
    // given
    var rawSection =
        new ProjectViewRawSection(sectionName, "  " + rawValueConstructor.apply("value") + "\t\n");

    // when
    var sectionTry = parser.parse(rawSection);

    // then
    assertTrue(sectionTry.isSuccess());
    var section = sectionTry.get();

    var expectedSection = sectionConstructor.apply("value");
    assertEquals(expectedSection, section.get());
  }

  // T parse(rawSections)

  @Test
  public void shouldReturnLastSectionWithoutExplicitDefault() {
    // given
    var rawSection1 = new ProjectViewRawSection("another_section1", "value1");
    var rawSection2 =
        new ProjectViewRawSection(sectionName, "  " + rawValueConstructor.apply("value2") + "\n");
    var rawSection3 = new ProjectViewRawSection("another_section2", "\tvalue3\n");
    var rawSection4 =
        new ProjectViewRawSection(
            sectionName, "    " + rawValueConstructor.apply("value4") + "\n  ");
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
    assertTrue(section.isEmpty());
  }

  // T parseOrDefault(rawSections, defaultValue)

  @Test
  public void shouldReturnLastSection() {
    // given
    var rawSection1 = new ProjectViewRawSection("another_section1", "value1");
    var rawSection2 =
        new ProjectViewRawSection(sectionName, "  " + rawValueConstructor.apply("value2") + "\n");
    var rawSection3 = new ProjectViewRawSection("another_section2", "\tvalue3\n");
    var rawSection4 =
        new ProjectViewRawSection(
            sectionName, "    " + rawValueConstructor.apply("value4") + "\n  \t");
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
    assertTrue(section.isEmpty());
  }
}
