package org.jetbrains.bsp.bazel.workspacecontext

import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import io.kotest.matchers.shouldBe
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBazelBinarySection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBuildFlagsSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBuildManualTargetsSection
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
                ).build()

            // when
            val workspaceContext = WorkspaceContextConstructor.construct(projectView)

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

            val expectedDotBazelBspDirPathSpec = DotBazelBspDirPathSpec(Path("").toAbsolutePath().resolve(".bazelbsp"))
            workspaceContext.dotBazelBspDirPath shouldBe expectedDotBazelBspDirPathSpec

            val expectedBuildManualTargetsSpec = BuildManualTargetsSpec(false)
            workspaceContext.buildManualTargets shouldBe expectedBuildManualTargetsSpec

            val expectedImportDepthSpec = ImportDepthSpec(3)
            workspaceContext.importDepth shouldBe expectedImportDepthSpec
        }
    }

    @Nested
    @DisplayName("fun constructDefault(): WorkspaceContext tests")
    inner class ConstructDefaultTest {

        @Test
        fun `should return success and default workspace context`() {
            // given
            // when
            val workspaceContext = WorkspaceContextConstructor.constructDefault()

            // then
            val expectedWorkspaceContext = WorkspaceContext(
                targets = TargetsSpec(emptyList(), emptyList()),
                buildFlags = BuildFlagsSpec(emptyList()),
                // TODO - for now we don't have a framework to change classpath, i'll fix it later
                bazelBinary = BazelBinarySpecExtractor.default(),
                dotBazelBspDirPath = DotBazelBspDirPathSpec(Path("").toAbsolutePath().resolve(".bazelbsp")),
                buildManualTargets = BuildManualTargetsSpec(false),
                importDepth = ImportDepthSpec(0),
            )
            workspaceContext shouldBe expectedWorkspaceContext
        }
    }
}
