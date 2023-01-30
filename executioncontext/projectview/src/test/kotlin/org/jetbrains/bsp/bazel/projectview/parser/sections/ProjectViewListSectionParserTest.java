package org.jetbrains.bsp.bazel.projectview.parser.sections;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBuildFlagsSection;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewListSection;
import org.jetbrains.bsp.bazel.projectview.parser.splitter.ProjectViewRawSection;
import org.jetbrains.bsp.bazel.projectview.parser.splitter.ProjectViewRawSections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

// TODO to kotlin
public class ProjectViewListSectionParserTest<V, T extends ProjectViewListSection<V>> {

  public static Stream<Arguments> data() {
    return Stream.of(buildFlagsSectionArguments());
  }

  private static Arguments buildFlagsSectionArguments() {
    var parser = ProjectViewBuildFlagsSectionParser.INSTANCE;
    var rawElementConstructor =
        (Function<String, String>) (seed) -> "--flag_" + seed + "=dummy_value";
    var sectionMapper =
        (Function<List<String>, ProjectViewBuildFlagsSection>) ProjectViewBuildFlagsSection::new;
    var elementMapper = (Function<String, String>) x -> x;
    var sectionConstructor =
        createSectionConstructor(sectionMapper, rawElementConstructor, elementMapper);
    var sectionName = parser.getSectionName();

    return Arguments.of(parser, rawElementConstructor, sectionConstructor, sectionName);
  }

  private static <V, T extends ProjectViewListSection<V>>
      Function<List<String>, T> createSectionConstructor(
          Function<List<V>, T> sectionMapper,
          Function<String, String> rawIncludedElementConstructor,
          Function<String, V> elementMapper) {
    return includedElements ->
        sectionMapper.apply(
            mapElements(rawIncludedElementConstructor, elementMapper, includedElements));
  }

  private static <V, T extends ProjectViewListSection<V>> List<V> mapElements(
      Function<String, String> rawIncludedElementConstructor,
      Function<String, V> elementMapper,
      List<String> rawElements) {
    return rawElements.stream()
        .map(rawIncludedElementConstructor)
        .map(elementMapper)
        .collect(Collectors.toList());
  }

  @Nested
  @DisplayName("ProjectViewListSection parse(rawSection) tests")
  class ParseRawSectionTest {

    @MethodSource(
        "org.jetbrains.bsp.bazel.projectview.parser.sections.ProjectViewListSectionParserTest#data")
    @ParameterizedTest
    public void shouldReturnFailureForWrongSectionName(
        ProjectViewListSectionParser<V, T> parser,
        Function<String, String> rawElementConstructor,
        Function<List<String>, T> sectionConstructor,
        String sectionName) {
      // given
      var rawSection = new ProjectViewRawSection("wrongsection", "bodyelement");

      // when
      var sectionTry = parser.parse(rawSection);

      // then
      assertThat(sectionTry.isFailure()).isTrue();

      assertThat(sectionTry.getCause().getMessage())
          .isEqualTo(
              "Project view parsing failed! Expected '"
                  + sectionName
                  + "' section name, got 'wrongsection'!");
    }

    @MethodSource(
        "org.jetbrains.bsp.bazel.projectview.parser.sections.ProjectViewListSectionParserTest#data")
    @ParameterizedTest
    public void shouldParseEmptySectionBody(
        ProjectViewListSectionParser<V, T> parser,
        Function<String, String> rawElementConstructor,
        Function<List<String>, T> sectionConstructor,
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
        "org.jetbrains.bsp.bazel.projectview.parser.sections.ProjectViewListSectionParserTest#data")
    @ParameterizedTest
    public void shouldParseElements(
        ProjectViewListSectionParser<V, T> parser,
        Function<String, String> rawElementConstructor,
        Function<List<String>, T> sectionConstructor,
        String sectionName) {
      // given
      var rawSection =
          new ProjectViewRawSection(
              sectionName,
              "  "
                  + rawElementConstructor.apply("element1")
                  + "\n\t"
                  + rawElementConstructor.apply("element2")
                  + "\n"
                  + rawElementConstructor.apply("element3")
                  + "\n\n");

      // when
      var sectionTry = parser.parse(rawSection);

      // then
      assertThat(sectionTry.isSuccess()).isTrue();
      var section = sectionTry.get();

      var expectedSection = sectionConstructor.apply(List.of("element1", "element2", "element3"));
      assertThat(section).isEqualTo(expectedSection);
    }

    // ProjectViewListSection parse(rawSections)

    @MethodSource(
        "org.jetbrains.bsp.bazel.projectview.parser.sections.ProjectViewListSectionParserTest#data")
    @ParameterizedTest
    public void shouldReturnEmptySectionIfThereIsNoSectionForParseWithoutDefault(
        ProjectViewListSectionParser<V, T> parser,
        Function<String, String> rawElementConstructor,
        Function<List<String>, T> sectionConstructor,
        String sectionName) {
      // given
      var rawSection1 =
          new ProjectViewRawSection(
              "another_section1", "  -bodyelement1.1\n\tbodyelement1.2\n-bodyelement1.3\n\n");
      var rawSection2 = new ProjectViewRawSection("another_section2", "bodyelement2.1");
      var rawSection3 = new ProjectViewRawSection("another_section3", "-bodyelement3.1");

      var rawSections = new ProjectViewRawSections(List.of(rawSection1, rawSection2, rawSection3));

      // when
      var section = parser.parse(rawSections);

      // then
      assertThat(section).isNull();
    }

    @MethodSource(
        "org.jetbrains.bsp.bazel.projectview.parser.sections.ProjectViewListSectionParserTest#data")
    @ParameterizedTest
    public void shouldParseAllSectionElementsFromListWithoutDefault(
        ProjectViewListSectionParser<V, T> parser,
        Function<String, String> rawElementConstructor,
        Function<List<String>, T> sectionConstructor,
        String sectionName) {
      // given
      var rawSection1 = new ProjectViewRawSection("another_section1", "bodyelement1");
      var rawSection2 =
          new ProjectViewRawSection(
              sectionName,
              " "
                  + rawElementConstructor.apply("element1")
                  + "\n"
                  + rawElementConstructor.apply("element2"));
      var rawSection3 = new ProjectViewRawSection("another_section2", "-bodyelement2");
      var rawSection4 =
          new ProjectViewRawSection(
              sectionName, "\n\t" + rawElementConstructor.apply("element3") + "\n\n\n");

      var rawSections =
          new ProjectViewRawSections(List.of(rawSection1, rawSection2, rawSection3, rawSection4));

      // when
      var section = parser.parse(rawSections);

      // then
      var expectedSection = sectionConstructor.apply(List.of("element1", "element2", "element3"));
      assertThat(section).isEqualTo(expectedSection);
    }
  }
}
