package org.jetbrains.bsp.bazel.workspacecontext

import io.kotest.matchers.shouldBe
import io.vavr.collection.List
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBuildFlagsSection
import org.junit.jupiter.api.Test

class BuildFlagsSpecMapperTest {

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
                    List.of(
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
