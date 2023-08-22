package org.jetbrains.bsp.bazel.projectview.parser.sections

import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import io.kotest.matchers.shouldBe
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewExcludableListSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewTargetsSection
import org.jetbrains.bsp.bazel.projectview.parser.splitter.ProjectViewRawSection
import org.jetbrains.bsp.bazel.projectview.parser.splitter.ProjectViewRawSections
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Collectors

class ProjectViewExcludableListSectionParserTest<V, T : ProjectViewExcludableListSection<V>> {

    companion object {
        @JvmStatic
        fun data(): List<Arguments> =
            listOf(targetsSectionArguments())

        private fun targetsSectionArguments(): Arguments {
            val parser = ProjectViewTargetsSectionParser
            val rawIncludedElementConstructor = { seed: String -> "//target:$seed" }
            val elementMapper = { s: String -> BuildTargetIdentifier(s) }
            val sectionConstructor =
                createSectionConstructor({ values: List<BuildTargetIdentifier>, excludedValues: List<BuildTargetIdentifier> ->
                    ProjectViewTargetsSection(
                        values,
                        excludedValues
                    )
                }, rawIncludedElementConstructor, elementMapper)
            val sectionName = parser.sectionName
            return Arguments.of(
                parser,
                rawIncludedElementConstructor,
                { seed: String -> "-" + rawIncludedElementConstructor(seed) },
                sectionConstructor,
                sectionName
            )
        }

        private fun <V, T : ProjectViewExcludableListSection<V>?> createSectionConstructor(
            sectionMapper: (List<V>, List<V>) -> T,
            rawIncludedElementConstructor: (String) -> String,
            elementMapper: (String) -> V,
        ): (List<String>, List<String>) -> T {
            return { includedElements: List<String>, excludedElements: List<String> ->
                sectionMapper(
                    mapElements(rawIncludedElementConstructor, elementMapper, includedElements),
                    mapElements(rawIncludedElementConstructor, elementMapper, excludedElements)
                )
            }
        }

        private fun <V> mapElements(
            rawIncludedElementConstructor: (String) -> String,
            elementMapper: (String) -> V,
            rawElements: List<String>,
        ): List<V> {
            return rawElements.stream()
                .map(rawIncludedElementConstructor)
                .map(elementMapper)
                .collect(Collectors.toList())
        }
    }

    @Nested
    @DisplayName("ProjectViewExcludableListSection parse(rawSection) tests")
    internal inner class ParseRawSectionTest {
        @MethodSource("org.jetbrains.bsp.bazel.projectview.parser.sections.ProjectViewExcludableListSectionParserTest#data")
        @ParameterizedTest
        fun shouldReturnFailureForWrongSectionName(
            parser: ProjectViewExcludableListSectionParser<V, T>,
            rawIncludedElementConstructor: (String) -> String,
            rawExcludedElementConstructor: (String) -> String,
            sectionConstructor: (List<String>, List<String>) -> T,
            sectionName: String,
        ) {
            // given
            val rawSection = ProjectViewRawSection("wrongsection", "-bodyelement")

            // when
            val sectionTry = parser.parse(rawSection)

            // then
            sectionTry.isFailure shouldBe true
            (sectionTry.exceptionOrNull()!! is IllegalArgumentException) shouldBe true
            sectionTry.exceptionOrNull()!!.message shouldBe
                "Project view parsing failed! Expected '$sectionName' section name, got 'wrongsection'!"

        }

        @MethodSource("org.jetbrains.bsp.bazel.projectview.parser.sections.ProjectViewExcludableListSectionParserTest#data")
        @ParameterizedTest
        fun shouldParseEmptySectionBody(
            parser: ProjectViewExcludableListSectionParser<V, T>,
            rawIncludedElementConstructor: (String) -> String,
            rawExcludedElementConstructor: (String) -> String,
            sectionConstructor: (List<String>, List<String>) -> T,
            sectionName: String,
        ) {
            // given
            val rawSection = ProjectViewRawSection(
                sectionName, ""
            )

            // when
            val sectionTry = parser.parse(rawSection)

            // then
            sectionTry.isSuccess shouldBe true
            sectionTry.getOrNull() shouldBe null
        }

        @MethodSource("org.jetbrains.bsp.bazel.projectview.parser.sections.ProjectViewExcludableListSectionParserTest#data")
        @ParameterizedTest
        fun shouldParseIncludedElements(
            parser: ProjectViewExcludableListSectionParser<V, T>,
            rawIncludedElementConstructor: (String) -> String,
            rawExcludedElementConstructor: (String) -> String,
            sectionConstructor: (List<String>, List<String>) -> T,
            sectionName: String,
        ) {
            // given
            val rawSection = ProjectViewRawSection(
                sectionName,
                """|  ${rawIncludedElementConstructor("included1")}
            |  ${rawIncludedElementConstructor("included2")}
            |  ${rawIncludedElementConstructor("included3")}
            |  
            |""".trimMargin()
            )

            // when
            val sectionTry = parser.parse(rawSection)

            // then
            sectionTry.isSuccess shouldBe true
            val expectedSection =
                sectionConstructor(listOf("included1", "included2", "included3"), listOf())
            sectionTry.getOrNull() shouldBe expectedSection
        }

        @MethodSource("org.jetbrains.bsp.bazel.projectview.parser.sections.ProjectViewExcludableListSectionParserTest#data")
        @ParameterizedTest
        fun shouldParseExcludedElements(
            parser: ProjectViewExcludableListSectionParser<V, T>,
            rawIncludedElementConstructor: (String) -> String,
            rawExcludedElementConstructor: (String) -> String,
            sectionConstructor: (List<String>, List<String>) -> T,
            sectionName: String,
        ) {
            // given
            val rawSection = ProjectViewRawSection(
                sectionName,
                """
            |  ${rawExcludedElementConstructor("excluded1")}
            |  ${rawExcludedElementConstructor("excluded2")}
            |  ${rawExcludedElementConstructor("excluded3")}
            |
            """.trimMargin()
            )

            // when
            val sectionTry = parser.parse(rawSection)

            // then
            sectionTry.isSuccess shouldBe true
            val expectedSection =
                sectionConstructor(listOf(), listOf("excluded1", "excluded2", "excluded3"))
            sectionTry.getOrNull() shouldBe expectedSection
        }

        @MethodSource("org.jetbrains.bsp.bazel.projectview.parser.sections.ProjectViewExcludableListSectionParserTest#data")
        @ParameterizedTest
        fun shouldParseIncludedAndExcludedElements(
            parser: ProjectViewExcludableListSectionParser<V, T>,
            rawIncludedElementConstructor: (String) -> String,
            rawExcludedElementConstructor: (String) -> String,
            sectionConstructor: (List<String>, List<String>) -> T,
            sectionName: String,
        ) {
            // given
            val rawSection = ProjectViewRawSection(
                sectionName,
                """
            |  ${rawIncludedElementConstructor("included1")}
            |  ${rawExcludedElementConstructor("excluded1")}
            |  ${rawExcludedElementConstructor("excluded2")}
            |
            """.trimMargin()
            )

            // when
            val sectionTry = parser.parse(rawSection)

            // then
            sectionTry.isSuccess shouldBe true
            val expectedSection = sectionConstructor(listOf("included1"), listOf("excluded1", "excluded2"))
            sectionTry.getOrNull() shouldBe expectedSection
        }
    }

    @Nested
    @DisplayName("ProjectViewExcludableListSection parse(rawSections) test")
    internal inner class ParseRawSectionsTest {
        @MethodSource("org.jetbrains.bsp.bazel.projectview.parser.sections.ProjectViewExcludableListSectionParserTest#data")
        @ParameterizedTest
        fun shouldReturnEmptySectionIfThereIsNoSectionForParseWithoutDefault(
            parser: ProjectViewExcludableListSectionParser<V, T>,
            rawIncludedElementConstructor: (String) -> String,
            rawExcludedElementConstructor: (String) -> String,
            sectionConstructor: (List<String>, List<String>) -> T,
            sectionName: String,
        ) {
            // given
            val rawSection1 = ProjectViewRawSection(
                "another_section1", "  -bodyelement1.1\n\tbodyelement1.2\n-bodyelement1.3\n\n"
            )
            val rawSection2 = ProjectViewRawSection("another_section2", "-bodyelement2.1")
            val rawSection3 = ProjectViewRawSection("another_section3", "-bodyelement3.1")
            val rawSections = ProjectViewRawSections(listOf(rawSection1, rawSection2, rawSection3))

            // when
            val section = parser.parse(rawSections)

            // then
            section shouldBe null
        }

        @MethodSource("org.jetbrains.bsp.bazel.projectview.parser.sections.ProjectViewExcludableListSectionParserTest#data")
        @ParameterizedTest
        fun shouldParseAllSectionElementsFromListWithoutDefault(
            parser: ProjectViewExcludableListSectionParser<V, T>,
            rawIncludedElementConstructor: (String) -> String,
            rawExcludedElementConstructor: (String) -> String,
            sectionConstructor: (List<String>, List<String>) -> T,
            sectionName: String,
        ) {
            // given
            val rawSection1 = ProjectViewRawSection("another_section1", "-bodyelement1")
            val rawSection2 = ProjectViewRawSection(
                sectionName,
                """ ${rawExcludedElementConstructor("excluded1")}
${rawExcludedElementConstructor("excluded2")}"""
            )
            val rawSection3 = ProjectViewRawSection("another_section2", "-bodyelement2")
            val rawSection4 = ProjectViewRawSection(
                sectionName, "\n\t" + rawIncludedElementConstructor("included1") + "\n\n\n"
            )
            val rawSections =
                ProjectViewRawSections(java.util.List.of(rawSection1, rawSection2, rawSection3, rawSection4))

            // when
            val section = parser.parse(rawSections)

            // then
            val expectedSection = sectionConstructor(listOf("included1"), listOf("excluded1", "excluded2"))
            section shouldBe expectedSection
        }
    }
}
