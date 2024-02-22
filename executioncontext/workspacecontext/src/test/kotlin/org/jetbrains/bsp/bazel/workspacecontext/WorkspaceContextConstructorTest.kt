package org.jetbrains.bsp.bazel.workspacecontext

import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import io.kotest.matchers.shouldBe
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBazelBinarySection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBuildFlagsSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBuildManualTargetsSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewDirectoriesSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewEnabledRulesSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewImportDepthSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewTargetsSection
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.io.path.Path

class WorkspaceContextConstructorTest {
    @Nested
    @DisplayName("fun construct(projectView: ProjectView): WorkspaceContext tests")
    inner class ConstructTest {

        @Test
        fun `should return success if project view is valid`() {
            // given
            val workspaceRoot = Path("path/to/workspace")
            val constructor = WorkspaceContextConstructor(workspaceRoot)
            val projectView =
                ProjectView.Builder(
                    targets =
                    ProjectViewTargetsSection(
                        listOf(
                            BuildTargetIdentifier("//included_target1"),
                            BuildTargetIdentifier("//included_target2"),
                            BuildTargetIdentifier("//included_target3")
                        ),
                        listOf(BuildTargetIdentifier("//excluded_target1")),
                    ),
                    directories = ProjectViewDirectoriesSection(
                        values = listOf(
                            Path("path/to/included1"),
                            Path("path/to/included2"),
                        ),
                        excludedValues = listOf(
                            Path("path/to/excluded"),
                        )
                    ),
                    buildFlags = ProjectViewBuildFlagsSection(
                        listOf(
                            "--build_flag1=value1",
                            "--build_flag2=value2",
                            "--build_flag3=value3",
                        )
                    ),
                    bazelBinary = ProjectViewBazelBinarySection(Path("/path/to/bazel")),
                    buildManualTargets = ProjectViewBuildManualTargetsSection(false),
                    importDepth = ProjectViewImportDepthSection(3),
                    enabledRules = ProjectViewEnabledRulesSection(listOf("rules_scala"))
                ).build()

            // when
            val workspaceContext = constructor.construct(projectView)

            // then
            val expectedTargets =
                TargetsSpec(
                    listOf(
                        BuildTargetIdentifier("//included_target1"),
                        BuildTargetIdentifier("//included_target2"),
                        BuildTargetIdentifier("//included_target3")
                    ), listOf(BuildTargetIdentifier("//excluded_target1"))
                )
            workspaceContext.targets shouldBe expectedTargets

            val expectedDirectories = DirectoriesSpec(
                values = listOf(
                    workspaceRoot.resolve("path/to/included1"),
                    workspaceRoot.resolve("path/to/included2")
                ),
                excludedValues = listOf(
                    workspaceRoot.resolve("path/to/excluded"),
                )
            )
            workspaceContext.directories shouldBe expectedDirectories
            val expectedBuildFlagsSpec = BuildFlagsSpec(
                listOf(
                    "--build_flag1=value1",
                    "--build_flag2=value2",
                    "--build_flag3=value3",
                )
            )
            workspaceContext.buildFlags shouldBe expectedBuildFlagsSpec

            val expectedBazelBinarySpec = BazelBinarySpec(Path("/path/to/bazel"))
            workspaceContext.bazelBinary shouldBe expectedBazelBinarySpec

            val expectedDotBazelBspDirPathSpec = DotBazelBspDirPathSpec(workspaceRoot.toAbsolutePath().resolve(".bazelbsp"))
            workspaceContext.dotBazelBspDirPath shouldBe expectedDotBazelBspDirPathSpec

            val expectedBuildManualTargetsSpec = BuildManualTargetsSpec(false)
            workspaceContext.buildManualTargets shouldBe expectedBuildManualTargetsSpec

            val expectedImportDepthSpec = ImportDepthSpec(3)
            workspaceContext.importDepth shouldBe expectedImportDepthSpec

            val expectedEnabledRules = EnabledRulesSpec(listOf("rules_scala"))
            workspaceContext.enabledRules shouldBe expectedEnabledRules
        }
    }
}
