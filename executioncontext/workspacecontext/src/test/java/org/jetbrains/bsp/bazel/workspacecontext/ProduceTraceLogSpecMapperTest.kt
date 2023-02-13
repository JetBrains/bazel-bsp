package org.jetbrains.bsp.bazel.workspacecontext

import io.kotest.matchers.shouldBe
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewProduceTraceLogSection
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ProduceTraceLogSpecMapperTest {

    @Nested
    @DisplayName("fun map(projectView): Try<ProduceTraceLogSpec> tests")
    inner class MapTest {

        @Test
        fun `should return success with default spec if produce trace log is null`() {
            // given
            val projectView = ProjectView.Builder(produceTraceLog = null).build().get()

            // when
            val produceTraceLogSpecTry = ProduceTraceLogSpecMapper.map(projectView)

            // then
            produceTraceLogSpecTry.isSuccess shouldBe true
            val produceTraceLogSpec = produceTraceLogSpecTry.get()

            val expectedProduceTraceLogSpecTry = ProduceTraceLogSpec(false)
            produceTraceLogSpec shouldBe expectedProduceTraceLogSpecTry
        }

        @Test
        fun `should return success for successful mapping`() {
            // given
            val projectView =
                ProjectView.Builder(
                    produceTraceLog = ProjectViewProduceTraceLogSection(true)
                ).build().get()

            // when
            val produceTraceLogSpecTry = ProduceTraceLogSpecMapper.map(projectView)

            // then
            produceTraceLogSpecTry.isSuccess shouldBe true
            val produceTraceLogSpec = produceTraceLogSpecTry.get()

            val expectedProduceTraceLogSpecTry = ProduceTraceLogSpec(true)
            produceTraceLogSpec shouldBe expectedProduceTraceLogSpecTry
        }
    }

    @Nested
    @DisplayName("fun default(): Try<ProduceTraceLogSpec> tests")
    inner class DefaultTest {

        @Test
        fun `should return success and default spec with false`() {
            // given
            // when
            val produceTraceLogSpecTry = ProduceTraceLogSpecMapper.default()

            // then
            produceTraceLogSpecTry.isSuccess shouldBe true
            val produceTraceLogSpec = produceTraceLogSpecTry.get()

            val expectedProduceTraceLogSpecTry = ProduceTraceLogSpec(false)
            produceTraceLogSpec shouldBe expectedProduceTraceLogSpecTry
        }
    }
}
