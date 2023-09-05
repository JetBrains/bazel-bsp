package org.jetbrains.bsp.bazel.workspacecontext

import io.kotest.matchers.shouldBe
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBuildManualTargetsSection
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BuildManualTargetsSpecMapperTest {

    @Nested
    @DisplayName("fun map(projectView): BuildManualTargetsSpec tests")
    inner class MapTest {

        @Test
        fun `should return success with default spec if build manual targets is null`() {
            // given
            val projectView = ProjectView.Builder(buildManualTargets = null).build()

            // when
            val buildManualTargetsSpec = BuildManualTargetsSpecExtractor.fromProjectView(projectView)

            // then
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
                    ).build()

            // when
            val buildManualTargetsSpec = BuildManualTargetsSpecExtractor.fromProjectView(projectView)

            // then
            val expectedBuildManualTargetsSpec = BuildManualTargetsSpec(
                    true
            )
            buildManualTargetsSpec shouldBe expectedBuildManualTargetsSpec
        }
    }
}
