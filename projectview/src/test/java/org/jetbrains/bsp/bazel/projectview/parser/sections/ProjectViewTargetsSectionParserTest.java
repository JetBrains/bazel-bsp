package org.jetbrains.bsp.bazel.projectview.parser.sections;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewTargetsSection;
import org.jetbrains.bsp.bazel.projectview.parser.splitter.ProjectViewRawSection;
import org.jetbrains.bsp.bazel.projectview.parser.splitter.ProjectViewRawSections;
import org.junit.Before;
import org.junit.Test;

public class ProjectViewTargetsSectionParserTest {

  private ProjectViewTargetsSectionParser parser;

  @Before
  public void before() {
    // given
    this.parser = new ProjectViewTargetsSectionParser();
  }

  // ProjectViewTargetsSection parse(rawSection)

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowIllegalArgumentExceptionForWrongSectionName() {
    // given
    var rawSection = new ProjectViewRawSection("wrongsection", "-bodyelement");

    // when
    parser.parse(rawSection);

    // then
    // throw an exception
  }

  @Test
  public void shouldParseEmptySectionBody() {
    // given
    var rawSection = new ProjectViewRawSection("targets", "");

    // when
    var section = parser.parse(rawSection);

    // then
    var expectedSection = new ProjectViewTargetsSection(ImmutableList.of(), ImmutableList.of());
    assertEquals(expectedSection, section);
  }

  @Test
  public void shouldParseIncludedTargets() {
    // given
    var sectionBody =
        "  //test_included1:test1\n\t//:test_included1:test2\n//:test_included2:test1\n\n";
    var rawSection = new ProjectViewRawSection("targets", sectionBody);

    // when
    var section = parser.parse(rawSection);

    // then
    var expectedSection =
        new ProjectViewTargetsSection(
            ImmutableList.of(
                "//test_included1:test1", "//:test_included1:test2", "//:test_included2:test1"),
            ImmutableList.of());
    assertEquals(expectedSection, section);
  }

  @Test
  public void shouldParseExcludedTargets() {
    // given
    var sectionBody =
        "  -//test_excluded1:test1\n\t-//test_excluded1:test2\n-//test_excluded2:test1\n\n";
    var rawSection = new ProjectViewRawSection("targets", sectionBody);

    // when
    var section = parser.parse(rawSection);

    // then
    var expectedSection =
        new ProjectViewTargetsSection(
            ImmutableList.of(),
            ImmutableList.of(
                "//test_excluded1:test1", "//test_excluded1:test2", "//test_excluded2:test1"));
    assertEquals(expectedSection, section);
  }

  @Test
  public void shouldParseIncludedAndExcludedTargets() {
    // given
    var sectionBody =
        "  -//test_excluded1:test1\n\t//test_included1:test1\n-//test_excluded1:test2\n\n";
    var rawSection = new ProjectViewRawSection("targets", sectionBody);

    // when
    var section = parser.parse(rawSection);

    // then
    var expectedSection =
        new ProjectViewTargetsSection(
            ImmutableList.of("//test_included1:test1"),
            ImmutableList.of("//test_excluded1:test1", "//test_excluded1:test2"));
    assertEquals(expectedSection, section);
  }

  // ProjectViewTargetsSection parse(rawSections)

  @Test
  public void shouldReturnEmptySectionIfThereIsNoSectionForParseWithoutDefault() {
    // given
    var sectionBody =
        "  -//test_excluded1:test1\n\t//test_included1:test1\n-//test_excluded1:test2\n\n";
    var rawSection1 = new ProjectViewRawSection("anotersection1", sectionBody);
    var rawSection2 = new ProjectViewRawSection("anotersection2", "-bodyelement2");
    var rawSection3 = new ProjectViewRawSection("anotersection3", "-bodyelement3");

    var rawSections =
        new ProjectViewRawSections(ImmutableList.of(rawSection1, rawSection2, rawSection3));

    // when
    var section = parser.parse(rawSections);

    // then
    var expectedSection = new ProjectViewTargetsSection();
    assertEquals(expectedSection, section);
  }

  @Test
  public void shouldParseAllTargetsSectionFromListWithoutDefault() {
    // given
    var rawSection1 = new ProjectViewRawSection("anotersection1", "-bodyelement1");
    var rawSection2 =
        new ProjectViewRawSection("targets", "  -//test_excluded1:test1\n-//test_excluded1:test2");
    var rawSection3 = new ProjectViewRawSection("anotersection2", "-bodyelement2");
    var rawSection4 = new ProjectViewRawSection("targets", "\n\t//test_included1:test1\n\n\n");

    var rawSections =
        new ProjectViewRawSections(
            ImmutableList.of(rawSection1, rawSection2, rawSection3, rawSection4));

    // when
    var section = parser.parse(rawSections);

    // then
    var expectedSection =
        new ProjectViewTargetsSection(
            ImmutableList.of("//test_included1:test1"),
            ImmutableList.of("//test_excluded1:test1", "//test_excluded1:test2"));
    assertEquals(expectedSection, section);
  }

  // ProjectViewTargetsSection parseOrDefault(rawSections, defaultValue)

  @Test
  public void shouldParseAllTargetsSectionFromList() {
    // given
    var rawSection1 = new ProjectViewRawSection("anotersection1", "-bodyelement1");
    var rawSection2 =
        new ProjectViewRawSection("targets", "  -//test_excluded1:test1\n-//test_excluded1:test2");
    var rawSection3 = new ProjectViewRawSection("anotersection2", "-bodyelement2");
    var rawSection4 = new ProjectViewRawSection("targets", "\n\t//test_included1:test1\n\n\n");

    var rawSections =
        new ProjectViewRawSections(
            ImmutableList.of(rawSection1, rawSection2, rawSection3, rawSection4));

    var defaultTargetsSection =
        new ProjectViewTargetsSection(
            ImmutableList.of("//default_test_included1:test1"),
            ImmutableList.of("//default_excluded_test:test1", "//default_excluded_test:test1"));

    // when
    var section = parser.parseOrDefault(rawSections, defaultTargetsSection);

    // then
    var expectedSection =
        new ProjectViewTargetsSection(
            ImmutableList.of("//test_included1:test1"),
            ImmutableList.of("//test_excluded1:test1", "//test_excluded1:test2"));
    assertEquals(expectedSection, section);
  }

  @Test
  public void shouldReturnDefaultForNoTargetsSectionInList() {
    // given
    var rawSection1 = new ProjectViewRawSection("anotersection1", "-bodyelement1");
    var rawSection2 = new ProjectViewRawSection("anotersection2", "-bodyelement2");

    var rawSections = new ProjectViewRawSections(ImmutableList.of(rawSection1, rawSection2));

    var defaultTargetsSection =
        new ProjectViewTargetsSection(
            ImmutableList.of("//default_test_included1:test1"),
            ImmutableList.of("//default_excluded_test:test1", "//default_excluded_test:test1"));

    // when
    var section = parser.parseOrDefault(rawSections, defaultTargetsSection);

    // then
    var expectedSection =
        new ProjectViewTargetsSection(
            ImmutableList.of("//default_test_included1:test1"),
            ImmutableList.of("//default_excluded_test:test1", "//default_excluded_test:test1"));
    assertEquals(expectedSection, section);
  }
}
