package org.jetbrains.bsp.bazel.projectview.generator.sections

import io.kotest.matchers.shouldBe
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBuildFlagsSection
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ProjectViewListSectionGeneratorTest {

    @Nested
    @DisplayName("ProjectViewBuildFlagsSectionGenerator tests")
    inner class ProjectViewBuildFlagsSectionGeneratorTest {
        
        @Test
        fun `should return null for null section`() {
            // given
            val section = null

            // when
            val generatedString = ProjectViewBuildFlagsSectionGenerator.generatePrettyString(section)

            // then
            generatedString shouldBe null
        }

        @Test
        fun `should return pretty string for empty section`() {
            // given
            val section = ProjectViewBuildFlagsSection(emptyList())

            // when
            val generatedString = ProjectViewBuildFlagsSectionGenerator.generatePrettyString(section)

            // then
            val expectedGeneratedString =
                """
                build_flags:
                """.trimIndent()
            generatedString shouldBe expectedGeneratedString
        }

        @Test
        fun `should return pretty string for non null section`() {
            // given
            val section = ProjectViewBuildFlagsSection(
                listOf(
                    "--build_flag1=value1",
                    "--build_flag2=value2",
                    "--build_flag3=value3",
                )
            )

            // when
            val generatedString = ProjectViewBuildFlagsSectionGenerator.generatePrettyString(section)

            // then
            val expectedGeneratedString =
                """
                build_flags:
                    --build_flag1=value1
                    --build_flag2=value2
                    --build_flag3=value3
                """.trimIndent()
            generatedString shouldBe expectedGeneratedString
        }
    }
}
