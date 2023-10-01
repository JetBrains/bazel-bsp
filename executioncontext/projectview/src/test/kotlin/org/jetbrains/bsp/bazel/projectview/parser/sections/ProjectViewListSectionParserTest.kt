package org.jetbrains.bsp.bazel.projectview.parser.sections

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBuildFlagsSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewListSection
import org.jetbrains.bsp.bazel.projectview.parser.splitter.ProjectViewRawSection
import org.jetbrains.bsp.bazel.projectview.parser.splitter.ProjectViewRawSections
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class ProjectViewListSectionParserTest {

    companion object {
        @JvmStatic
        fun data(): List<Arguments> =
            listOf(buildFlagsSectionArguments())

        private fun buildFlagsSectionArguments(): Arguments {
            val parser = ProjectViewBuildFlagsSectionParser
            val rawElementConstructor = { seed: String -> "--flag_$seed=dummy_value" }
            val elementMapper = { x: String -> x }
            val sectionMapper = { values: List<String> ->
                ProjectViewBuildFlagsSection(values)
            }
            val sectionConstructor =
                createSectionConstructor(sectionMapper, rawElementConstructor, elementMapper)
            val sectionName = parser.sectionName
            return Arguments.of(parser, rawElementConstructor, sectionConstructor, sectionName)
        }

        private fun <V, T : ProjectViewListSection<V>?> createSectionConstructor(
            sectionMapper: (List<V>) -> T,
            rawIncludedElementConstructor: (String) -> String,
            elementMapper: (String) -> V,
        ): (List<String>) -> T =
            { includedElements: List<String> ->
                sectionMapper(
                    mapElements(
                        rawIncludedElementConstructor,
                        elementMapper,
                        includedElements
                    )
                )
            }

        private fun <V> mapElements(
            rawIncludedElementConstructor: (String) -> String,
            elementMapper: (String) -> V,
            rawElements: List<String>,
        ): List<V> = rawElements
            .map(rawIncludedElementConstructor)
            .map(elementMapper)
    }

    @Nested
    @DisplayName("ProjectViewListSection parse(rawSection) tests")
    internal inner class ParseRawSectionTest<V, T : ProjectViewListSection<V>> {
        @MethodSource("org.jetbrains.bsp.bazel.projectview.parser.sections.ProjectViewListSectionParserTest#data")
        @ParameterizedTest
        fun shouldReturnFailureForWrongSectionName(
            parser: ProjectViewListSectionParser<V, T>,
            rawElementConstructor: (String) -> String,
            sectionConstructor: (List<String>) -> T?,
            sectionName: String,
        ) {
            // given
            val rawSection = ProjectViewRawSection("wrongsection", "bodyelement")

            // when
            val exception = shouldThrow<IllegalArgumentException> {
                parser.parse(rawSection)
            }

            // then
            exception.message shouldBe "Project view parsing failed. Expected '$sectionName' section name, got 'wrongsection'."
        }

        @MethodSource("org.jetbrains.bsp.bazel.projectview.parser.sections.ProjectViewListSectionParserTest#data")
        @ParameterizedTest
        fun shouldParseEmptySectionBody(
            parser: ProjectViewListSectionParser<V, T>,
            rawElementConstructor: (String) -> String,
            sectionConstructor: (List<String>) -> T?,
            sectionName: String,
        ) {
            // given
            val rawSection = ProjectViewRawSection(
                sectionName, ""
            )

            // when
            val section = parser.parse(rawSection)

            // then
            section shouldBe null
        }

        @MethodSource("org.jetbrains.bsp.bazel.projectview.parser.sections.ProjectViewListSectionParserTest#data")
        @ParameterizedTest
        fun shouldParseElements(
            parser: ProjectViewListSectionParser<V, T>,
            rawElementConstructor: (String) -> String,
            sectionConstructor: (List<String>) -> T,
            sectionName: String,
        ) {
            // given
            val rawSection = ProjectViewRawSection(
                sectionName,
                """
                    |  ${rawElementConstructor("element1")}
                    |  ${rawElementConstructor("element2")}
                    |  ${rawElementConstructor("element3")}
                    
                """.trimMargin()
            )

            // when
            val section = parser.parse(rawSection)

            // then
            val expectedSection = sectionConstructor(listOf("element1", "element2", "element3"))
            section shouldBe expectedSection
        }

        // ProjectViewListSection parse(rawSections)
        @MethodSource("org.jetbrains.bsp.bazel.projectview.parser.sections.ProjectViewListSectionParserTest#data")
        @ParameterizedTest
        fun shouldReturnEmptySectionIfThereIsNoSectionForParseWithoutDefault(
            parser: ProjectViewListSectionParser<V, T>,
            rawElementConstructor: (String) -> String,
            sectionConstructor: (List<String>) -> T,
            sectionName: String,
        ) {
            // given
            val rawSection1 = ProjectViewRawSection(
                "another_section1", "  -bodyelement1.1\n\tbodyelement1.2\n-bodyelement1.3\n\n"
            )
            val rawSection2 = ProjectViewRawSection("another_section2", "bodyelement2.1")
            val rawSection3 = ProjectViewRawSection("another_section3", "-bodyelement3.1")
            val rawSections = ProjectViewRawSections(listOf(rawSection1, rawSection2, rawSection3))

            // when
            val section = parser.parse(rawSections)

            // then
            section shouldBe null
        }

        @MethodSource("org.jetbrains.bsp.bazel.projectview.parser.sections.ProjectViewListSectionParserTest#data")
        @ParameterizedTest
        fun shouldParseAllSectionElementsFromListWithoutDefault(
            parser: ProjectViewListSectionParser<V, T>,
            rawElementConstructor: (String) -> String,
            sectionConstructor: (List<String>) -> T,
            sectionName: String,
        ) {
            // given
            val rawSection1 = ProjectViewRawSection("another_section1", "bodyelement1")
            val rawSection2 = ProjectViewRawSection(
                sectionName,
                """ ${rawElementConstructor("element1")}
${rawElementConstructor("element2")}"""
            )
            val rawSection3 = ProjectViewRawSection("another_section2", "-bodyelement2")
            val rawSection4 = ProjectViewRawSection(
                sectionName, "\n\t" + rawElementConstructor("element3") + "\n\n\n"
            )
            val rawSections =
                ProjectViewRawSections(listOf(rawSection1, rawSection2, rawSection3, rawSection4))

            // when
            val section = parser.parse(rawSections)

            // then
            val expectedSection = sectionConstructor(listOf("element1", "element2", "element3"))
            section shouldBe expectedSection
        }
    }
}
