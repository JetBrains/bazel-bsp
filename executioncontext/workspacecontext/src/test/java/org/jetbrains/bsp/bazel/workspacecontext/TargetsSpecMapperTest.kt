package org.jetbrains.bsp.bazel.workspacecontext

import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import io.kotest.matchers.shouldBe
import org.jetbrains.bsp.bazel.executioncontext.api.ProjectViewToExecutionContextEntityMapperException
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewDeriveTargetsFromDirectoriesSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewDirectoriesSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewTargetsSection
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.io.path.Path

class TargetsSpecMapperTest {

    @Nested
    @DisplayName("fun map(projectView): Try<TargetsSpec> tests")
    inner class MapTest {

        @Nested
        @DisplayName("without derive_targets_from_targets")
        inner class MapTestWithoutderiveTargetsFromDirectoriesSection {
            @Test
            fun `should return success with default spec if targets are null`() {
                // given
                val projectView = ProjectView.Builder(targets = null).build().get()

                // when
                val targetsSpecTry = TargetsSpecMapper.map(projectView)

                // then
                targetsSpecTry.isSuccess shouldBe true
                val targetsSpec = targetsSpecTry.get()

                val expectedTargetsSpec = TargetsSpec(emptyList(), emptyList())
                targetsSpec shouldBe expectedTargetsSpec
            }

            @Test
            fun `should return success and default if targets have no values`() {
                // given
                val projectView = ProjectView.Builder(
                        targets = ProjectViewTargetsSection(
                                emptyList(),
                                emptyList()
                        )
                ).build().get()

                // when
                val targetsSpecTry = TargetsSpecMapper.map(projectView)

                // then
                targetsSpecTry.isSuccess shouldBe true
                val targetsSpec = targetsSpecTry.get()

                val expectedTargetsSpec = TargetsSpec(emptyList(), emptyList())
                targetsSpec shouldBe expectedTargetsSpec
            }

            @Test
            fun `should return fail if targets have no included values`() {
                // given
                val projectView = ProjectView.Builder(
                        targets = ProjectViewTargetsSection(
                                emptyList(),
                                listOf(
                                        BuildTargetIdentifier("//excluded_target1"),
                                        BuildTargetIdentifier("//excluded_target2"),
                                )
                        )
                ).build().get()

                // when
                val targetsSpecTry = TargetsSpecMapper.map(projectView)

                // then
                targetsSpecTry.isFailure shouldBe true
                targetsSpecTry.cause::class shouldBe ProjectViewToExecutionContextEntityMapperException::class
                targetsSpecTry.cause.message shouldBe "Mapping project view into 'targets' failed! 'targets' section has no included targets."
            }

            @Test
            fun `should return success for successful mapping`() {
                // given
                val projectView =
                        ProjectView.Builder(
                                targets = ProjectViewTargetsSection(
                                        listOf(
                                                BuildTargetIdentifier("//included_target1"),
                                                BuildTargetIdentifier("//included_target2"),
                                                BuildTargetIdentifier("//included_target3")
                                        ),
                                        listOf(
                                                BuildTargetIdentifier("//excluded_target1"),
                                                BuildTargetIdentifier("//excluded_target2"),
                                        ),
                                )
                        ).build().get()

                // when
                val targetsSpecTry = TargetsSpecMapper.map(projectView)

                // then
                targetsSpecTry.isSuccess shouldBe true
                val targetsSpec = targetsSpecTry.get()

                val expectedTargets =
                        TargetsSpec(
                                listOf(
                                        BuildTargetIdentifier("//included_target1"),
                                        BuildTargetIdentifier("//included_target2"),
                                        BuildTargetIdentifier("//included_target3"),
                                ),
                                listOf(
                                        BuildTargetIdentifier("//excluded_target1"),
                                        BuildTargetIdentifier("//excluded_target2"),
                                ),
                        )
                targetsSpec shouldBe expectedTargets
            }
        }

        @Nested
        @DisplayName("with derive_targets_from_targets false")
        inner class MapTestWithderiveTargetsFromDirectoriesSectionFalse {
            @Test
            fun `should return success with default spec if targets are null and flag is false`() {
                // given
                val projectView = ProjectView.Builder(
                        targets = null,
                        deriveTargetsFromDirectories = ProjectViewDeriveTargetsFromDirectoriesSection(false)).build().get()

                // when
                val targetsSpecTry = TargetsSpecMapper.map(projectView)

                // then
                targetsSpecTry.isSuccess shouldBe true
                val targetsSpec = targetsSpecTry.get()

                val expectedTargetsSpec = TargetsSpec(emptyList(), emptyList())
                targetsSpec shouldBe expectedTargetsSpec
            }

            @Test
            fun `should return success and default if targets have no values and flag is false`() {
                // given
                val projectView = ProjectView.Builder(
                        targets = ProjectViewTargetsSection(
                                emptyList(),
                                emptyList()
                        ),
                        deriveTargetsFromDirectories = ProjectViewDeriveTargetsFromDirectoriesSection(false)
                ).build().get()

                // when
                val targetsSpecTry = TargetsSpecMapper.map(projectView)

                // then
                targetsSpecTry.isSuccess shouldBe true
                val targetsSpec = targetsSpecTry.get()

                val expectedTargetsSpec = TargetsSpec(emptyList(), emptyList())
                targetsSpec shouldBe expectedTargetsSpec
            }

            @Test
            fun `should return fail if targets have no included values and flag is False`() {
                // given
                val projectView = ProjectView.Builder(
                        targets = ProjectViewTargetsSection(
                                emptyList(),
                                listOf(
                                        BuildTargetIdentifier("//excluded_target1"),
                                        BuildTargetIdentifier("//excluded_target2"),
                                )
                        ),
                        deriveTargetsFromDirectories = ProjectViewDeriveTargetsFromDirectoriesSection(false)
                ).build().get()

                // when
                val targetsSpecTry = TargetsSpecMapper.map(projectView)

                // then
                targetsSpecTry.isFailure shouldBe true
                targetsSpecTry.cause::class shouldBe ProjectViewToExecutionContextEntityMapperException::class
                targetsSpecTry.cause.message shouldBe "Mapping project view into 'targets' failed! 'targets' section has no included targets."
            }

            @Test
            fun `should return success for successful mapping`() {
                // given
                val projectView =
                        ProjectView.Builder(
                                targets = ProjectViewTargetsSection(
                                        listOf(
                                                BuildTargetIdentifier("//included_target1"),
                                                BuildTargetIdentifier("//included_target2"),
                                                BuildTargetIdentifier("//included_target3")
                                        ),
                                        listOf(
                                                BuildTargetIdentifier("//excluded_target1"),
                                                BuildTargetIdentifier("//excluded_target2"),
                                        ),
                                ),
                                deriveTargetsFromDirectories = ProjectViewDeriveTargetsFromDirectoriesSection(false)
                        ).build().get()

                // when
                val targetsSpecTry = TargetsSpecMapper.map(projectView)

                // then
                targetsSpecTry.isSuccess shouldBe true
                val targetsSpec = targetsSpecTry.get()

                val expectedTargets =
                        TargetsSpec(
                                listOf(
                                        BuildTargetIdentifier("//included_target1"),
                                        BuildTargetIdentifier("//included_target2"),
                                        BuildTargetIdentifier("//included_target3"),
                                ),
                                listOf(
                                        BuildTargetIdentifier("//excluded_target1"),
                                        BuildTargetIdentifier("//excluded_target2"),
                                ),
                        )
                targetsSpec shouldBe expectedTargets
            }
        }

        @Nested
        @DisplayName("with derive_targets_from_targets true")
        inner class MapTestWithderiveTargetsFromDirectoriesSectionTrue {
            @Test
            fun `should return success with default spec if targets and directories are null and flag is true`() {
                // given
                val projectView = ProjectView.Builder(
                        targets = null,
                        directories = null,
                        deriveTargetsFromDirectories = ProjectViewDeriveTargetsFromDirectoriesSection(true)).build().get()

                // when
                val targetsSpecTry = TargetsSpecMapper.map(projectView)

                // then
                targetsSpecTry.isSuccess shouldBe true
                val targetsSpec = targetsSpecTry.get()

                val expectedTargetsSpec = TargetsSpec(emptyList(), emptyList())
                targetsSpec shouldBe expectedTargetsSpec
            }

            @Test
            fun `should return success and default if targets and directories have no values and flag is true`() {
                // given
                val projectView = ProjectView.Builder(
                        targets = ProjectViewTargetsSection(
                                emptyList(),
                                emptyList()
                        ),
                        directories = ProjectViewDirectoriesSection(
                                emptyList(),
                                emptyList()
                        ),
                        deriveTargetsFromDirectories = ProjectViewDeriveTargetsFromDirectoriesSection(true)
                ).build().get()

                // when
                val targetsSpecTry = TargetsSpecMapper.map(projectView)

                // then
                targetsSpecTry.isSuccess shouldBe true
                val targetsSpec = targetsSpecTry.get()

                val expectedTargetsSpec = TargetsSpec(emptyList(), emptyList())
                targetsSpec shouldBe expectedTargetsSpec
            }

            @Test
            fun `should return fail if directories have no included values and flag is True`() {
                // given
                val projectView = ProjectView.Builder(
                        targets = ProjectViewTargetsSection(
                                emptyList(),
                                listOf(
                                        BuildTargetIdentifier("//excluded_target1"),
                                        BuildTargetIdentifier("//excluded_target2"),
                                )
                        ),
                        directories = ProjectViewDirectoriesSection(
                                emptyList(),
                                listOf(
                                        Path("excluded_target1"),
                                        Path("excluded_target2"),)
                        ),
                        deriveTargetsFromDirectories = ProjectViewDeriveTargetsFromDirectoriesSection(true)
                ).build().get()

                // when
                val targetsSpecTry = TargetsSpecMapper.map(projectView)

                // then
                targetsSpecTry.isFailure shouldBe true
                targetsSpecTry.cause::class shouldBe ProjectViewToExecutionContextEntityMapperException::class
                targetsSpecTry.cause.message shouldBe "Mapping project view into 'targets' failed! 'directories' section has no included targets."
            }

            @Test
            fun `should return success for successful mapping with empty targets`() {
                // given
                val projectView =
                        ProjectView.Builder(
                                targets = ProjectViewTargetsSection(
                                        emptyList(),
                                        emptyList()
                                ),
                                directories = ProjectViewDirectoriesSection(
                                        listOf(
                                                Path("included_dir1"),
                                                Path("included_dir2/"),
                                                Path("included_dir3")
                                        ),
                                        listOf(
                                                Path("excluded_dir1"),
                                                Path("excluded_dir2"),
                                        ),
                                ),
                                deriveTargetsFromDirectories = ProjectViewDeriveTargetsFromDirectoriesSection(true)
                        ).build().get()

                // when
                val targetsSpecTry = TargetsSpecMapper.map(projectView)

                // then
                targetsSpecTry.isSuccess shouldBe true
                val targetsSpec = targetsSpecTry.get()

                val expectedTargets =
                        TargetsSpec(
                                listOf(
                                        BuildTargetIdentifier("//included_dir1/..."),
                                        BuildTargetIdentifier("//included_dir2/..."),
                                        BuildTargetIdentifier("//included_dir3/..."),
                                ),
                                listOf(
                                        BuildTargetIdentifier("//excluded_dir1/..."),
                                        BuildTargetIdentifier("//excluded_dir2/..."),
                                ),
                        )
                targetsSpec shouldBe expectedTargets
            }

            @Test
            fun `should return success for successful mapping with non-empty targets`() {
                // given
                val projectView =
                        ProjectView.Builder(
                                targets = ProjectViewTargetsSection(
                                        listOf(
                                                BuildTargetIdentifier("//included_target1"),
                                                BuildTargetIdentifier("//included_target2"),
                                                BuildTargetIdentifier("//included_target3")
                                        ),
                                        listOf(
                                                BuildTargetIdentifier("//excluded_target1"),
                                                BuildTargetIdentifier("//excluded_target2"),
                                        )
                                ),
                                directories = ProjectViewDirectoriesSection(
                                        listOf(
                                                Path("included_dir1"),
                                                Path("included_dir2/"),
                                                Path("included_dir3")
                                        ),
                                        listOf(
                                                Path("excluded_dir1"),
                                                Path("excluded_dir2"),
                                        ),
                                ),
                                deriveTargetsFromDirectories = ProjectViewDeriveTargetsFromDirectoriesSection(true)
                        ).build().get()

                // when
                val targetsSpecTry = TargetsSpecMapper.map(projectView)

                // then
                targetsSpecTry.isSuccess shouldBe true
                val targetsSpec = targetsSpecTry.get()

                val expectedTargets =
                        TargetsSpec(
                                listOf(
                                        BuildTargetIdentifier("//included_target1"),
                                        BuildTargetIdentifier("//included_target2"),
                                        BuildTargetIdentifier("//included_target3"),
                                        BuildTargetIdentifier("//included_dir1/..."),
                                        BuildTargetIdentifier("//included_dir2/..."),
                                        BuildTargetIdentifier("//included_dir3/..."),
                                ),
                                listOf(
                                        BuildTargetIdentifier("//excluded_target1"),
                                        BuildTargetIdentifier("//excluded_target2"),
                                        BuildTargetIdentifier("//excluded_dir1/..."),
                                        BuildTargetIdentifier("//excluded_dir2/..."),
                                ),
                        )
                targetsSpec shouldBe expectedTargets
            }

            @Test
            fun `should return success for successful mapping with different directory cases`() {
                // given
                val projectView =
                        ProjectView.Builder(
                                targets = ProjectViewTargetsSection(
                                        emptyList(),
                                        emptyList()
                                ),
                                directories = ProjectViewDirectoriesSection(
                                        listOf(
                                                Path("included_dir1/"),
                                                Path("included_dir2/"),
                                                Path(".")
                                        ),
                                        listOf(
                                                Path("excluded_dir1/"),
                                                Path("excluded_dir2/"),
                                        ),
                                ),
                                deriveTargetsFromDirectories = ProjectViewDeriveTargetsFromDirectoriesSection(true)
                        ).build().get()

                // when
                val targetsSpecTry = TargetsSpecMapper.map(projectView)

                // then
                targetsSpecTry.isSuccess shouldBe true
                val targetsSpec = targetsSpecTry.get()

                val expectedTargets =
                        TargetsSpec(
                                listOf(
                                        BuildTargetIdentifier("//included_dir1/..."),
                                        BuildTargetIdentifier("//included_dir2/..."),
                                        BuildTargetIdentifier("//..."),
                                ),
                                listOf(
                                        BuildTargetIdentifier("//excluded_dir1/..."),
                                        BuildTargetIdentifier("//excluded_dir2/..."),
                                ),
                        )
                targetsSpec shouldBe expectedTargets
            }
        }
    }

    @Nested
    @DisplayName("fun default(): Try<TargetsSpec> tests")
    inner class DefaultTest {

        @Test
        fun `should return success with default spec`() {
            // given

            // when
            val targetsSpecTry = TargetsSpecMapper.default()

            // then
            targetsSpecTry.isSuccess shouldBe true
            val targetsSpec = targetsSpecTry.get()

            val expectedTargetsSpec = TargetsSpec(emptyList(), emptyList())
            targetsSpec shouldBe expectedTargetsSpec
        }
    }
}
