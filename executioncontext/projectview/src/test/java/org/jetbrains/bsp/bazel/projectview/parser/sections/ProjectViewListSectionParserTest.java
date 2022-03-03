package org.jetbrains.bsp.bazel.projectview.parser.sections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewListSection;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewTargetsSection;
import org.jetbrains.bsp.bazel.projectview.parser.splitter.ProjectViewRawSection;
import org.jetbrains.bsp.bazel.projectview.parser.splitter.ProjectViewRawSections;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@SuppressWarnings("OptionalGetWithoutIsPresent")
@RunWith(value = Parameterized.class)
public class ProjectViewListSectionParserTest<V, T extends ProjectViewListSection<V>> {

  private final ProjectViewListSectionParser<V, T> parser;

  private final Function<String, String> rawIncludedElementConstructor;
  private final Function<String, String> rawExcludedElementConstructor;

  private final BiFunction<List<String>, List<String>, T> sectionConstructor;

  private final String sectionName;

  public ProjectViewListSectionParserTest(
      ProjectViewListSectionParser<V, T> parser,
      Function<String, String> rawIncludedElementConstructor,
      BiFunction<List<V>, List<V>, T> sectionMapper,
      Function<String, V> elementMapper) {
    this.parser = parser;

    this.rawIncludedElementConstructor = rawIncludedElementConstructor;
    this.rawExcludedElementConstructor = (seed) -> "-" + rawIncludedElementConstructor.apply(seed);

    this.sectionConstructor =
        createSectionConstructor(sectionMapper, rawIncludedElementConstructor, elementMapper);

    this.sectionName = parser.sectionName;
  }

  private BiFunction<List<String>, List<String>, T> createSectionConstructor(
      BiFunction<List<V>, List<V>, T> sectionMapper,
      Function<String, String> rawIncludedElementConstructor,
      Function<String, V> elementMapper) {

    return (includedElements, excludedElements) ->
        sectionMapper.apply(
            mapElements(rawIncludedElementConstructor, elementMapper, includedElements),
            mapElements(rawIncludedElementConstructor, elementMapper, excludedElements));
  }

  private List<V> mapElements(
      Function<String, String> rawIncludedElementConstructor,
      Function<String, V> elementMapper,
      List<String> rawElements) {
    return rawElements.stream()
        .map(rawIncludedElementConstructor)
        .map(elementMapper)
        .collect(Collectors.toList());
  }

  @Parameterized.Parameters(name = "{index}: ProjectViewListSectionParserTest for {0}")
  public static Collection<Object[]> data() {
    return List.of(
        new Object[][] {
          {
            new ProjectViewTargetsSectionParser(),
            (Function<String, String>) (seed) -> "//target:" + seed,
            (BiFunction<
                    List<BuildTargetIdentifier>,
                    List<BuildTargetIdentifier>,
                    ProjectViewTargetsSection>)
                ProjectViewTargetsSection::new,
            (Function<String, BuildTargetIdentifier>) BuildTargetIdentifier::new,
          },
        });
  }
  // ProjectViewListSection parse(rawSection)

  @Test
  public void shouldReturnFailureForWrongSectionName() {
    // given
    var rawSection = new ProjectViewRawSection("wrongsection", "-bodyelement");

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
  public void shouldParseEmptySectionBody() {
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
  public void shouldParseIncludedElements() {
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
    assertTrue(sectionTry.isSuccess());
    var section = sectionTry.get();

    var expectedSection =
        sectionConstructor.apply(List.of("included1", "included2", "included3"), List.of());
    assertEquals(expectedSection, section.get());
  }

  @Test
  public void shouldParseExcludedElements() {
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
    assertTrue(sectionTry.isSuccess());
    var section = sectionTry.get();

    var expectedSection =
        sectionConstructor.apply(List.of(), List.of("excluded1", "excluded2", "excluded3"));
    assertEquals(expectedSection, section.get());
  }

  @Test
  public void shouldParseIncludedAndExcludedElements() {
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
    assertTrue(sectionTry.isSuccess());
    var section = sectionTry.get();

    var expectedSection =
        sectionConstructor.apply(List.of("included1"), List.of("excluded1", "excluded2"));
    assertEquals(expectedSection, section.get());
  }

  // ProjectViewListSection parse(rawSections)

  @Test
  public void shouldReturnEmptySectionIfThereIsNoSectionForParseWithoutDefault() {
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
    assertTrue(section.isEmpty());
  }

  @Test
  public void shouldParseAllSectionElementsFromListWithoutDefault() {
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
    assertEquals(expectedSection, section.get());
  }

  // ProjectViewListSection parseOrDefault(rawSections, defaultValue)

  @Test
  public void shouldParseAllSectionElementsFromListAndSkipDefault() {
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

    var defaultListSection =
        sectionConstructor.apply(
            List.of("default_included1"), List.of("default_excluded1", "default_excluded2"));

    // when
    var section = parser.parseOrDefault(rawSections, Optional.of(defaultListSection));

    // then
    var expectedSection =
        sectionConstructor.apply(List.of("included1"), List.of("excluded1", "excluded2"));
    assertEquals(expectedSection, section.get());
  }

  @Test
  public void shouldReturnDefaultForNoElementsSectionInList() {
    // given
    var rawSection1 = new ProjectViewRawSection("another_section1", "-bodyelement1");
    var rawSection2 = new ProjectViewRawSection("another_section2", "-bodyelement2");

    var rawSections = new ProjectViewRawSections(List.of(rawSection1, rawSection2));

    var defaultListSection =
        sectionConstructor.apply(
            List.of("default_included1"), List.of("default_excluded1", "default_excluded2"));

    // when
    var section = parser.parseOrDefault(rawSections, Optional.of(defaultListSection));

    // then
    var expectedSection =
        sectionConstructor.apply(
            List.of("default_included1"), List.of("default_excluded1", "default_excluded2"));
    assertEquals(expectedSection, section.get());
  }
}
