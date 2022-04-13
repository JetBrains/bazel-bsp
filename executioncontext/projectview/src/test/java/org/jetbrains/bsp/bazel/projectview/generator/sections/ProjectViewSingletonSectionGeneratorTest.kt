package org.jetbrains.bsp.bazel.projectview.generator.sections

import io.kotest.matchers.shouldBe
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewJavaPathSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewSingletonSection
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.nio.file.Paths

class ProjectViewSingletonSectionGeneratorTest<T : ProjectViewSingletonSection<*>> {

    @MethodSource("data")
    @ParameterizedTest
    fun `should return null for null section`(
        generator: ProjectViewSingletonSectionGenerator<T>,
        sectionConstructor: (String) -> T,
        expectedGeneratedStringConstructor: (String) -> String,
    ) {
        // given

        // when
        val generatedString = generator.generatePrettyStringRepresentation(null)

        // then
        generatedString shouldBe null
    }

    @MethodSource("data")
    @ParameterizedTest
    fun `should return pretty string for non null section`(
        generator: ProjectViewSingletonSectionGenerator<T>,
        sectionConstructor: (String) -> T,
        expectedGeneratedStringConstructor: (String) -> String,
    ) {
        // given
        val section = sectionConstructor("value")

        // when
        val generatedString = generator.generatePrettyStringRepresentation(section)

        // then
        val expectedGeneratedString = expectedGeneratedStringConstructor("value")
        generatedString shouldBe expectedGeneratedString
    }

    companion object {

        @JvmStatic
        fun data() = listOf(javaPathSectionArguments())

        private fun javaPathSectionArguments(): Arguments {
            // given
            val generator = ProjectViewJavaPathSectionGenerator()
            val sectionConstructor = { seed: String -> ProjectViewJavaPathSection(Paths.get("/path/to/$seed")) }
            val expectedGeneratedStringConstructor =
                { seed: String -> "${ProjectViewJavaPathSection.SECTION_NAME}: /path/to/$seed" }

            return Arguments.of(generator, sectionConstructor, expectedGeneratedStringConstructor)
        }
    }
}
