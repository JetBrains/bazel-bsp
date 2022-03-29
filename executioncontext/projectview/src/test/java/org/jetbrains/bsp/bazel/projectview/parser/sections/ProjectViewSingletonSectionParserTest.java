package org.jetbrains.bsp.bazel.projectview.parser.sections;

import com.google.common.net.HostAndPort;
import io.vavr.collection.List;
import io.vavr.control.Option;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;
import java.util.stream.Stream;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBazelPathSection;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewDebuggerAddressSection;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewJavaPathSection;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewSingletonSection;
import org.jetbrains.bsp.bazel.projectview.parser.splitter.ProjectViewRawSection;
import org.jetbrains.bsp.bazel.projectview.parser.splitter.ProjectViewRawSections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProjectViewSingletonSectionParserTest<V, T extends ProjectViewSingletonSection<V>> {

  private Function<String, T> createSectionConstructor(
      Function<String, String> rawValueConstructor,
      Function<V, T> sectionMapper,
      Function<String, V> elementMapper) {

    return (seed) -> sectionMapper.apply(elementMapper.apply(rawValueConstructor.apply(seed)));
  }

  public static Stream<Arguments> data() {
    return Stream.of(
        Arguments.of(
            new ProjectViewBazelPathSectionParser(),
            (Function<String, String>) (seed) -> "/path/to/bazel/" + seed,
            (Function<Path, ProjectViewBazelPathSection>) ProjectViewBazelPathSection::new,
            (Function<String, Path>) Paths::get),
        Arguments.of(
            new ProjectViewDebuggerAddressSectionParser(),
            (Function<String, String>) (seed) -> "host_" + seed + ":8080",
            (Function<HostAndPort, ProjectViewDebuggerAddressSection>)
                ProjectViewDebuggerAddressSection::new,
            (Function<String, HostAndPort>) HostAndPort::fromString),
        Arguments.of(
            new ProjectViewJavaPathSectionParser(),
            (Function<String, String>) (seed) -> "/path/to/java/" + seed,
            (Function<Path, ProjectViewJavaPathSection>) ProjectViewJavaPathSection::new,
            (Function<String, Path>) Paths::get));
  }

  // T parse(rawSection)

  @MethodSource("data")
  @ParameterizedTest
  public void shouldReturnFailureForWrongSectionName(
      ProjectViewSingletonSectionParser<V, T> parser,
      Function<String, String> rawValueConstructor,
      Function<V, T> sectionMapper,
      Function<String, V> elementMapper) {
    var sectionConstructor =
            createSectionConstructor(rawValueConstructor, sectionMapper, elementMapper);

    var sectionName = parser.sectionName;

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

  @MethodSource("data")
  @ParameterizedTest
  public void shouldReturnEmptyForEmptySectionBody(
      ProjectViewSingletonSectionParser<V, T> parser,
      Function<String, String> rawValueConstructor,
      Function<V, T> sectionMapper,
      Function<String, V> elementMapper) {
    var sectionConstructor =
            createSectionConstructor(rawValueConstructor, sectionMapper, elementMapper);

    var sectionName = parser.sectionName;

    // given
    var rawSection = new ProjectViewRawSection(sectionName, "");

    // when
    var sectionTry = parser.parse(rawSection);

    // then
    assertTrue(sectionTry.isSuccess());
    var section = sectionTry.get();

    assertTrue(section.isEmpty());
  }

  @MethodSource("data")
  @ParameterizedTest
  public void shouldReturnSectionWithTrimmedValue(
      ProjectViewSingletonSectionParser<V, T> parser,
      Function<String, String> rawValueConstructor,
      Function<V, T> sectionMapper,
      Function<String, V> elementMapper) {
    var sectionConstructor =
            createSectionConstructor(rawValueConstructor, sectionMapper, elementMapper);

    var sectionName = parser.sectionName;

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

  @MethodSource("data")
  @ParameterizedTest
  public void shouldReturnLastSectionWithoutExplicitDefault(
      ProjectViewSingletonSectionParser<V, T> parser,
      Function<String, String> rawValueConstructor,
      Function<V, T> sectionMapper,
      Function<String, V> elementMapper) {
    var sectionConstructor =
            createSectionConstructor(rawValueConstructor, sectionMapper, elementMapper);

    var sectionName = parser.sectionName;

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

  @MethodSource("data")
  @ParameterizedTest
  public void shouldReturnEmptyIfSectionDoesntExist(
      ProjectViewSingletonSectionParser<V, T> parser,
      Function<String, String> rawValueConstructor,
      Function<V, T> sectionMapper,
      Function<String, V> elementMapper) {
    var sectionConstructor =
            createSectionConstructor(rawValueConstructor, sectionMapper, elementMapper);

    var sectionName = parser.sectionName;

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

  @MethodSource("data")
  @ParameterizedTest
  public void shouldReturnLastSection(
      ProjectViewSingletonSectionParser<V, T> parser,
      Function<String, String> rawValueConstructor,
      Function<V, T> sectionMapper,
      Function<String, V> elementMapper) {
    var sectionConstructor =
            createSectionConstructor(rawValueConstructor, sectionMapper, elementMapper);

    var sectionName = parser.sectionName;

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
    var section = parser.parseOrDefault(rawSections, Option.of(defaultBazelPathSection));

    // then
    var expectedSection = sectionConstructor.apply("value4");
    assertEquals(expectedSection, section.get());
  }

  @MethodSource("data")
  @ParameterizedTest
  public void shouldReturnDefaultValueIfSectionDoesntExist(
      ProjectViewSingletonSectionParser<V, T> parser,
      Function<String, String> rawValueConstructor,
      Function<V, T> sectionMapper,
      Function<String, V> elementMapper) {
    var sectionConstructor =
            createSectionConstructor(rawValueConstructor, sectionMapper, elementMapper);

    var sectionName = parser.sectionName;

    // given
    var rawSection1 = new ProjectViewRawSection("another_section1", "value1");
    var rawSection2 = new ProjectViewRawSection("another_section2", "  value2");
    var rawSection3 = new ProjectViewRawSection("another_section3", "\tvalue3\n");

    var rawSections = new ProjectViewRawSections(List.of(rawSection1, rawSection2, rawSection3));

    var defaultBazelPathSection = sectionConstructor.apply("default_value");

    // when
    var section = parser.parseOrDefault(rawSections, Option.of(defaultBazelPathSection));

    // then
    var expectedBazelPathSection = sectionConstructor.apply("default_value");
    assertEquals(expectedBazelPathSection, section.get());
  }

  @MethodSource("data")
  @ParameterizedTest
  public void shouldReturnEmptyDefaultValueIfSectionDoesntExist(
      ProjectViewSingletonSectionParser<V, T> parser,
      Function<String, String> rawValueConstructor,
      Function<V, T> sectionMapper,
      Function<String, V> elementMapper) {
    var sectionConstructor =
            createSectionConstructor(rawValueConstructor, sectionMapper, elementMapper);

    var sectionName = parser.sectionName;

    // given
    var rawSection1 = new ProjectViewRawSection("another_section1", "value1");
    var rawSection2 = new ProjectViewRawSection("another_section2", "  value2");
    var rawSection3 = new ProjectViewRawSection("another_section3", "\tvalue3\n");

    var rawSections = new ProjectViewRawSections(List.of(rawSection1, rawSection2, rawSection3));

    // when
    var section = parser.parseOrDefault(rawSections, Option.none());

    // then
    assertTrue(section.isEmpty());
  }
}
