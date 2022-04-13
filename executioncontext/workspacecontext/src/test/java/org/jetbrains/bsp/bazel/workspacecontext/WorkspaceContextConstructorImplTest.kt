package org.jetbrains.bsp.bazel.workspacecontext

import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import io.kotest.matchers.shouldBe
import io.vavr.control.Try
import io.vavr.collection.List
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewTargetsSection
import org.jetbrains.bsp.bazel.workspacecontext.entries.ExecutionContextTargetsEntity
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class WorkspaceContextConstructorImplTest {

    private lateinit var workspaceContextConstructor: WorkspaceContextConstructorImpl

    @BeforeEach
    fun beforeEach() {
        // given
        this.workspaceContextConstructor = WorkspaceContextConstructorImpl()
    }

    @Nested
    @DisplayName("fun construct(projectViewTry: Try<ProjectView>): Try<WorkspaceContext> tests")
    inner class ConstructTryTest {

        @Test
        fun `should return failure if project view is failure`() {
            // given
            val projectViewTry = Try.failure<ProjectView>(Exception("exception message"))

            // when
            val workspaceContextTry = workspaceContextConstructor.construct(projectViewTry)

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
                        List.of(
                            BuildTargetIdentifier("//included_target1"),
                            BuildTargetIdentifier("//included_target2"),
                            BuildTargetIdentifier("//included_target3")
                        ),
                        List.of(BuildTargetIdentifier("//excluded_target1")),
                    )
                ).build()

            // when
            val workspaceContextTry = workspaceContextConstructor.construct(projectView)

            // then
            workspaceContextTry.isSuccess shouldBe true
            val workspaceContext = workspaceContextTry.get()

            val expectedTargets =
                ExecutionContextTargetsEntity(
                    List.of(
                        BuildTargetIdentifier("//included_target1"),
                        BuildTargetIdentifier("//included_target2"),
                        BuildTargetIdentifier("//included_target3")
                    ), List.of(BuildTargetIdentifier("//excluded_target1"))
                )
            workspaceContext.targets shouldBe expectedTargets
        }
    }
}
