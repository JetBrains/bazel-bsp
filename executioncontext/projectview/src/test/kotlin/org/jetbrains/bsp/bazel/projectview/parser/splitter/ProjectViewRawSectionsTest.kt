package org.jetbrains.bsp.bazel.projectview.parser.splitter

import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ProjectViewRawSectionsTest {

    @Nested
    @DisplayName("fun getLastSectionWithName(sectionName: String): ProjectViewRawSection? tests")
    inner class GetLastSectionWithNameTest {

        @Test
        fun `should return null for empty sections`() {
            // given
            val sections = emptyList<ProjectViewRawSection>()
            val projectViewRawSections = ProjectViewRawSections(sections)

            // when
            val sectionWithName = projectViewRawSections.getLastSectionWithName("doesntexist")

            // then
            sectionWithName shouldBe null
        }

        @Test
        fun `should return null if section doesnt exist`() {
            // given
            val sections = listOf(
                ProjectViewRawSection("name1", "body1"),
                ProjectViewRawSection("name2", "body2"),
                ProjectViewRawSection("name3", "body3")
            )
            val projectViewRawSections = ProjectViewRawSections(sections)

            // when
            val sectionWithName = projectViewRawSections.getLastSectionWithName("doesntexist")

            // then
            sectionWithName shouldBe null
        }

        @Test
        fun `should return only section with name`() {
            // given
            val sections = listOf(
                ProjectViewRawSection("name1", "body1"),
                ProjectViewRawSection("name2", "body2"),
                ProjectViewRawSection("name3", "body3")
            )
            val projectViewRawSections = ProjectViewRawSections(sections)

            // when
            val sectionWithName = projectViewRawSections.getLastSectionWithName("name1")

            // then
            val expectedSection = ProjectViewRawSection("name1", "body1")
            sectionWithName shouldBe expectedSection
        }

        @Test
        fun `should return last section with name`() {
            // given
            val sections = listOf(
                ProjectViewRawSection("name1", "body1.1"),
                ProjectViewRawSection("name2", "body2"),
                ProjectViewRawSection("name1", "body1.2"),
                ProjectViewRawSection("name1", "body1.3"),
                ProjectViewRawSection("name3", "body3")
            )
            val projectViewRawSections = ProjectViewRawSections(sections)

            // when
            val sectionWithName = projectViewRawSections.getLastSectionWithName("name1")

            // then
            val expectedSection = ProjectViewRawSection("name1", "body1.3")
            sectionWithName shouldBe expectedSection
        }
    }

    @Nested
    @DisplayName("fun getAllWithName(sectionName: String): List<ProjectViewRawSection> tests")
    inner class GetAllWithNameTest {

        @Test
        fun `should return empty list for empty sections`() {
            // given
            val sections = emptyList<ProjectViewRawSection>()
            val projectViewRawSections = ProjectViewRawSections(sections)

            // when
            val sectionsWithName = projectViewRawSections.getAllWithName("doesntexist")

            // then
            sectionsWithName shouldBe emptyList()
        }

        @Test
        fun shouldReturnEmptyListIfSectionDoesntExist() {
            // given
            val sections = listOf(
                ProjectViewRawSection("name1", "body1"),
                ProjectViewRawSection("name2", "body2"),
                ProjectViewRawSection("name3", "body3")
            )
            val projectViewRawSections = ProjectViewRawSections(sections)

            // when
            val sectionsWithName = projectViewRawSections.getAllWithName("doesntexist")

            // then
            sectionsWithName shouldBe emptyList()
        }

        @Test
        fun shouldReturnAllSectionsWithName() {
            // given
            val sections = listOf(
                ProjectViewRawSection("name1", "body1.1"),
                ProjectViewRawSection("name2", "body2"),
                ProjectViewRawSection("name1", "body1.2"),
                ProjectViewRawSection("name1", "body1.3"),
                ProjectViewRawSection("name3", "body3")
            )
            val projectViewRawSections = ProjectViewRawSections(sections)

            // when
            val sectionsWithName = projectViewRawSections.getAllWithName("name1")

            // then
            val expectedSections = listOf(
                ProjectViewRawSection("name1", "body1.1"),
                ProjectViewRawSection("name1", "body1.3"),
                ProjectViewRawSection("name1", "body1.2")
            )
            sectionsWithName shouldContainExactlyInAnyOrder expectedSections
        }
    }
}
