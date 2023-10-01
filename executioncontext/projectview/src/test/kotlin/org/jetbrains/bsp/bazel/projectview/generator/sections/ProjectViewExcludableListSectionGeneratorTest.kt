package org.jetbrains.bsp.bazel.projectview.generator.sections

import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import io.kotest.matchers.shouldBe
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewTargetsSection
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ProjectViewExcludableListSectionGeneratorTest {

    @Nested
    @DisplayName("ProjectViewTargetsSectionGenerator tests")
    inner class ProjectViewTargetsSectionGeneratorTest {

        @Test
        fun `should return null for null section`() {
            // given
            val section = null

            // when
            val generatedString = ProjectViewTargetsSectionGenerator.generatePrettyString(section)

            // then
            generatedString shouldBe null
        }

        @Test
        fun `should return pretty string for section with included targets`() {
            // given
            val section = ProjectViewTargetsSection(
                listOf(
                    BuildTargetIdentifier("//included_target1"),
                    BuildTargetIdentifier("//included_target2"),
                    BuildTargetIdentifier("//included_target3"),
                ), emptyList()
            )

            // when
            val generatedString = ProjectViewTargetsSectionGenerator.generatePrettyString(section)

            // then
            val expectedGeneratedString = """
                targets:
                    //included_target1
                    //included_target2
                    //included_target3
                """.trimIndent()
            generatedString shouldBe expectedGeneratedString
        }

        @Test
        fun `should return pretty string for section with excluded targets`() {
            // given
            val section = ProjectViewTargetsSection(
                emptyList(), listOf(
                    BuildTargetIdentifier("//excluded_target1"),
                    BuildTargetIdentifier("//excluded_target2"),
                )
            )

            // when
            val generatedString = ProjectViewTargetsSectionGenerator.generatePrettyString(section)

            // then
            val expectedGeneratedString = """
                targets:
                    -//excluded_target1
                    -//excluded_target2
                """.trimIndent()
            generatedString shouldBe expectedGeneratedString
        }

        @Test
        fun `should return pretty string for section with included and excluded targets`() {
            // given
            val section = ProjectViewTargetsSection(
                listOf(
                    BuildTargetIdentifier("//included_target1"),
                    BuildTargetIdentifier("//included_target2"),
                    BuildTargetIdentifier("//included_target3"),
                ), listOf(
                    BuildTargetIdentifier("//excluded_target1"),
                    BuildTargetIdentifier("//excluded_target2"),
                )
            )

            // when
            val generatedString = ProjectViewTargetsSectionGenerator.generatePrettyString(section)

            // then
            val expectedGeneratedString = """
                targets:
                    //included_target1
                    //included_target2
                    //included_target3
                    -//excluded_target1
                    -//excluded_target2
                """.trimIndent()
            generatedString shouldBe expectedGeneratedString
        }
    }
}
