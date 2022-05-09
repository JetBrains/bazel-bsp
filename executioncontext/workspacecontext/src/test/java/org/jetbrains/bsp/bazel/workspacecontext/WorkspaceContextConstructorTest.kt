package org.jetbrains.bsp.bazel.workspacecontext

import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import io.kotest.matchers.shouldBe
import io.vavr.control.Try
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBazelPathSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBuildFlagsSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewTargetsSection
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.io.path.Path

class WorkspaceContextConstructorTest {

    @Nested
    @DisplayName("fun construct(projectViewTry: Try<ProjectView>): Try<WorkspaceContext> tests")
    inner class ConstructTryTest {

        @Test
        fun `should return failure if project view is failure`() {
            // given
            val projectViewTry = Try.failure<ProjectView>(Exception("exception message"))

            // when
            val workspaceContextTry = WorkspaceContextConstructor.construct(projectViewTry)

            // then
            workspaceContextTry.isFailure shouldBe true
            workspaceContextTry.cause::class shouldBe Exception::class
            workspaceContextTry.cause.message shouldBe "exception message"
        }
    }

    @Nested
    @DisplayName("fun construct(projectView: ProjectView): Try<WorkspaceContext> tests")
    inner class ConstructTest {

        @Test
        fun `should return success if project view is valid`() {
            // given
            val projectView =
                ProjectView.Builder(
                    targets =
                    ProjectViewTargetsSection(
                        io.vavr.collection.List.of(
                            BuildTargetIdentifier("//included_target1"),
                            BuildTargetIdentifier("//included_target2"),
                            BuildTargetIdentifier("//included_target3")
                        ),
                        io.vavr.collection.List.of(BuildTargetIdentifier("//excluded_target1")),
                    ),
                    buildFlags = ProjectViewBuildFlagsSection(
                        io.vavr.collection.List.of(
                            "--build_flag1=value1",
                            "--build_flag2=value2",
                            "--build_flag3=value3",
                        )
                    ),
                    bazelPath = ProjectViewBazelPathSection(Path("/path/to/bazel"))
                ).build()

            // when
            val workspaceContextTry = WorkspaceContextConstructor.construct(projectView)

            // then
            workspaceContextTry.isSuccess shouldBe true
            val workspaceContext = workspaceContextTry.get()

            val expectedTargets =
                TargetsSpec(
                    listOf(
                        BuildTargetIdentifier("//included_target1"),
                        BuildTargetIdentifier("//included_target2"),
                        BuildTargetIdentifier("//included_target3")
                    ), listOf(BuildTargetIdentifier("//excluded_target1"))
                )
            workspaceContext.targets shouldBe expectedTargets

            val expectedBuildFlagsSpec = BuildFlagsSpec(
                listOf(
                    "--build_flag1=value1",
                    "--build_flag2=value2",
                    "--build_flag3=value3",
                )
            )
            workspaceContext.buildFlags shouldBe expectedBuildFlagsSpec

            val expectedBazelPathSpec = BazelPathSpec(Path("/path/to/bazel"))
            workspaceContext.bazelPath shouldBe expectedBazelPathSpec
        }
    }
}
