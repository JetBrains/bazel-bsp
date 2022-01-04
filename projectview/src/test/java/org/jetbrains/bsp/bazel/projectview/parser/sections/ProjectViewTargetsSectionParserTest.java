package org.jetbrains.bsp.bazel.projectview.parser.sections;

import com.google.common.collect.ImmutableList;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewTargetsSection;
import org.jetbrains.bsp.bazel.projectview.parser.splitter.ProjectViewRawSection;
import org.jetbrains.bsp.bazel.projectview.parser.splitter.ProjectViewRawSections;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ProjectViewTargetsSectionParserTest {

  private ProjectViewTargetsSectionParser parser;

  @Before
  public void before() {
    // given
    this.parser = new ProjectViewTargetsSectionParser();
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowIllegalArgumentExceptionForWrongSectionName() {
    // given
    ProjectViewRawSection rawSection = new ProjectViewRawSection("wrongsection", "-bodyelement");

    // when
    parser.parse(rawSection);

    // then
    // throw an exception
  }

  @Test
  public void shouldParseEmptySectionBody() {
    // given
    ProjectViewRawSection rawSection = new ProjectViewRawSection("targets", "");

    // when
    ProjectViewTargetsSection section = parser.parse(rawSection);

    // then
    ProjectViewTargetsSection expectedSection =
        new ProjectViewTargetsSection(ImmutableList.of(), ImmutableList.of());

    assertEquals(expectedSection, section);
  }

  @Test
  public void shouldParseIncludedTargets() {
    // given
    String sectionBody =
        "  //test_included1:test1\n\t//:test_included1:test2\n//:test_included2:test1\n\n";
    ProjectViewRawSection rawSection = new ProjectViewRawSection("targets", sectionBody);

    // when
    ProjectViewTargetsSection section = parser.parse(rawSection);

    // then
    ProjectViewTargetsSection expectedSection =
        new ProjectViewTargetsSection(
            ImmutableList.of(
                "//test_included1:test1", "//:test_included1:test2", "//:test_included2:test1"),
            ImmutableList.of());

    assertEquals(expectedSection, section);
  }

  @Test
  public void shouldParseExcludedTargets() {
    // given
    String sectionBody =
        "  -//test_excluded1:test1\n\t-//test_excluded1:test2\n-//test_excluded2:test1\n\n";
    ProjectViewRawSection rawSection = new ProjectViewRawSection("targets", sectionBody);

    // when
    ProjectViewTargetsSection section = parser.parse(rawSection);

    // then
    ProjectViewTargetsSection expectedSection =
        new ProjectViewTargetsSection(
            ImmutableList.of(),
            ImmutableList.of(
                "//test_excluded1:test1", "//test_excluded1:test2", "//test_excluded2:test1"));

    assertEquals(expectedSection, section);
  }

  @Test
  public void shouldParseIncludedAndExcludedTargets() {
    // given
    String sectionBody =
        "  -//test_excluded1:test1\n\t//test_included1:test1\n-//test_excluded1:test2\n\n";
    ProjectViewRawSection rawSection = new ProjectViewRawSection("targets", sectionBody);

    // when
    ProjectViewTargetsSection section = parser.parse(rawSection);

    // then
    ProjectViewTargetsSection expectedSection =
        new ProjectViewTargetsSection(
            ImmutableList.of("//test_included1:test1"),
            ImmutableList.of("//test_excluded1:test1", "//test_excluded1:test2"));

    assertEquals(expectedSection, section);
  }

  @Test
  public void shouldReturnEmptySectionIfThereIsNoSectionForParseWithoutDefault() {
    // given
    String sectionBody =
        "  -//test_excluded1:test1\n\t//test_included1:test1\n-//test_excluded1:test2\n\n";
    ProjectViewRawSection rawSection1 = new ProjectViewRawSection("anotersection1", sectionBody);
    ProjectViewRawSection rawSection2 =
        new ProjectViewRawSection("anotersection2", "-bodyelement2");
    ProjectViewRawSection rawSection3 =
        new ProjectViewRawSection("anotersection3", "-bodyelement3");

    ProjectViewRawSections rawSections =
        new ProjectViewRawSections(ImmutableList.of(rawSection1, rawSection2, rawSection3));

    // when
    ProjectViewTargetsSection section = parser.parse(rawSections);

    // then
    assertEquals(new ProjectViewTargetsSection(), section);
  }

  @Test
  public void shouldParseAllTargetsSectionFromListWithoutDefault() {
    // given
    ProjectViewRawSection rawSection1 =
        new ProjectViewRawSection("anotersection1", "-bodyelement1");
    ProjectViewRawSection rawSection2 =
        new ProjectViewRawSection("targets", "  -//test_excluded1:test1\n-//test_excluded1:test2");
    ProjectViewRawSection rawSection3 =
        new ProjectViewRawSection("anotersection2", "-bodyelement2");
    ProjectViewRawSection rawSection4 =
        new ProjectViewRawSection("targets", "\n\t//test_included1:test1\n\n\n");

    ProjectViewRawSections rawSections =
        new ProjectViewRawSections(
            ImmutableList.of(rawSection1, rawSection2, rawSection3, rawSection4));

    // when
    ProjectViewTargetsSection section = parser.parse(rawSections);

    // then
    ProjectViewTargetsSection expectedSection =
        new ProjectViewTargetsSection(
            ImmutableList.of("//test_included1:test1"),
            ImmutableList.of("//test_excluded1:test1", "//test_excluded1:test2"));

    assertEquals(expectedSection, section);
  }

  @Test
  public void shouldParseAllTargetsSectionFromList() {
    // given
    ProjectViewRawSection rawSection1 =
        new ProjectViewRawSection("anotersection1", "-bodyelement1");
    ProjectViewRawSection rawSection2 =
        new ProjectViewRawSection("targets", "  -//test_excluded1:test1\n-//test_excluded1:test2");
    ProjectViewRawSection rawSection3 =
        new ProjectViewRawSection("anotersection2", "-bodyelement2");
    ProjectViewRawSection rawSection4 =
        new ProjectViewRawSection("targets", "\n\t//test_included1:test1\n\n\n");

    ProjectViewRawSections rawSections =
        new ProjectViewRawSections(
            ImmutableList.of(rawSection1, rawSection2, rawSection3, rawSection4));

    ProjectViewTargetsSection defaultTargetsSection =
        new ProjectViewTargetsSection(
            ImmutableList.of("//default_test_included1:test1"),
            ImmutableList.of("//default_excluded_test:test1", "//default_excluded_test:test1"));

    // when
    ProjectViewTargetsSection section = parser.parseOrDefault(rawSections, defaultTargetsSection);

    // then
    ProjectViewTargetsSection expectedSection =
        new ProjectViewTargetsSection(
            ImmutableList.of("//test_included1:test1"),
            ImmutableList.of("//test_excluded1:test1", "//test_excluded1:test2"));

    assertEquals(expectedSection, section);
  }

  @Test
  public void shouldReturnDefaultForNoTargetsSectionInList() {
    // given
    ProjectViewRawSection rawSection1 =
        new ProjectViewRawSection("anotersection1", "-bodyelement1");
    ProjectViewRawSection rawSection2 =
        new ProjectViewRawSection("anotersection2", "-bodyelement2");

    ProjectViewRawSections rawSections =
        new ProjectViewRawSections(ImmutableList.of(rawSection1, rawSection2));

    ProjectViewTargetsSection defaultTargetsSection =
        new ProjectViewTargetsSection(
            ImmutableList.of("//default_test_included1:test1"),
            ImmutableList.of("//default_excluded_test:test1", "//default_excluded_test:test1"));

    // when
    ProjectViewTargetsSection section = parser.parseOrDefault(rawSections, defaultTargetsSection);

    // then
    ProjectViewTargetsSection expectedSection =
        new ProjectViewTargetsSection(
            ImmutableList.of("//default_test_included1:test1"),
            ImmutableList.of("//default_excluded_test:test1", "//default_excluded_test:test1"));

    assertEquals(expectedSection, section);
  }
}
