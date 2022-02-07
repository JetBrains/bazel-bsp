package org.jetbrains.bsp.bazel.projectview.parser.splitter;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class ProjectViewSectionSplitterTest {

  private final String fileContent;
  private final ProjectViewRawSections expectedSections;

  public ProjectViewSectionSplitterTest(
      String fileContent, ProjectViewRawSections expectedSections) {
    this.fileContent = fileContent;
    this.expectedSections = expectedSections;
  }

  @Parameters(name = "{index}: ProjectViewSectionSplitter.split({0}) should equals {1}")
  public static Collection<Object[]> data() {
    return List.of(
        new Object[][] {
          {
            // given - empty
            "",
            // then
            new ProjectViewRawSections(List.of())
          },
          {
            // given - no colon
            "import path/to/file.bazelproject\n",
            // then
            new ProjectViewRawSections(
                List.of(new ProjectViewRawSection("import", "path/to/file.bazelproject\n")))
          },
          {
            // given - new line in front
            "\nimport path/to/file.bazelproject\n",
            // then
            new ProjectViewRawSections(
                List.of(new ProjectViewRawSection("import", "path/to/file.bazelproject\n")))
          },
          {
            // given - multiple elements
            "section: "
                + "section_included_element1 "
                + "-section_excluded_element1 "
                + "-section_excluded_element2 "
                + "-section_excluded_element3 "
                + "\n",
            // then
            new ProjectViewRawSections(
                List.of(
                    new ProjectViewRawSection(
                        "section",
                        " "
                            + "section_included_element1 "
                            + "-section_excluded_element1 "
                            + "-section_excluded_element2 "
                            + "-section_excluded_element3 "
                            + "\n")))
          },
          {
            // given - new line in front and multiple elements
            "\n\nsection: "
                + "section_included_element1 "
                + "-section_excluded_element1 "
                + "-section_excluded_element2 "
                + "-section_excluded_element3 "
                + "\n",
            // then
            new ProjectViewRawSections(
                List.of(
                    new ProjectViewRawSection(
                        "section",
                        " "
                            + "section_included_element1 "
                            + "-section_excluded_element1 "
                            + "-section_excluded_element2 "
                            + "-section_excluded_element3 "
                            + "\n")))
          },
          {
            // given - multiple elements with whitespaces in front
            "section:\n"
                + "\tsection_included_element1\n"
                + "-section_excluded_element1\n"
                + "\tsection_included_element2\n"
                + "\n",
            // then
            new ProjectViewRawSections(
                List.of(
                    new ProjectViewRawSection(
                        "section",
                        "\n"
                            + "\tsection_included_element1\n"
                            + "-section_excluded_element1\n"
                            + "\tsection_included_element2\n"
                            + "\n")))
          },
          {
            // given - single element
            "section: section_element\n",
            // then
            new ProjectViewRawSections(
                List.of(new ProjectViewRawSection("section", " section_element\n")))
          },
          {
            // given - emelent with dots and colon
            "section: 1.2.3.4:8080\n",
            // then
            new ProjectViewRawSections(
                List.of(new ProjectViewRawSection("section", " 1.2.3.4:8080\n")))
          },
          {
            // given - full file
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
                + "\n",
            // then
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
                        "sectionb", "*sectionb_element1\n*sectionb_element2\n\n")))
          }
        });
  }

  @Test
  public void shouldParseEmptyFile() {
    // when
    var sections = ProjectViewSectionSplitter.split(fileContent);

    // then
    assertEquals(expectedSections, sections);
  }
}
