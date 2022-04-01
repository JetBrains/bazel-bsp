package org.jetbrains.bsp.bazel.projectview.parser.sections;

import static org.assertj.core.api.Assertions.assertThat;

import io.vavr.collection.List;
import io.vavr.control.Option;
import java.util.function.BiFunction;
import java.util.function.Function;
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

public class ProjectViewListSectionParserTest<V, T extends ProjectViewListSection<V>> {

  public static Stream<Arguments> data() {
    return Stream.of(targetsSectionArguments());
  }

  public static Collection<Object[]> data() {
    return Arrays.asList(
            new Object[][] {
                    {
                            new ProjectViewBuildFlagsSectionParser(),
                            (Function<String, String>) (seed) -> "--flag_" + seed + "=dummy_value",
                            (Function<List<String>, ProjectViewBuildFlagsSection>)
                                    ProjectViewBuildFlagsSection::new,
                            Function.<String>identity()
                    },
            });
  }

  private static Arguments targetsSectionArguments() {
    var parser = new ProjectViewTargetsSectionParser();
    var rawIncludedElementConstructor = (Function<String, String>) (seed) -> "//target:" + seed;
    var rawExcludedElementConstructor =
        createRawExcludedElementConstructor(rawIncludedElementConstructor);
    var sectionMapper =
        (BiFunction<
                List<BuildTargetIdentifier>,
                List<BuildTargetIdentifier>,
                ProjectViewTargetsSection>)
            ProjectViewTargetsSection::new;
    var elementMapper = (Function<String, BuildTargetIdentifier>) BuildTargetIdentifier::new;
    var sectionConstructor =
        createSectionConstructor(sectionMapper, rawIncludedElementConstructor, elementMapper);
    var sectionName = parser.sectionName;

    return Arguments.of(
        parser,
        rawIncludedElementConstructor,
        rawExcludedElementConstructor,
        sectionConstructor,
        sectionName);
  }

  private static Function<String, String> createRawExcludedElementConstructor(
      Function<String, String> rawIncludedElementConstructor) {
    return seed -> "-" + rawIncludedElementConstructor.apply(seed);
  }

  private static <V, T extends ProjectViewListSection<V>>
      BiFunction<List<String>, List<String>, T> createSectionConstructor(
          BiFunction<List<V>, List<V>, T> sectionMapper,
          Function<String, String> rawIncludedElementConstructor,
          Function<String, V> elementMapper) {
    return (includedElements, excludedElements) ->
        sectionMapper.apply(
            mapElements(rawIncludedElementConstructor, elementMapper, includedElements),
            mapElements(rawIncludedElementConstructor, elementMapper, excludedElements));
  }

  private static <V, T extends ProjectViewListSection<V>> List<V> mapElements(
      Function<String, String> rawIncludedElementConstructor,
      Function<String, V> elementMapper,
      List<String> rawElements) {
    return rawElements.map(rawIncludedElementConstructor).map(elementMapper);
  }

  @Nested
  @DisplayName("ProjectViewListSection parse(rawSection) tests")
  class ParseRawSectionTest {

    @MethodSource(
            "org.jetbrains.bsp.bazel.projectview.parser.sections.ProjectViewListSectionParserTest#data")
    @ParameterizedTest
  public void shouldReturnFailureForWrongSectionName(ProjectViewListSectionParser<V, T> parser,
                                                     Function<String, String> rawIncludedElementConstructor,
                                                     Function<String, String> rawExcludedElementConstructor,
                                                     BiFunction<List<String>, List<String>, T> sectionConstructor,
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

  @Test
  public void shouldParseEmptySectionBody() {
    // given
    var rawSection = new ProjectViewRawSection(sectionName, "");

    // when
    var sectionTry = parser.parse(rawSection);

    // then
    assertThat(sectionTry.isSuccess()).isTrue();
    var section = sectionTry.get();

    assertThat(section).isEmpty();
  }

  @Test
  public void shouldParseElements() {
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
    assertThat(section.get()).isEqualTo(expectedSection);
  }

  // ProjectViewListSection parse(rawSections)

  @Test
  public void shouldReturnEmptySectionIfThereIsNoSectionForParseWithoutDefault() {
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
    assertThat(section).isEmpty();
  }

  @Test
  public void shouldParseAllSectionElementsFromListWithoutDefault() {
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
    assertThat(section.get()).isEqualTo(expectedSection);
  }

  @Nested
  @DisplayName("ProjectViewListSection parseOrDefault(rawSections, defaultValue) tests")
  class ParseOrDefaultRawSectionsDefaultValueTest {

  @Test
  public void shouldParseAllSectionElementsFromListAndSkipDefault() {
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

    var defaultListSection =
        sectionConstructor.apply(List.of("default_element1", "default_element2"));

    // when
    var section = parser.parseOrDefault(rawSections, Option.of(defaultListSection));

    // then
    var expectedSection = sectionConstructor.apply(List.of("element1", "element2", "element3"));
    assertThat(section.get()).isEqualTo(expectedSection);
  }

  @Test
  public void shouldReturnDefaultForNoElementsSectionInList() {
    // given
    var rawSection1 = new ProjectViewRawSection("another_section1", "bodyelement1");
    var rawSection2 = new ProjectViewRawSection("another_section2", "-bodyelement2");

    var rawSections = new ProjectViewRawSections(List.of(rawSection1, rawSection2));

    var defaultListSection =
        sectionConstructor.apply(List.of("default_element1", "default_element2"));

    // when
    var section = parser.parseOrDefault(rawSections, Option.of(defaultListSection));

    // then
    var expectedSection = sectionConstructor.apply(List.of("default_element1", "default_element2"));
    assertThat(section.get()).isEqualTo(expectedSection);
  }
}
