package org.jetbrains.bsp.bazel.projectview.parser.sections

import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBuildFlagsSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewListSection
import org.jetbrains.bsp.bazel.projectview.parser.splitter.ProjectViewRawSection
import org.jetbrains.bsp.bazel.projectview.parser.splitter.ProjectViewRawSections
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.function.Function
import java.util.stream.Collectors
import java.util.stream.Stream

class ProjectViewListSectionParserTest {

    companion object {
        @JvmStatic
        fun data(): Stream<Arguments> {
            return Stream.of(buildFlagsSectionArguments())
        }

        private fun buildFlagsSectionArguments(): Arguments {
            val parser = ProjectViewBuildFlagsSectionParser
            val rawElementConstructor = Function { seed: String -> "--flag_$seed=dummy_value" }
            val elementMapper = Function { x: String -> x }
            val sectionMapper = Function { values: List<String> ->
                ProjectViewBuildFlagsSection(values)
            }
            val sectionConstructor =
                createSectionConstructor(sectionMapper, rawElementConstructor, elementMapper)
            val sectionName = parser.sectionName
            return Arguments.of(parser, rawElementConstructor, sectionConstructor, sectionName)
        }

        private fun <V, T : ProjectViewListSection<V>?> createSectionConstructor(
            sectionMapper: Function<List<V>, T>,
            rawIncludedElementConstructor: Function<String, String>,
            elementMapper: Function<String, V>,
        ): Function<List<String>, T> {
            return Function { includedElements: List<String> ->
                sectionMapper.apply(
                    mapElements(
                        rawIncludedElementConstructor,
                        elementMapper,
                        includedElements
                    )
                )
            }
        }

        private fun <V> mapElements(
            rawIncludedElementConstructor: Function<String, String>,
            elementMapper: Function<String, V>,
            rawElements: List<String>,
        ): List<V> {
            return rawElements.stream()
                .map(rawIncludedElementConstructor)
                .map(elementMapper)
                .collect(Collectors.toList())
        }
    }

    @Nested
    @DisplayName("ProjectViewListSection parse(rawSection) tests")
    internal inner class ParseRawSectionTest<V, T : ProjectViewListSection<V>> {
        @MethodSource("org.jetbrains.bsp.bazel.projectview.parser.sections.ProjectViewListSectionParserTest#data")
        @ParameterizedTest
        fun shouldReturnFailureForWrongSectionName(
            parser: ProjectViewListSectionParser<V, T>,
            rawElementConstructor: Function<String?, String?>?,
            sectionConstructor: Function<List<String?>?, T>?,
            sectionName: String,
        ) {
            // given
            val rawSection = ProjectViewRawSection("wrongsection", "bodyelement")

            // when
            val sectionTry = parser.parse(rawSection)

            // then
            assertTrue(sectionTry.isFailure)
            assertEquals(
                "Project view parsing failed! Expected '"
                    + sectionName
                    + "' section name, got 'wrongsection'!", sectionTry.exceptionOrNull()!!.message
            )
        }

        @MethodSource("org.jetbrains.bsp.bazel.projectview.parser.sections.ProjectViewListSectionParserTest#data")
        @ParameterizedTest
        fun shouldParseEmptySectionBody(
            parser: ProjectViewListSectionParser<V, T>,
            rawElementConstructor: Function<String?, String?>?,
            sectionConstructor: Function<List<String?>?, T>?,
            sectionName: String?,
        ) {
            // given
            val rawSection = ProjectViewRawSection(
                sectionName!!, ""
            )

            // when
            val sectionTry = parser.parse(rawSection)

            // then
            assertTrue(sectionTry.isSuccess)
            assertTrue(sectionTry.getOrNull() == null)
        }

        @MethodSource("org.jetbrains.bsp.bazel.projectview.parser.sections.ProjectViewListSectionParserTest#data")
        @ParameterizedTest
        fun shouldParseElements(
            parser: ProjectViewListSectionParser<V, T>,
            rawElementConstructor: Function<String?, String>,
            sectionConstructor: Function<List<String?>?, T>,
            sectionName: String?,
        ) {
            // given
            val rawSection = ProjectViewRawSection(
                sectionName!!,
                "  "
                    + rawElementConstructor.apply("element1")
                    + "\n\t"
                    + rawElementConstructor.apply("element2")
                    + "\n"
                    + rawElementConstructor.apply("element3")
                    + "\n\n"
            )

            // when
            val sectionTry = parser.parse(rawSection)

            // then
            assertTrue(sectionTry.isSuccess)
            val expectedSection = sectionConstructor.apply(listOf("element1", "element2", "element3"))
            assertEquals(expectedSection, sectionTry.getOrNull())
        }

        // ProjectViewListSection parse(rawSections)
        @MethodSource("org.jetbrains.bsp.bazel.projectview.parser.sections.ProjectViewListSectionParserTest#data")
        @ParameterizedTest
        fun shouldReturnEmptySectionIfThereIsNoSectionForParseWithoutDefault(
            parser: ProjectViewListSectionParser<V, T>,
            rawElementConstructor: Function<String?, String?>?,
            sectionConstructor: Function<List<String?>?, T>?,
            sectionName: String?,
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
            assertTrue(section == null)
        }

        @MethodSource("org.jetbrains.bsp.bazel.projectview.parser.sections.ProjectViewListSectionParserTest#data")
        @ParameterizedTest
        fun shouldParseAllSectionElementsFromListWithoutDefault(
            parser: ProjectViewListSectionParser<V, T>,
            rawElementConstructor: Function<String?, String>,
            sectionConstructor: Function<List<String?>?, T>,
            sectionName: String?,
        ) {
            // given
            val rawSection1 = ProjectViewRawSection("another_section1", "bodyelement1")
            val rawSection2 = ProjectViewRawSection(
                sectionName!!,
                """ ${rawElementConstructor.apply("element1")}
${rawElementConstructor.apply("element2")}"""
            )
            val rawSection3 = ProjectViewRawSection("another_section2", "-bodyelement2")
            val rawSection4 = ProjectViewRawSection(
                sectionName, "\n\t" + rawElementConstructor.apply("element3") + "\n\n\n"
            )
            val rawSections =
                ProjectViewRawSections(listOf(rawSection1, rawSection2, rawSection3, rawSection4))

            // when
            val section = parser.parse(rawSections)

            // then
            val expectedSection = sectionConstructor.apply(listOf("element1", "element2", "element3"))
            assertEquals(expectedSection, section)
        }
    }
}
