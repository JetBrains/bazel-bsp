package org.jetbrains.bsp.bazel.projectview.model.sections

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.vavr.collection.List
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class ProjectViewListSectionTest<V, T : ProjectViewListSection<V>> {

    @ParameterizedTest
    @MethodSource("data")
    fun `should return true for the same sections with the same values`(sectionConstructor: (List<String>) -> T) {
        // given & when
        val section1 = sectionConstructor(List.of("value1", "value2"))
        val section2 = sectionConstructor(List.of("value1", "value2"))

        // then
        section1 shouldBe section2
    }

    @ParameterizedTest
    @MethodSource("data")
    fun `should return false for the same sections with different values`(sectionConstructor: (List<String>) -> T) {
        // given & when
        val section1 = sectionConstructor(List.of("value1", "value3"))
        val section2 = sectionConstructor(List.of("value2", "value1"))

        // then
        section1 shouldNotBe section2
    }

    companion object {

        @JvmStatic
        fun data() = listOf(buildFlagsSectionArguments())

        private fun buildFlagsSectionArguments(): Arguments {
            val sectionConstructor =
                createSectionConstructor(::ProjectViewBuildFlagsSection) { "--flag_$it=dummy_value" }

            return Arguments.of(sectionConstructor)
        }

        private fun <V, T : ProjectViewListSection<V>?> createSectionConstructor(
            sectionMapper: (List<V>) -> T,
            elementMapper: (String) -> V,
        ): (List<String>) -> T = { sectionMapper(it.map(elementMapper)) }
    }
}
