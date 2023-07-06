package org.jetbrains.bsp.bazel.projectview.parser.sections;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBazelPathSection;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBuildManualTargetsSection;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewSingletonSection;
import org.jetbrains.bsp.bazel.projectview.parser.splitter.ProjectViewRawSection;
import org.jetbrains.bsp.bazel.projectview.parser.splitter.ProjectViewRawSections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

// TODO to kotlin
public class ProjectViewSingletonSectionParserTest<V, T extends ProjectViewSingletonSection<V>> {

  public static Stream<Arguments> data() {
    return Stream.of(
        bazelPathSectionArguments(),
        buildManualTargetsSectionArguments());
  }

  private static Arguments bazelPathSectionArguments() {
    var parser = ProjectViewBazelPathSectionParser.INSTANCE;
    var rawValueConstructor = (Function<String, String>) (seed) -> "/path/to/bazel/" + seed;
    var sectionMapper =
        (Function<Path, ProjectViewBazelPathSection>) ProjectViewBazelPathSection::new;
    var elementMapper = (Function<String, Path>) Paths::get;

    var sectionConstructor =
        createSectionConstructor(rawValueConstructor, sectionMapper, elementMapper);
    var sectionName = parser.getSectionName();

    return Arguments.of(parser, rawValueConstructor, sectionConstructor, sectionName);
  }

  private static Arguments buildManualTargetsSectionArguments() {
    var parser = ProjectViewBuildManualTargetsSectionParser.INSTANCE;
    var rawValueConstructor = (Function<String, String>) (seed) -> "false";
    var sectionMapper =
        (Function<Boolean, ProjectViewBuildManualTargetsSection>)
            ProjectViewBuildManualTargetsSection::new;
    var elementMapper = (Function<String, Boolean>) Boolean::valueOf;

    var sectionConstructor =
        createSectionConstructor(rawValueConstructor, sectionMapper, elementMapper);
    var sectionName = parser.getSectionName();

    return Arguments.of(parser, rawValueConstructor, sectionConstructor, sectionName);
  }

  private static <V, T extends ProjectViewSingletonSection<V>>
      Function<String, T> createSectionConstructor(
          Function<String, String> rawValueConstructor,
          Function<V, T> sectionMapper,
          Function<String, V> elementMapper) {

    return (seed) -> sectionMapper.apply(elementMapper.apply(rawValueConstructor.apply(seed)));
  }

  @Nested
  @DisplayName("T parse(rawSection) tests")
  class ParseRawSectionTest {

    @MethodSource(
        "org.jetbrains.bsp.bazel.projectview.parser.sections.ProjectViewSingletonSectionParserTest#data")
    @ParameterizedTest
    public void shouldReturnFailureForWrongSectionName(
        ProjectViewSingletonSectionParser<V, T> parser,
        Function<String, String> rawValueConstructor,
        Function<String, T> sectionConstructor,
        String sectionName) {
      // given
      var rawSection = new ProjectViewRawSection("wrongsection", "value");

      // when
      var sectionTry = parser.parse(rawSection);

      // then
      assertThat(sectionTry.isFailure()).isTrue();
      assertThat(sectionTry.getCause().getClass()).isEqualTo(IllegalArgumentException.class);
      assertThat(sectionTry.getCause().getMessage())
          .isEqualTo(
              "Project view parsing failed! Expected '"
                  + sectionName
                  + "' section name, got 'wrongsection'!");
    }

    @MethodSource(
        "org.jetbrains.bsp.bazel.projectview.parser.sections.ProjectViewSingletonSectionParserTest#data")
    @ParameterizedTest
    public void shouldReturnEmptyForEmptySectionBody(
        ProjectViewSingletonSectionParser<V, T> parser,
        Function<String, String> rawValueConstructor,
        Function<String, T> sectionConstructor,
        String sectionName) {
      // given
      var rawSection = new ProjectViewRawSection(sectionName, "");

      // when
      var sectionTry = parser.parse(rawSection);

      // then
      assertThat(sectionTry.isSuccess()).isTrue();
      var section = sectionTry.get();

      assertThat(section).isNull();
    }

    @MethodSource(
        "org.jetbrains.bsp.bazel.projectview.parser.sections.ProjectViewSingletonSectionParserTest#data")
    @ParameterizedTest
    public void shouldReturnSectionWithTrimmedValue(
        ProjectViewSingletonSectionParser<V, T> parser,
        Function<String, String> rawValueConstructor,
        Function<String, T> sectionConstructor,
        String sectionName) {
      // given
      var rawSection =
          new ProjectViewRawSection(
              sectionName, "  " + rawValueConstructor.apply("value") + "\t\n");

      // when
      var sectionTry = parser.parse(rawSection);

      // then
      assertThat(sectionTry.isSuccess()).isTrue();
      var section = sectionTry.get();

      var expectedSection = sectionConstructor.apply("value");
      assertThat(section).isEqualTo(expectedSection);
    }
  }

  @Nested
  @DisplayName("T parse(rawSections) tests")
  class ParseRawSectionsTest {

    @MethodSource(
        "org.jetbrains.bsp.bazel.projectview.parser.sections.ProjectViewSingletonSectionParserTest#data")
    @ParameterizedTest
    public void shouldReturnLastSectionWithoutExplicitDefault(
        ProjectViewSingletonSectionParser<V, T> parser,
        Function<String, String> rawValueConstructor,
        Function<String, T> sectionConstructor,
        String sectionName) {
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
      assertThat(section).isEqualTo(expectedSection);
    }

    @MethodSource(
        "org.jetbrains.bsp.bazel.projectview.parser.sections.ProjectViewSingletonSectionParserTest#data")
    @ParameterizedTest
    public void shouldReturnEmptyIfSectionDoesntExist(
        ProjectViewSingletonSectionParser<V, T> parser,
        Function<String, String> rawValueConstructor,
        Function<String, T> sectionConstructor,
        String sectionName) {
      // given
      var rawSection1 = new ProjectViewRawSection("another_section1", "value1");
      var rawSection2 = new ProjectViewRawSection("another_section2", "  value2");
      var rawSection3 = new ProjectViewRawSection("another_section3", "\tvalue3\n");

      var rawSections = new ProjectViewRawSections(List.of(rawSection1, rawSection2, rawSection3));

      // when
      var section = parser.parse(rawSections);

      // then
      assertThat(section).isNull();
    }
  }
}
