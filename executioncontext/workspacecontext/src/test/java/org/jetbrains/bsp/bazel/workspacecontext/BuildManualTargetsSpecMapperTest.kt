package org.jetbrains.bsp.bazel.workspacecontext

import io.kotest.matchers.shouldBe
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBuildManualTargetsSection
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BuildManualTargetsSpecMapperTest {

    @Nested
    @DisplayName("fun map(projectView): Try<BuildManualTargetsSpec> tests")
    inner class MapTest {

        @Test
        fun `should return success with default spec if build manual targets is null`() {
            // given
            val projectView = ProjectView.Builder(buildManualTargets = null).build().get()

            // when
            val buildManualTargetsSpecTry = BuildManualTargetsSpecMapper.map(projectView)

            // then
            buildManualTargetsSpecTry.isSuccess shouldBe true
            val buildManualTargetsSpec = buildManualTargetsSpecTry.get()

            val expectedBuildManualTargetsSpec = BuildManualTargetsSpec(false)
            buildManualTargetsSpec shouldBe expectedBuildManualTargetsSpec
        }

        @Test
        fun `should return success for successful mapping`() {
            // given
            val projectView =
                    ProjectView.Builder(
                            buildManualTargets = ProjectViewBuildManualTargetsSection(
                                    true
                            )
                    ).build().get()

            // when
            val buildManualTargetsSpecTry = BuildManualTargetsSpecMapper.map(projectView)

            // then
            buildManualTargetsSpecTry.isSuccess shouldBe true
            val buildManualTargetsSpec = buildManualTargetsSpecTry.get()

            val expectedBuildManualTargetsSpec = BuildManualTargetsSpec(
                    true
            )
            buildManualTargetsSpec shouldBe expectedBuildManualTargetsSpec
        }
    }

    @Nested
    @DisplayName("fun default(): Try<BuildManualTargetsSpec> tests")
    inner class DefaultTest {

        @Test
        fun `should return success and default spec with false`() {
            // given
            // when
            val buildManualTargetsTry = BuildManualTargetsSpecMapper.default()

            // then
            buildManualTargetsTry.isSuccess shouldBe true
            val buildManualTargetsSpec = buildManualTargetsTry.get()

            val expectedBuildFlagsSpec = BuildManualTargetsSpec(false)
            buildManualTargetsSpec shouldBe expectedBuildFlagsSpec
        }
    }
}
