package org.jetbrains.bsp.bazel.projectview.parser.sections;

import static org.assertj.core.api.Assertions.assertThat;

import io.vavr.collection.List;
import io.vavr.control.Option;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBuildFlagsSection;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewListSection;
import org.jetbrains.bsp.bazel.projectview.parser.splitter.ProjectViewRawSection;
import org.jetbrains.bsp.bazel.projectview.parser.splitter.ProjectViewRawSections;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(value = Parameterized.class)
public class ProjectViewListSectionParserTest<V, T extends ProjectViewListSection<V>> {

  private final ProjectViewListSectionParser<V, T> parser;

  private final Function<String, String> rawElementConstructor;
  private final Function<List<String>, T> sectionConstructor;

  private final String sectionName;

  public ProjectViewListSectionParserTest(
      ProjectViewListSectionParser<V, T> parser,
      Function<String, String> rawElementConstructor,
      Function<List<V>, T> sectionMapper,
      Function<String, V> elementMapper) {
    this.parser = parser;

    this.rawElementConstructor = rawElementConstructor;
    this.sectionConstructor =
        createSectionConstructor(sectionMapper, rawElementConstructor, elementMapper);

    this.sectionName = parser.sectionName;
  }

  private Function<List<String>, T> createSectionConstructor(
      Function<List<V>, T> sectionMapper,
      Function<String, String> rawElementConstructor,
      Function<String, V> elementMapper) {

    return elements ->
        sectionMapper.apply(mapElements(rawElementConstructor, elementMapper, elements));
  }

  private List<V> mapElements(
      Function<String, String> rawIncludedElementConstructor,
      Function<String, V> elementMapper,
      List<String> rawElements) {
    return rawElements.map(rawIncludedElementConstructor).map(elementMapper);
  }

  @Parameterized.Parameters(name = "{index}: ProjectViewListSectionParserTest for {0}")
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
  // ProjectViewListSection parse(rawSection)

  @Test
  public void shouldReturnFailureForWrongSectionName() {
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

  // ProjectViewListSection parseOrDefault(rawSections, defaultValue)

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
