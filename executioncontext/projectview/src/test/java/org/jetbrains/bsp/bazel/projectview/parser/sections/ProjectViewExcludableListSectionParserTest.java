package org.jetbrains.bsp.bazel.projectview.parser.sections;

import static org.assertj.core.api.Assertions.assertThat;

import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewExcludableListSection;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewTargetsSection;
import org.jetbrains.bsp.bazel.projectview.parser.splitter.ProjectViewRawSection;
import org.jetbrains.bsp.bazel.projectview.parser.splitter.ProjectViewRawSections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

// TODO to kotlin
public class ProjectViewExcludableListSectionParserTest<
    V, T extends ProjectViewExcludableListSection<V>> {

  public static Stream<Arguments> data() {
    return Stream.of(targetsSectionArguments());
  }

  private static Arguments targetsSectionArguments() {
    var parser = ProjectViewTargetsSectionParser.INSTANCE;
    var rawIncludedElementConstructor = (Function<String, String>) (seed) -> "//target:" + seed;
    var rawExcludedElementConstructor =
        (Function<String, String>) (seed) -> "-" + rawIncludedElementConstructor.apply(seed);
    var sectionMapper =
        (BiFunction<
                List<BuildTargetIdentifier>,
                List<BuildTargetIdentifier>,
                ProjectViewTargetsSection>)
            ProjectViewTargetsSection::new;
    var elementMapper = (Function<String, BuildTargetIdentifier>) BuildTargetIdentifier::new;

    var sectionConstructor =
        createSectionConstructor(sectionMapper, rawIncludedElementConstructor, elementMapper);

    var sectionName = parser.getSectionName();

    return Arguments.of(
        parser,
        rawIncludedElementConstructor,
        rawExcludedElementConstructor,
        sectionConstructor,
        sectionName);
  }

  private static <V, T extends ProjectViewExcludableListSection<V>>
      BiFunction<List<String>, List<String>, T> createSectionConstructor(
          BiFunction<List<V>, List<V>, T> sectionMapper,
          Function<String, String> rawIncludedElementConstructor,
          Function<String, V> elementMapper) {

    return (includedElements, excludedElements) ->
        sectionMapper.apply(
            mapElements(rawIncludedElementConstructor, elementMapper, includedElements),
            mapElements(rawIncludedElementConstructor, elementMapper, excludedElements));
  }

  private static <V> List<V> mapElements(
      Function<String, String> rawIncludedElementConstructor,
      Function<String, V> elementMapper,
      List<String> rawElements) {
    return rawElements.stream().map(rawIncludedElementConstructor).map(elementMapper).collect(Collectors.toList());
  }

  @Nested
  @DisplayName("ProjectViewExcludableListSection parse(rawSection) tests")
  class ParseRawSectionTest {

    @MethodSource(
        "org.jetbrains.bsp.bazel.projectview.parser.sections.ProjectViewExcludableListSectionParserTest#data")
    @ParameterizedTest
    public void shouldReturnFailureForWrongSectionName(
        ProjectViewExcludableListSectionParser<V, T> parser,
        Function<String, String> rawIncludedElementConstructor,
        Function<String, String> rawExcludedElementConstructor,
        BiFunction<List<String>, List<String>, T> sectionConstructor,
        String sectionName) {
      // given
      var rawSection = new ProjectViewRawSection("wrongsection", "-bodyelement");

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
        "org.jetbrains.bsp.bazel.projectview.parser.sections.ProjectViewExcludableListSectionParserTest#data")
    @ParameterizedTest
    public void shouldParseEmptySectionBody(
        ProjectViewExcludableListSectionParser<V, T> parser,
        Function<String, String> rawIncludedElementConstructor,
        Function<String, String> rawExcludedElementConstructor,
        BiFunction<List<String>, List<String>, T> sectionConstructor,
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
        "org.jetbrains.bsp.bazel.projectview.parser.sections.ProjectViewExcludableListSectionParserTest#data")
    @ParameterizedTest
    public void shouldParseIncludedElements(
        ProjectViewExcludableListSectionParser<V, T> parser,
        Function<String, String> rawIncludedElementConstructor,
        Function<String, String> rawExcludedElementConstructor,
        BiFunction<List<String>, List<String>, T> sectionConstructor,
        String sectionName) {
      // given
      var rawSection =
          new ProjectViewRawSection(
              sectionName,
              "  "
                  + rawIncludedElementConstructor.apply("included1")
                  + "\n\t"
                  + rawIncludedElementConstructor.apply("included2")
                  + "\n"
                  + rawIncludedElementConstructor.apply("included3")
                  + "\n\n");

      // when
      var sectionTry = parser.parse(rawSection);

      // then
      assertThat(sectionTry.isSuccess()).isTrue();
      var section = sectionTry.get();

      var expectedSection =
          sectionConstructor.apply(List.of("included1", "included2", "included3"), List.of());
      assertThat(section).isEqualTo(expectedSection);
    }

    @MethodSource(
        "org.jetbrains.bsp.bazel.projectview.parser.sections.ProjectViewExcludableListSectionParserTest#data")
    @ParameterizedTest
    public void shouldParseExcludedElements(
        ProjectViewExcludableListSectionParser<V, T> parser,
        Function<String, String> rawIncludedElementConstructor,
        Function<String, String> rawExcludedElementConstructor,
        BiFunction<List<String>, List<String>, T> sectionConstructor,
        String sectionName) {
      // given
      var rawSection =
          new ProjectViewRawSection(
              sectionName,
              "  "
                  + rawExcludedElementConstructor.apply("excluded1")
                  + "\n\t"
                  + rawExcludedElementConstructor.apply("excluded2")
                  + "\n"
                  + rawExcludedElementConstructor.apply("excluded3")
                  + "\n\n");

      // when
      var sectionTry = parser.parse(rawSection);

      // then
      assertThat(sectionTry.isSuccess()).isTrue();
      var section = sectionTry.get();

      var expectedSection =
          sectionConstructor.apply(List.of(), List.of("excluded1", "excluded2", "excluded3"));
      assertThat(section).isEqualTo(expectedSection);
    }

    @MethodSource(
        "org.jetbrains.bsp.bazel.projectview.parser.sections.ProjectViewExcludableListSectionParserTest#data")
    @ParameterizedTest
    public void shouldParseIncludedAndExcludedElements(
        ProjectViewExcludableListSectionParser<V, T> parser,
        Function<String, String> rawIncludedElementConstructor,
        Function<String, String> rawExcludedElementConstructor,
        BiFunction<List<String>, List<String>, T> sectionConstructor,
        String sectionName) {
      // given
      var rawSection =
          new ProjectViewRawSection(
              sectionName,
              "  "
                  + rawExcludedElementConstructor.apply("excluded1")
                  + "\n\t"
                  + rawIncludedElementConstructor.apply("included1")
                  + "\n"
                  + rawExcludedElementConstructor.apply("excluded2")
                  + "\n\n");

      // when
      var sectionTry = parser.parse(rawSection);

      // then
      assertThat(sectionTry.isSuccess()).isTrue();
      var section = sectionTry.get();

      var expectedSection =
          sectionConstructor.apply(List.of("included1"), List.of("excluded1", "excluded2"));
      assertThat(section).isEqualTo(expectedSection);
    }
  }

  @Nested
  @DisplayName("ProjectViewExcludableListSection parse(rawSections) test")
  class ParseRawSectionsTest {

    @MethodSource(
        "org.jetbrains.bsp.bazel.projectview.parser.sections.ProjectViewExcludableListSectionParserTest#data")
    @ParameterizedTest
    public void shouldReturnEmptySectionIfThereIsNoSectionForParseWithoutDefault(
        ProjectViewExcludableListSectionParser<V, T> parser,
        Function<String, String> rawIncludedElementConstructor,
        Function<String, String> rawExcludedElementConstructor,
        BiFunction<List<String>, List<String>, T> sectionConstructor,
        String sectionName) {
      // given
      var rawSection1 =
          new ProjectViewRawSection(
              "another_section1", "  -bodyelement1.1\n\tbodyelement1.2\n-bodyelement1.3\n\n");
      var rawSection2 = new ProjectViewRawSection("another_section2", "-bodyelement2.1");
      var rawSection3 = new ProjectViewRawSection("another_section3", "-bodyelement3.1");

      var rawSections = new ProjectViewRawSections(List.of(rawSection1, rawSection2, rawSection3));

      // when
      var section = parser.parse(rawSections);

      // then
      assertThat(section).isNull();
    }

    @MethodSource(
        "org.jetbrains.bsp.bazel.projectview.parser.sections.ProjectViewExcludableListSectionParserTest#data")
    @ParameterizedTest
    public void shouldParseAllSectionElementsFromListWithoutDefault(
        ProjectViewExcludableListSectionParser<V, T> parser,
        Function<String, String> rawIncludedElementConstructor,
        Function<String, String> rawExcludedElementConstructor,
        BiFunction<List<String>, List<String>, T> sectionConstructor,
        String sectionName) {
      // given
      var rawSection1 = new ProjectViewRawSection("another_section1", "-bodyelement1");
      var rawSection2 =
          new ProjectViewRawSection(
              sectionName,
              " "
                  + rawExcludedElementConstructor.apply("excluded1")
                  + "\n"
                  + rawExcludedElementConstructor.apply("excluded2"));
      var rawSection3 = new ProjectViewRawSection("another_section2", "-bodyelement2");
      var rawSection4 =
          new ProjectViewRawSection(
              sectionName, "\n\t" + rawIncludedElementConstructor.apply("included1") + "\n\n\n");

      var rawSections =
          new ProjectViewRawSections(List.of(rawSection1, rawSection2, rawSection3, rawSection4));

      // when
      var section = parser.parse(rawSections);

      // then
      var expectedSection =
          sectionConstructor.apply(List.of("included1"), List.of("excluded1", "excluded2"));
      assertThat(section).isEqualTo(expectedSection);
    }
  }
}
