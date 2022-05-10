package org.jetbrains.bsp.bazel.workspacecontext

import io.kotest.matchers.shouldBe
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBuildFlagsSection
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BuildFlagsSpecMapperTest {

    @Nested
    @DisplayName("fun map(projectView): Try<BuildFlagsSpec> tests")
    inner class MapTest {

        @Test
        fun `should return success with default spec if build flags are null`() {
            // given
            val projectView = ProjectView.Builder(buildFlags = null).build().get()

            // when
            val buildFlagsSpecTry = BuildFlagsSpecMapper.map(projectView)

            // then
            buildFlagsSpecTry.isSuccess shouldBe true
            val buildFlagsSpec = buildFlagsSpecTry.get()

            val expectedBuildFlagsSpec = BuildFlagsSpec(emptyList())
            buildFlagsSpec shouldBe expectedBuildFlagsSpec
        }

        @Test
        fun `should return success for successful mapping`() {
            // given
            val projectView =
                ProjectView.Builder(
                    buildFlags = ProjectViewBuildFlagsSection(
                        listOf(
                            "--build_flag1=value1",
                            "--build_flag2=value2",
                            "--build_flag3=value3",
                        ),
                    )
                ).build().get()

            // when
            val buildFlagsSpecTry = BuildFlagsSpecMapper.map(projectView)

            // then
            buildFlagsSpecTry.isSuccess shouldBe true
            val buildFlagsSpec = buildFlagsSpecTry.get()

            val expectedBuildFlagsSpec = BuildFlagsSpec(
                listOf(
                    "--build_flag1=value1",
                    "--build_flag2=value2",
                    "--build_flag3=value3",
                )
            )
            buildFlagsSpec shouldBe expectedBuildFlagsSpec
        }
    }

    @Nested
    @DisplayName("fun default(): Try<BuildFlagsSpec> tests")
    inner class DefaultTest {

        @Test
        fun `should return success and default spec with empty list`() {
            // given
            // when
            val buildFlagsSpecTry = BuildFlagsSpecMapper.default()

            // then
            buildFlagsSpecTry.isSuccess shouldBe true
            val buildFlagsSpec = buildFlagsSpecTry.get()

            val expectedBuildFlagsSpec = BuildFlagsSpec(emptyList())
            buildFlagsSpec shouldBe expectedBuildFlagsSpec
        }
    }
}
