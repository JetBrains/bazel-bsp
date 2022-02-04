package org.jetbrains.bsp.bazel.projectview.parser.splitter;

import static org.junit.Assert.assertEquals;

import java.util.List;
import org.junit.Test;

public class ProjectViewSectionSplitterTest {

  @Test
  public void shouldParseEmptyFile() {
    // given
    var emptyContent = "";

    // when
    var sections = ProjectViewSectionSplitter.split(emptyContent);

    // then
    var expectedSections = new ProjectViewRawSections(List.of());

    assertEquals(expectedSections, sections);
  }

  @Test
  public void shouldParseSectionWithoutColon() {
    // given
    var fileContent = "import path/to/file.bazelproject\n";

    // when
    var sections = ProjectViewSectionSplitter.split(fileContent);

    // then
    var expectedSections =
        new ProjectViewRawSections(
            List.of(new ProjectViewRawSection("import", "path/to/file.bazelproject\n")));

    assertEquals(expectedSections, sections);
  }

  @Test
  public void shouldParseSectionWithOnlyNewLineBefore() {
    // given
    var fileContent = "\nimport path/to/file.bazelproject\n";

    // when
    var sections = ProjectViewSectionSplitter.split(fileContent);

    // then
    var expectedSections =
        new ProjectViewRawSections(
            List.of(new ProjectViewRawSection("import", "path/to/file.bazelproject\n")));

    assertEquals(expectedSections, sections);
  }

  @Test
  public void shouldParseSectionsWithSpacesBetween() {
    // given
    var fileContent =
        "section: "
            + "section_included_element1 "
            + "-section_excluded_element1 "
            + "-section_excluded_element2 "
            + "-section_excluded_element3 "
            + "\n";

    // when
    var sections = ProjectViewSectionSplitter.split(fileContent);

    // then
    var expectedSections =
        new ProjectViewRawSections(
            List.of(
                new ProjectViewRawSection(
                    "section",
                    " "
                        + "section_included_element1 "
                        + "-section_excluded_element1 "
                        + "-section_excluded_element2 "
                        + "-section_excluded_element3 "
                        + "\n")));

    assertEquals(expectedSections, sections);
  }

  @Test
  public void shouldParseSectionsWithSpacesBetweenWithOnlyNewLineBefore() {
    // given
    var fileContent =
        "\n\nsection: "
            + "section_included_element1 "
            + "-section_excluded_element1 "
            + "-section_excluded_element2 "
            + "-section_excluded_element3 "
            + "\n";

    // when
    var sections = ProjectViewSectionSplitter.split(fileContent);

    // then
    var expectedSections =
        new ProjectViewRawSections(
            List.of(
                new ProjectViewRawSection(
                    "section",
                    " "
                        + "section_included_element1 "
                        + "-section_excluded_element1 "
                        + "-section_excluded_element2 "
                        + "-section_excluded_element3 "
                        + "\n")));

    assertEquals(expectedSections, sections);
  }

  @Test
  public void shouldParseSectionsWithTabsBetween() {
    // given
    var fileContent =
        "section:\n"
            + "\tsection_included_element1\n"
            + "-section_excluded_element1\n"
            + "\tsection_included_element2\n"
            + "\n";

    // when
    var sections = ProjectViewSectionSplitter.split(fileContent);

    // then
    var expectedSections =
        new ProjectViewRawSections(
            List.of(
                new ProjectViewRawSection(
                    "section",
                    "\n"
                        + "\tsection_included_element1\n"
                        + "-section_excluded_element1\n"
                        + "\tsection_included_element2\n"
                        + "\n")));

    assertEquals(expectedSections, sections);
  }

  @Test
  public void shouldParseSectionsWithOneElement() {
    // given
    var fileContent = "section: section_element\n";

    // when
    var sections = ProjectViewSectionSplitter.split(fileContent);

    // then
    var expectedSections =
        new ProjectViewRawSections(
            List.of(new ProjectViewRawSection("section", " section_element\n")));

    assertEquals(expectedSections, sections);
  }

  @Test
  public void shouldParseRegularFile() {
    // given
    var fileContent =
        "import path/to/file.bazelproject"
            + "\n"
            + "section1: "
            + "section1_included_element1 "
            + "-section1_excluded_element1 "
            + "-section1_excluded_element2 "
            + "-section1_excluded_element3 "
            + "\n"
            + "section2:\n"
            + "  section2_included_element1\n"
            + " -section2_excluded_element1\n"
            + "\tsection2_included_element2\n"
            + "\n"
            + "section3: section3_element\n"
            + "\n\n\n"
            + "sectionA:\n"
            + "  --sectionA_element_flag\n"
            + "\n"
            + "sectionb:"
            + "*sectionb_element1\n"
            + "*sectionb_element2\n"
            + "\n";

    // when
    var sections = ProjectViewSectionSplitter.split(fileContent);

    // then
    var expectedSections =
        new ProjectViewRawSections(
            List.of(
                new ProjectViewRawSection("import", "path/to/file.bazelproject\n"),
                new ProjectViewRawSection(
                    "section1",
                    " "
                        + "section1_included_element1 "
                        + "-section1_excluded_element1 "
                        + "-section1_excluded_element2 "
                        + "-section1_excluded_element3 "
                        + "\n"),
                new ProjectViewRawSection(
                    "section2",
                    "\n"
                        + "  section2_included_element1\n"
                        + " -section2_excluded_element1\n"
                        + "\tsection2_included_element2\n"
                        + "\n"),
                new ProjectViewRawSection("section3", " section3_element\n\n\n\n"),
                new ProjectViewRawSection("sectionA", "\n  --sectionA_element_flag\n\n"),
                new ProjectViewRawSection(
                    "sectionb", "*sectionb_element1\n*sectionb_element2\n\n")));

    assertEquals(expectedSections, sections);
  }
}
