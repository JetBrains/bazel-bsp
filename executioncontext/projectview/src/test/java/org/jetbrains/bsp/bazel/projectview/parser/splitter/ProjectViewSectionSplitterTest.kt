package org.jetbrains.bsp.bazel.projectview.parser.splitter

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class ProjectViewSectionSplitterTest {

    @Test
    fun `should return empty for sections for empty string`() {
        // given
        val fileContent = ""

        // when
        val sections = ProjectViewSectionSplitter.getRawSectionsForFileContent(fileContent)

        // then
        val expectedSections = ProjectViewRawSections(emptyList())
        sections shouldBe expectedSections
    }

    @Test
    fun `should return empty section`() {
        // given
        val fileContent = "section:   "

        // when
        val sections = ProjectViewSectionSplitter.getRawSectionsForFileContent(fileContent)

        // then
        val expectedSections = ProjectViewRawSections(
            listOf(
                ProjectViewRawSection("section", "   ")
            )
        )
        sections shouldBe expectedSections
    }

    @Test
    fun `should return section (import) with no colon`() {
        // given
        val fileContent = "import path/to/file.bazelproject\n"

        // when
        val sections = ProjectViewSectionSplitter.getRawSectionsForFileContent(fileContent)

        // then
        val expectedSections = ProjectViewRawSections(
            listOf(
                ProjectViewRawSection("import", "path/to/file.bazelproject\n")
            )
        )
        sections shouldBe expectedSections
    }

    @Test
    fun `should return section with new line in front`() {
        // given
        val fileContent = "\nimport path/to/file.bazelproject\n"

        // when
        val sections = ProjectViewSectionSplitter.getRawSectionsForFileContent(fileContent)

        // then
        val expectedSections = ProjectViewRawSections(
            listOf(
                ProjectViewRawSection("import", "path/to/file.bazelproject\n")
            )
        )
        sections shouldBe expectedSections
    }

    @Test
    fun `should return section with multiple elements`() {
        // given
        val fileContent =
            "section: section_included_element1 -section_excluded_element1 -section_excluded_element2 -section_excluded_element3 \n"

        // when
        val sections = ProjectViewSectionSplitter.getRawSectionsForFileContent(fileContent)

        // then
        val expectedSections = ProjectViewRawSections(
            listOf(
                ProjectViewRawSection(
                    "section",
                    " section_included_element1 -section_excluded_element1 -section_excluded_element2 -section_excluded_element3 \n"
                )
            )
        )
        sections shouldBe expectedSections
    }

    @Test
    fun `should return section with multiple elements with new lines in front`() {
        // given
        val fileContent =
            "\n\nsection: section_included_element1 -section_excluded_element1 -section_excluded_element2 -section_excluded_element3 \n"

        // when
        val sections = ProjectViewSectionSplitter.getRawSectionsForFileContent(fileContent)

        // then
        val expectedSections = ProjectViewRawSections(
            listOf(
                ProjectViewRawSection(
                    "section",
                    " section_included_element1 -section_excluded_element1 -section_excluded_element2 -section_excluded_element3 \n"
                )
            )
        )
        sections shouldBe expectedSections
    }

    @Test
    fun `should return section with multiple elements with whitespaces in front`() {
        // given
        val fileContent =
            """
            |section:
            |${'\t'}section_included_element1
            |   -section_excluded_element1
            |  section_included_element2
            """.trimMargin()

        // when
        val sections = ProjectViewSectionSplitter.getRawSectionsForFileContent(fileContent)

        // then
        val expectedSections = ProjectViewRawSections(
            listOf(
                ProjectViewRawSection(
                    "section",
                    """
                    |
                    |${'\t'}section_included_element1
                    |   -section_excluded_element1
                    |  section_included_element2
                    """.trimMargin()
                )
            )
        )
        sections shouldBe expectedSections
    }

    @Test
    fun `should return section with single element`() {
        // given
        val fileContent = "section: section_element\n"

        // when
        val sections = ProjectViewSectionSplitter.getRawSectionsForFileContent(fileContent)

        // then
        val expectedSection = ProjectViewRawSections(
            listOf(ProjectViewRawSection("section", " section_element\n"))
        )
        sections shouldBe expectedSection
    }

    @Test
    fun `should return section with single element with dots and colon`() {
        // given
        val fileContent = "section: 1.2.3.4:8080\n"

        // when
        val sections = ProjectViewSectionSplitter.getRawSectionsForFileContent(fileContent)

        // then
        val expectedSection = ProjectViewRawSections(
            listOf(ProjectViewRawSection("section", " 1.2.3.4:8080\n"))
        )
        sections shouldBe expectedSection
    }

    @Test
    fun `should return empty for comment`() {
        // given
        val fileContent = "  # comment"

        // when
        val sections = ProjectViewSectionSplitter.getRawSectionsForFileContent(fileContent)

        // then
        val expectedSection = ProjectViewRawSections(emptyList())
        sections shouldBe expectedSection
    }

    @Test
    fun `should return empty for comment out section`() {
        // given
        val fileContent = "#section: value\n"

        // when
        val sections = ProjectViewSectionSplitter.getRawSectionsForFileContent(fileContent)

        // then
        val expectedSection = ProjectViewRawSections(emptyList())
        sections shouldBe expectedSection
    }

    @Test
    fun `should return section with multiple elements with new lines and comments`() {
        // given
        val fileContent =
            """
            |# comment 1
            |section:
	        |${'\t'}section_included_element1
            |   #comment2
            |  -section_excluded_element1 # comment 3 
	        | section_included_element2
	        |       #comment 4
            """.trimMargin()

        // when
        val sections = ProjectViewSectionSplitter.getRawSectionsForFileContent(fileContent)

        // then
        val expectedSection = ProjectViewRawSections(
            listOf(
                ProjectViewRawSection(
                    "section",
                    """
                    |
                    |${'\t'}section_included_element1
                    |   
                    |  -section_excluded_element1 
                    | section_included_element2
                    |       
                    |
                    """.trimMargin()
                )
            )
        )
        sections shouldBe expectedSection
    }

    @Test
    fun `should return sections for multiple sections with comments`() {
        // given
        val fileContent =
            """
            |import path/to/file.bazelproject
            |# some comment
            |section1: section1_included_element1 -section1_excluded_element1 -section1_excluded_element2 -section1_excluded_element3 
            |
            |section2:
            |${'\t'}section2_included_element1
            |  -section2_excluded_element1
            |   # commented_out_target
            |       section2_included_element2
            |
            |section3: section3_element
            |
            |
            |
            |sectionA:
            |${'\t'}--sectionA_element_flag
            |# commented_out_section: comment
            |
            |
            |sectionb:
            |   *sectionb_element1
            |   *sectionb_element2
            |
            """.trimMargin()

        // when
        val sections = ProjectViewSectionSplitter.getRawSectionsForFileContent(fileContent)

        // then
        val expectedSection = ProjectViewRawSections(
            listOf(
                ProjectViewRawSection("import", "path/to/file.bazelproject\n\n"),
                ProjectViewRawSection(
                    "section1",
                    " section1_included_element1 -section1_excluded_element1 -section1_excluded_element2 -section1_excluded_element3 \n\n"
                ),
                ProjectViewRawSection(
                    "section2",
                    """
                    |
                    |${'\t'}section2_included_element1
                    |  -section2_excluded_element1
                    |   
                    |       section2_included_element2
                    |
                    |
                    """.trimMargin()
                ),
                ProjectViewRawSection("section3", " section3_element\n\n\n\n"),
                ProjectViewRawSection("sectionA",
                    """
                    |
                    |${'\t'}--sectionA_element_flag
                    |
                    |
                    |
                    |
                    """.trimMargin()),
                ProjectViewRawSection(
                    "sectionb",
                    """
                    |
                    |   *sectionb_element1
                    |   *sectionb_element2
                    |
                    """.trimMargin()
                )
            )
        )
        sections shouldBe expectedSection
    }
}
