package org.jetbrains.bsp.bazel.projectview.generator.sections

import io.kotest.matchers.shouldBe
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBazelPathSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewDebuggerAddressSection
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
            val section = null

            // when
            val generatedString = ProjectViewJavaPathSectionGenerator.generatePrettyString(section)

            // then
            generatedString shouldBe null
        }

        @Test
        fun `should return pretty string for non null section`() {
            // given
            val section = ProjectViewJavaPathSection(Paths.get("/path/to/java"))

            // when
            val generatedString = ProjectViewJavaPathSectionGenerator.generatePrettyString(section)

            // then
            val expectedGeneratedString = "java_path: /path/to/java"
            generatedString shouldBe expectedGeneratedString
        }
    }

    @Nested
    @DisplayName("ProjectViewDebuggerAddressSectionGenerator tests")
    inner class ProjectViewDebuggerAddressSectionGeneratorTest {

        @Test
        fun `should return null for null section`() {
            // given
            val section = null

            // when
            val generatedString = ProjectViewDebuggerAddressSectionGenerator.generatePrettyString(section)

            // then
            generatedString shouldBe null
        }

        @Test
        fun `should return pretty string for non null section`() {
            // given
            val section = ProjectViewDebuggerAddressSection("localhost:8000")

            // when
            val generatedString = ProjectViewDebuggerAddressSectionGenerator.generatePrettyString(section)

            // then
            val expectedGeneratedString = "debugger_address: localhost:8000"
            generatedString shouldBe expectedGeneratedString
        }
    }

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
}
