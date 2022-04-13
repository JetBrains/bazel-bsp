package org.jetbrains.bsp.bazel.projectview.generator.sections

import io.kotest.matchers.shouldBe
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewJavaPathSection
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.nio.file.Paths

class ProjectViewSingletonSectionGeneratorTest {

    @Nested
    @DisplayName("ProjectViewJavaPathSectionGenerator tests")
    inner class ProjectViewJavaPathSectionGeneratorTest {

        @Test
        fun `should return null for null section`() {
            // given
            val generator = ProjectViewJavaPathSectionGenerator()
            val section = null

            // when
            val generatedString = generator.generatePrettyStringRepresentation(section)

            // then
            generatedString shouldBe null
        }

        @Test
        fun `should return pretty string for non null section`() {
            // given
            val generator = ProjectViewJavaPathSectionGenerator()
            val section = ProjectViewJavaPathSection(Paths.get("/path/to/java"))

            // when
            val generatedString = generator.generatePrettyStringRepresentation(section)

            // then
            val expectedGeneratedString = "java_path: /path/to/java"
            generatedString shouldBe expectedGeneratedString
        }
    }
}
