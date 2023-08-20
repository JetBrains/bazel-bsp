package org.jetbrains.bsp.bazel.projectview.parser.sections

import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBazelBinarySection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBuildManualTargetsSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewSingletonSection
import org.jetbrains.bsp.bazel.projectview.parser.splitter.ProjectViewRawSection
import org.jetbrains.bsp.bazel.projectview.parser.splitter.ProjectViewRawSections
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.nio.file.Path
import java.nio.file.Paths
import java.util.function.Function
import java.util.stream.Stream

class ProjectViewSingletonSectionParserTest<V, T : ProjectViewSingletonSection<V>> {
  companion object {
    @JvmStatic
    fun data(): Stream<Arguments> {
      return Stream.of(
        bazelBinarySectionArguments(),
        buildManualTargetsSectionArguments()
      )
    }

    private fun bazelBinarySectionArguments(): Arguments {
      val parser = ProjectViewBazelBinarySectionParser
      val rawValueConstructor = Function { seed: String -> "/path/to/bazel/$seed" }
      val elementMapper = Function { first: String -> Paths.get(first) }
      val sectionConstructor = createSectionConstructor(
        rawValueConstructor,
        { value: Path -> ProjectViewBazelBinarySection(value) },
        elementMapper
      )
      val sectionName = parser.sectionName
      return Arguments.of(parser, rawValueConstructor, sectionConstructor, sectionName)
    }

    private fun buildManualTargetsSectionArguments(): Arguments {
      val parser = ProjectViewBuildManualTargetsSectionParser
      val rawValueConstructor = Function { seed: String -> "false" }
      val sectionConstructor = createSectionConstructor(
        rawValueConstructor,
        Function<Boolean, ProjectViewBuildManualTargetsSection> { value: Boolean ->
          ProjectViewBuildManualTargetsSection(
            value
          )
        },
        Function { value: String -> value.toBoolean() }
      )
      val sectionName = parser.sectionName
      return Arguments.of(parser, rawValueConstructor, sectionConstructor, sectionName)
    }

    private fun <V, T : ProjectViewSingletonSection<V>?> createSectionConstructor(
      rawValueConstructor: Function<String, String>,
      sectionMapper: Function<V, T>,
      elementMapper: Function<String, V>,
    ): Function<String, T> {
      return Function { seed: String -> sectionMapper.apply(elementMapper.apply(rawValueConstructor.apply(seed))) }
    }
  }

  @Nested
  @DisplayName("T parse(rawSection) tests")
  internal inner class ParseRawSectionTest {
    @MethodSource("org.jetbrains.bsp.bazel.projectview.parser.sections.ProjectViewSingletonSectionParserTest#data")
    @ParameterizedTest
    fun shouldReturnFailureForWrongSectionName(
      parser: ProjectViewSingletonSectionParser<V, T>,
      rawValueConstructor: Function<String?, String?>?,
      sectionConstructor: Function<String?, T>?,
      sectionName: String,
    ) {
      // given
      val rawSection = ProjectViewRawSection("wrongsection", "value")

      // when
      val sectionTry = parser.parse(rawSection)

      // then
      assertTrue(sectionTry.isFailure)
      assertTrue(sectionTry.exceptionOrNull() is IllegalArgumentException)
      assertEquals(
        "Project view parsing failed! Expected '$sectionName' section name, got 'wrongsection'!",
        sectionTry.exceptionOrNull()!!.message
      )
    }

    @MethodSource("org.jetbrains.bsp.bazel.projectview.parser.sections.ProjectViewSingletonSectionParserTest#data")
    @ParameterizedTest
    fun shouldReturnEmptyForEmptySectionBody(
      parser: ProjectViewSingletonSectionParser<V, T>,
      rawValueConstructor: Function<String, String>,
      sectionConstructor: Function<String, T>,
      sectionName: String,
    ) {
      // given
      val rawSection = ProjectViewRawSection(
        sectionName, ""
      )

      // when
      val sectionTry = parser.parse(rawSection)

      // then
      assertTrue(sectionTry.isSuccess)
      assertTrue(sectionTry.getOrNull() == null)
    }

    @MethodSource("org.jetbrains.bsp.bazel.projectview.parser.sections.ProjectViewSingletonSectionParserTest#data")
    @ParameterizedTest
    fun shouldReturnSectionWithTrimmedValue(
      parser: ProjectViewSingletonSectionParser<V, T>,
      rawValueConstructor: Function<String?, String>,
      sectionConstructor: Function<String?, T>,
      sectionName: String?,
    ) {
      // given
      val rawSection = ProjectViewRawSection(
        sectionName!!, "  " + rawValueConstructor.apply("value") + "\t\n"
      )

      // when
      val sectionTry = parser.parse(rawSection)

      // then
      assertTrue(sectionTry.isSuccess)
      val expectedSection = sectionConstructor.apply("value")
      assertEquals(expectedSection, sectionTry.getOrNull())
    }
  }

  @Nested
  @DisplayName("T parse(rawSections) tests")
  internal inner class ParseRawSectionsTest {
    @MethodSource("org.jetbrains.bsp.bazel.projectview.parser.sections.ProjectViewSingletonSectionParserTest#data")
    @ParameterizedTest
    fun shouldReturnLastSectionWithoutExplicitDefault(
      parser: ProjectViewSingletonSectionParser<V, T>,
      rawValueConstructor: Function<String?, String>,
      sectionConstructor: Function<String?, T>,
      sectionName: String?,
    ) {
      // given
      val rawSection1 = ProjectViewRawSection("another_section1", "value1")
      val rawSection2 = ProjectViewRawSection(
        sectionName!!, "  " + rawValueConstructor.apply("value2") + "\n"
      )
      val rawSection3 = ProjectViewRawSection("another_section2", "\tvalue3\n")
      val rawSection4 = ProjectViewRawSection(
        sectionName, """    ${rawValueConstructor.apply("value4")}
  """
      )
      val rawSection5 = ProjectViewRawSection("another_section3", "\tvalue5\n")
      val rawSections = ProjectViewRawSections(
        listOf(rawSection1, rawSection2, rawSection3, rawSection4, rawSection5)
      )

      // when
      val section = parser.parse(rawSections)

      // then
      val expectedSection = sectionConstructor.apply("value4")
      assertEquals(expectedSection, section)
    }

    @MethodSource("org.jetbrains.bsp.bazel.projectview.parser.sections.ProjectViewSingletonSectionParserTest#data")
    @ParameterizedTest
    fun shouldReturnEmptyIfSectionDoesntExist(
      parser: ProjectViewSingletonSectionParser<V, T>,
      rawValueConstructor: Function<String?, String?>?,
      sectionConstructor: Function<String?, T>?,
      sectionName: String?,
    ) {
      // given
      val rawSection1 = ProjectViewRawSection("another_section1", "value1")
      val rawSection2 = ProjectViewRawSection("another_section2", "  value2")
      val rawSection3 = ProjectViewRawSection("another_section3", "\tvalue3\n")
      val rawSections = ProjectViewRawSections(listOf(rawSection1, rawSection2, rawSection3))

      // when
      val section = parser.parse(rawSections)

      // then
      assertTrue(section == null)
    }
  }
}
