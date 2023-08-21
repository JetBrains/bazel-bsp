package org.jetbrains.bsp.bazel.projectview.parser.sections

import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import org.assertj.core.api.Assertions
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewExcludableListSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewTargetsSection
import org.jetbrains.bsp.bazel.projectview.parser.splitter.ProjectViewRawSection
import org.jetbrains.bsp.bazel.projectview.parser.splitter.ProjectViewRawSections
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.function.BiFunction
import java.util.function.Function
import java.util.stream.Collectors
import java.util.stream.Stream

class ProjectViewExcludableListSectionParserTest<V, T : ProjectViewExcludableListSection<V>> {
  
  companion object {
    @JvmStatic
    fun data(): List<Arguments> =
      listOf(targetsSectionArguments())

    private fun targetsSectionArguments(): Arguments {
      val parser = ProjectViewTargetsSectionParser
      val rawIncludedElementConstructor = { seed: String -> "//target:$seed" }
      val elementMapper =
        Function { s: String -> BuildTargetIdentifier(s) }
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
        Function { seed: String -> "-" + rawIncludedElementConstructor.apply(seed) },
        sectionConstructor,
        sectionName
      )
    }

    private fun <V, T : ProjectViewExcludableListSection<V>?> createSectionConstructor(
      sectionMapper: BiFunction<List<V>, List<V>, T>,
      rawIncludedElementConstructor: Function<String, String>,
      elementMapper: Function<String, V>
    ): BiFunction<List<String>, List<String>, T> {
      return BiFunction { includedElements: List<String>, excludedElements: List<String> ->
        sectionMapper.apply(
          mapElements(rawIncludedElementConstructor, elementMapper, includedElements),
          mapElements(rawIncludedElementConstructor, elementMapper, excludedElements)
        )
      }
    }

    private fun <V> mapElements(
      rawIncludedElementConstructor: Function<String, String>,
      elementMapper: Function<String, V>,
      rawElements: List<String>
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
      rawIncludedElementConstructor: Function<String?, String?>?,
      rawExcludedElementConstructor: Function<String?, String?>?,
      sectionConstructor: BiFunction<List<String?>?, List<String?>?, T>?,
      sectionName: String
    ) {
      // given
      val rawSection = ProjectViewRawSection("wrongsection", "-bodyelement")

      // when
      val sectionTry = parser.parse(rawSection)

      // then
      assertTrue(sectionTry.isFailure)
      assertTrue(sectionTry.exceptionOrNull()!! is IllegalArgumentException)
      assertEquals(
        "Project view parsing failed! Expected '$sectionName' section name, got 'wrongsection'!",
        sectionTry.exceptionOrNull()!!.message
      )
    }

    @MethodSource("org.jetbrains.bsp.bazel.projectview.parser.sections.ProjectViewExcludableListSectionParserTest#data")
    @ParameterizedTest
    fun shouldParseEmptySectionBody(
      parser: ProjectViewExcludableListSectionParser<V, T>,
      rawIncludedElementConstructor: Function<String?, String?>?,
      rawExcludedElementConstructor: Function<String?, String?>?,
      sectionConstructor: BiFunction<List<String?>?, List<String?>?, T>?,
      sectionName: String?
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

    @MethodSource("org.jetbrains.bsp.bazel.projectview.parser.sections.ProjectViewExcludableListSectionParserTest#data")
    @ParameterizedTest
    fun shouldParseIncludedElements(
      parser: ProjectViewExcludableListSectionParser<V, T>,
      rawIncludedElementConstructor: Function<String?, String>,
      rawExcludedElementConstructor: Function<String?, String?>?,
      sectionConstructor: BiFunction<List<String?>?, List<String?>?, T>,
      sectionName: String?
    ) {
      // given
      val rawSection = ProjectViewRawSection(
        sectionName!!,
        "  "
          + rawIncludedElementConstructor.apply("included1")
          + "\n\t"
          + rawIncludedElementConstructor.apply("included2")
          + "\n"
          + rawIncludedElementConstructor.apply("included3")
          + "\n\n"
      )

      // when
      val sectionTry = parser.parse(rawSection)

      // then
      assertTrue(sectionTry.isSuccess)
      val expectedSection = sectionConstructor.apply(listOf("included1", "included2", "included3"), listOf<String>())
      assertEquals(expectedSection, sectionTry.getOrNull())
    }

    @MethodSource("org.jetbrains.bsp.bazel.projectview.parser.sections.ProjectViewExcludableListSectionParserTest#data")
    @ParameterizedTest
    fun shouldParseExcludedElements(
      parser: ProjectViewExcludableListSectionParser<V, T>,
      rawIncludedElementConstructor: Function<String?, String?>?,
      rawExcludedElementConstructor: Function<String?, String>,
      sectionConstructor: BiFunction<List<String?>?, List<String?>?, T>,
      sectionName: String?
    ) {
      // given
      val rawSection = ProjectViewRawSection(
        sectionName!!,
        "  "
          + rawExcludedElementConstructor.apply("excluded1")
          + "\n\t"
          + rawExcludedElementConstructor.apply("excluded2")
          + "\n"
          + rawExcludedElementConstructor.apply("excluded3")
          + "\n\n"
      )

      // when
      val sectionTry = parser.parse(rawSection)

      // then
      assertTrue(sectionTry.isSuccess)
      val expectedSection = sectionConstructor.apply(listOf<String>(), listOf("excluded1", "excluded2", "excluded3"))
      assertEquals(expectedSection, sectionTry.getOrNull())
    }

    @MethodSource("org.jetbrains.bsp.bazel.projectview.parser.sections.ProjectViewExcludableListSectionParserTest#data")
    @ParameterizedTest
    fun shouldParseIncludedAndExcludedElements(
      parser: ProjectViewExcludableListSectionParser<V, T>,
      rawIncludedElementConstructor: Function<String?, String>,
      rawExcludedElementConstructor: Function<String?, String>,
      sectionConstructor: BiFunction<List<String?>?, List<String?>?, T>,
      sectionName: String?
    ) {
      // given
      val rawSection = ProjectViewRawSection(
        sectionName!!,
        "  "
          + rawExcludedElementConstructor.apply("excluded1")
          + "\n\t"
          + rawIncludedElementConstructor.apply("included1")
          + "\n"
          + rawExcludedElementConstructor.apply("excluded2")
          + "\n\n"
      )

      // when
      val sectionTry = parser.parse(rawSection)

      // then
      assertTrue(sectionTry.isSuccess)
      val expectedSection = sectionConstructor.apply(listOf("included1"), listOf("excluded1", "excluded2"))
      assertEquals(expectedSection, sectionTry.getOrNull())
    }
  }

  @Nested
  @DisplayName("ProjectViewExcludableListSection parse(rawSections) test")
  internal inner class ParseRawSectionsTest {
    @MethodSource("org.jetbrains.bsp.bazel.projectview.parser.sections.ProjectViewExcludableListSectionParserTest#data")
    @ParameterizedTest
    fun shouldReturnEmptySectionIfThereIsNoSectionForParseWithoutDefault(
      parser: ProjectViewExcludableListSectionParser<V, T>,
      rawIncludedElementConstructor: Function<String?, String?>?,
      rawExcludedElementConstructor: Function<String?, String?>?,
      sectionConstructor: BiFunction<List<String?>?, List<String?>?, T>?,
      sectionName: String?
    ) {
      // given
      val rawSection1 = ProjectViewRawSection(
        "another_section1", "  -bodyelement1.1\n\tbodyelement1.2\n-bodyelement1.3\n\n"
      )
      val rawSection2 = ProjectViewRawSection("another_section2", "-bodyelement2.1")
      val rawSection3 = ProjectViewRawSection("another_section3", "-bodyelement3.1")
      val rawSections = ProjectViewRawSections(java.util.List.of(rawSection1, rawSection2, rawSection3))

      // when
      val section = parser.parse(rawSections)

      // then
      Assertions.assertThat(section).isNull()
    }

    @MethodSource("org.jetbrains.bsp.bazel.projectview.parser.sections.ProjectViewExcludableListSectionParserTest#data")
    @ParameterizedTest
    fun shouldParseAllSectionElementsFromListWithoutDefault(
      parser: ProjectViewExcludableListSectionParser<V, T>,
      rawIncludedElementConstructor: Function<String?, String>,
      rawExcludedElementConstructor: Function<String?, String>,
      sectionConstructor: BiFunction<List<String?>?, List<String?>?, T>,
      sectionName: String?
    ) {
      // given
      val rawSection1 = ProjectViewRawSection("another_section1", "-bodyelement1")
      val rawSection2 = ProjectViewRawSection(
        sectionName!!,
        """ ${rawExcludedElementConstructor.apply("excluded1")}
${rawExcludedElementConstructor.apply("excluded2")}"""
      )
      val rawSection3 = ProjectViewRawSection("another_section2", "-bodyelement2")
      val rawSection4 = ProjectViewRawSection(
        sectionName, "\n\t" + rawIncludedElementConstructor.apply("included1") + "\n\n\n"
      )
      val rawSections = ProjectViewRawSections(java.util.List.of(rawSection1, rawSection2, rawSection3, rawSection4))

      // when
      val section = parser.parse(rawSections)

      // then
      val expectedSection = sectionConstructor.apply(listOf("included1"), listOf("excluded1", "excluded2"))
      Assertions.assertThat(section).isEqualTo(expectedSection)
    }
  }
}
