package org.jetbrains.bsp.bazel.projectview.parser.splitter;

import static org.assertj.core.api.Assertions.assertThat;

import io.vavr.collection.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ProjectViewSectionSplitterTest {

  public static Stream<Arguments> data() {
    return Stream.of(
        Arguments.of(
            // given - empty
            "",
            // then
            new ProjectViewRawSections(List.of())),
        Arguments.of(
            // given - empty section
            "section:   ",
            // then
            new ProjectViewRawSections(List.of(new ProjectViewRawSection("section", "   ")))),
        Arguments.of(
            // given - no colon
            "import path/to/file.bazelproject\n",
            // then
            new ProjectViewRawSections(
                List.of(new ProjectViewRawSection("import", "path/to/file.bazelproject\n")))),
        Arguments.of(
            // given - new line in front
            "\nimport path/to/file.bazelproject\n",
            // then
            new ProjectViewRawSections(
                List.of(new ProjectViewRawSection("import", "path/to/file.bazelproject\n")))),
        Arguments.of(
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
                            + "\n")))),
        Arguments.of(
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
                            + "\n")))),
        Arguments.of(
            // given - multiple elements with whitespaces in front and new lines
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
                            + "\n")))),
        Arguments.of(
            // given - single element
            "section: section_element\n",
            // then
            new ProjectViewRawSections(
                List.of(new ProjectViewRawSection("section", " section_element\n")))),
        Arguments.of(
            // given - element with dots and colon
            "section: 1.2.3.4:8080\n",
            // then
            new ProjectViewRawSections(
                List.of(new ProjectViewRawSection("section", " 1.2.3.4:8080\n")))),
        Arguments.of(
            // given - comment
            "  # comment",
            // then
            new ProjectViewRawSections(List.of())),
        Arguments.of(
            // given - commented out section
            "#section: value\n",
            // then
            new ProjectViewRawSections(List.of())),
        Arguments.of(
            // given - multiple elements with  new lines and comments
            " # comment 1\n\n"
                + "section:\n"
                + "\tsection_included_element1\n"
                + "#comment2\n"
                + "-section_excluded_element1 # comment 3 \n"
                + "\tsection_included_element2\n"
                + "\t #comment 4\n\n"
                + "\n",
            // then
            new ProjectViewRawSections(
                List.of(
                    new ProjectViewRawSection(
                        "section",
                        "\n"
                            + "\tsection_included_element1\n"
                            + "\n"
                            + "-section_excluded_element1 \n"
                            + "\tsection_included_element2\n"
                            + "\t \n\n"
                            + "\n")))),
        Arguments.of(
            // given - multiple elements with whitespaces in front and new lines
            "section:\n"
                + "\tsection_included_element1\n\n\n"
                + "-section_excluded_element1\n"
                + "\tsection_included_element2\n"
                + "\n",
            // then
            new ProjectViewRawSections(
                List.of(
                    new ProjectViewRawSection(
                        "section",
                        "\n"
                            + "\tsection_included_element1\n\n\n"
                            + "-section_excluded_element1\n"
                            + "\tsection_included_element2\n"
                            + "\n")))),
        Arguments.of(
            // given - full file
            "import path/to/file.bazelproject"
                + "\n"
                + "# some comment\n"
                + "section1: "
                + "section1_included_element1 "
                + "-section1_excluded_element1 "
                + "-section1_excluded_element2 "
                + "-section1_excluded_element3 "
                + "\n"
                + "section2:\n"
                + "  section2_included_element1\n"
                + " -section2_excluded_element1\n"
                + " # commented_out_target\n"
                + "\tsection2_included_element2\n"
                + "\n"
                + "section3: section3_element\n"
                + "\n\n\n"
                + "sectionA:\n"
                + "  --sectionA_element_flag\n"
                + "# commented_out_section: comment\n\n"
                + "\n"
                + "sectionb:"
                + "*sectionb_element1\n"
                + "*sectionb_element2\n"
                + "\n",
            // then
            new ProjectViewRawSections(
                List.of(
                    new ProjectViewRawSection("import", "path/to/file.bazelproject\n\n"),
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
                            + " \n"
                            + "\tsection2_included_element2\n"
                            + "\n"),
                    new ProjectViewRawSection("section3", " section3_element\n\n\n\n"),
                    new ProjectViewRawSection("sectionA", "\n  --sectionA_element_flag\n\n\n\n"),
                    new ProjectViewRawSection(
                        "sectionb", "*sectionb_element1\n*sectionb_element2\n\n")))));
  }

  @MethodSource("data")
  @ParameterizedTest(name = "{index}: ProjectViewSectionSplitter.split({0}) should equals {1}")
  public void shouldSplitContent(String fileContent, ProjectViewRawSections expectedSections) {
    // when
    var sections = ProjectViewSectionSplitter.getRawSectionsForFileContent(fileContent);

    // then
    assertThat(sections).isEqualTo(expectedSections);
  }
}
