package org.jetbrains.bsp.bazel.projectview.generator.sections

import io.kotest.matchers.shouldBe
import io.vavr.collection.List
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
            val generator = ProjectViewBuildFlagsSectionGenerator()
            val section = null

            // when
            val generatedString = generator.generatePrettyString(section)

            // then
            generatedString shouldBe null
        }

        @Test
        fun `should return pretty string for non null section`() {
            // given
            val generator = ProjectViewBuildFlagsSectionGenerator()
            val section = ProjectViewBuildFlagsSection(
                List.of(
                    "--build_flag1=value1",
                    "--build_flag2=value2",
                    "--build_flag3=value3",
                )
            )

            // when
            val generatedString = generator.generatePrettyString(section)

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
