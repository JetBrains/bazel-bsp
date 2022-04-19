package org.jetbrains.bsp.bazel.projectview.generator.sections

import io.kotest.matchers.shouldBe
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewJavaPathSection
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.nio.file.Paths

class ProjectViewSingletonSectionGeneratorTest {

    @Nested
    @DisplayName("ProjectViewJavaPathSectionGenerator tests")
    inner class ProjectViewJavaPathSectionGeneratorTest {

        private lateinit var generator: ProjectViewJavaPathSectionGenerator

        @BeforeEach
        fun beforeEach() {
            // given
            this.generator = ProjectViewJavaPathSectionGenerator()
        }

        @Test
        fun `should return null for null section`() {
            // given
            val section = null

            // when
            val generator = ProjectViewJavaPathSectionGenerator()
            val generatedString = generator.generatePrettyString(section)

            // then
            generatedString shouldBe null
        }

        @Test
        fun `should return pretty string for non null section`() {
            // given
            val section = ProjectViewJavaPathSection(Paths.get("/path/to/java"))

            // when
            val generator = ProjectViewJavaPathSectionGenerator()
            val generatedString = generator.generatePrettyString(section)

            // then
            val expectedGeneratedString = "java_path: /path/to/java"
            generatedString shouldBe expectedGeneratedString
        }
    }
}