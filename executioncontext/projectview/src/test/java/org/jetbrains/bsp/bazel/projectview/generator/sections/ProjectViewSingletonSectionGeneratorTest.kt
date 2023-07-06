package org.jetbrains.bsp.bazel.projectview.generator.sections

import io.kotest.matchers.shouldBe
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBazelPathSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewImportDepthSection
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.nio.file.Paths

class ProjectViewSingletonSectionGeneratorTest {

    @Nested
    @DisplayName("ProjectViewBazelPathSectionGenerator tests")
    inner class ProjectViewBazelPathSectionGeneratorTest {

        @Test
        fun `should return null for null section`() {
            // given
            val section = null

            // when
            val generatedString = ProjectViewBazelPathSectionGenerator.generatePrettyString(section)

            // then
            generatedString shouldBe null
        }

        @Test
        fun `should return pretty string for non null section`() {
            // given
            val section = ProjectViewBazelPathSection(Paths.get("/path/to/bazel"))

            // when
            val generatedString = ProjectViewBazelPathSectionGenerator.generatePrettyString(section)

            // then
            val expectedGeneratedString = "bazel_path: /path/to/bazel"
            generatedString shouldBe expectedGeneratedString
        }
    }

    @Nested
    @DisplayName("ProjectViewImportDepthSectionGenerator tests")
    inner class ProjectViewImportDepthSectionGeneratorTest {

        @Test
        fun `should return null for null section`() {
            // given
            val section = null

            // when
            val generatedString = ProjectViewImportDepthSectionGenerator.generatePrettyString(section)

            // then
            generatedString shouldBe null
        }

        @Test
        fun `should return pretty string for non null section`() {
            // given
            val section = ProjectViewImportDepthSection(1)

            // when
            val generatedString = ProjectViewImportDepthSectionGenerator.generatePrettyString(section)

            // then
            val expectedGeneratedString = "import_depth: 1"
            generatedString shouldBe expectedGeneratedString
        }
    }
}
