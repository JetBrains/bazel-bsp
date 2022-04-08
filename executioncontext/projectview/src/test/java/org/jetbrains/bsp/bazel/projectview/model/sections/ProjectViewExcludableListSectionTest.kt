package org.jetbrains.bsp.bazel.projectview.model.sections

import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.vavr.collection.List
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class ProjectViewExcludableListSectionTest<V, T : ProjectViewExcludableListSection<V>> {

    @ParameterizedTest
    @MethodSource("data")
    fun `should return true for the same sections with the same values`(sectionConstructor: (List<String>, List<String>) -> T) {
        // given & when
        val section1: T = sectionConstructor(
            List.of("included_value1", "included_value2"),
            List.of("excluded_value1", "excluded_value3", "excluded_value2")
        )
        val section2: T = sectionConstructor(
            List.of("included_value2", "included_value1"),
            List.of("excluded_value3", "excluded_value2", "excluded_value1")
        )

        // then
        section1 shouldBe section2
    }

    @ParameterizedTest
    @MethodSource("data")
    fun `should return false for the same sections with different included values`(sectionConstructor: (List<String>, List<String>) -> T) {
        // given & when
        val section1: T = sectionConstructor(
            List.of("included_value1", "included_value3"),
            List.of("excluded_value1", "excluded_value3", "excluded_value2")
        )
        val section2: T = sectionConstructor(
            List.of("included_value2", "included_value1"),
            List.of("excluded_value3", "excluded_value2", "excluded_value1")
        )

        // then
        section1 shouldNotBe section2
    }

    @ParameterizedTest
    @MethodSource("data")
    fun `should return false for the same sections with different excluded values`(sectionConstructor: (List<String>, List<String>) -> T) {
        // given & when
        val section1: T = sectionConstructor(
            List.of("included_value1", "included_value2"),
            List.of("excluded_value1", "excluded_value3", "excluded_value2")
        )
        val section2: T = sectionConstructor(
            List.of("included_value2", "included_value1"),
            List.of("excluded_value3", "excluded_value5", "excluded_value1")
        )

        // then
        section1 shouldNotBe section2
    }

    companion object {

        @JvmStatic
        fun data() = listOf(targetsSectionArguments())

        private fun targetsSectionArguments(): Arguments {
            val sectionConstructor =
                createSectionConstructor(::ProjectViewTargetsSection) { BuildTargetIdentifier("//:$it") }

            return Arguments.of(sectionConstructor)
        }

        private fun <V, T : ProjectViewExcludableListSection<V>> createSectionConstructor(
            sectionMapper: (List<V>, List<V>) -> T, elementMapper: (String) -> V
        ): (List<String>, List<String>) -> T = { includedElements, excludedElements ->
            sectionMapper(includedElements.map(elementMapper), excludedElements.map(elementMapper))
        }
    }
}
