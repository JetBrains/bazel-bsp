package org.jetbrains.bsp.bazel.projectview.parser

import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBazelBinarySection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBuildFlagsSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBuildManualTargetsSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewDeriveTargetsFromDirectoriesSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewDirectoriesSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewEnabledRulesSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewImportDepthSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewTargetsSection
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.nio.file.NoSuchFileException
import kotlin.io.path.Path

class DefaultProjectViewParserTest {

    private lateinit var parser: ProjectViewParser

    @BeforeEach
    fun beforeEach() {
        // given
        parser = ProjectViewParserTestMock
    }

    @Nested
    @DisplayName("fun parse(projectViewFilePath): ProjectView tests")
    internal inner class ParseProjectViewFilePathTest {

        @Test
        fun `should return failure for not existing file`() {
            // given
            val projectViewFilePath = Path("/does/not/exist.bazelproject")

            // when
            val exception = shouldThrow<NoSuchFileException> { parser.parse(projectViewFilePath) }

            // then
            exception.message shouldBe "/does/not/exist.bazelproject"
        }

        @Test
        fun `should return failure for not existing imported file`() {
            // given
            val projectViewFilePath = Path("/projectview/file9ImportsNotExisting.bazelproject")

            // when
            val exception = shouldThrow<NoSuchFileException> { parser.parse(projectViewFilePath) }

            // then
            exception.message shouldBe "/projectview/does/not/exist.bazelproject"
        }

        @Test
        fun `should return empty targets section for file without targets section`() {
            // given
            val projectViewFilePath = Path("/projectview/without/targets.bazelproject")

            // when
            val projectView = parser.parse(projectViewFilePath)

            // then
            projectView.targets shouldBe null
        }

        @Test
        fun `should return empty bazel path for file without bazel path section`() {
            // given
            val projectViewFilePath = Path("/projectview/without/bazelbinary.bazelproject")

            // when
            val projectView = parser.parse(projectViewFilePath)

            // then

            projectView.bazelBinary shouldBe null
        }

        @Test
        fun `should return null import depth for file without import depth section`() {
            // given
            val projectViewFilePath = Path("/projectview/without/importdepth.bazelproject")

            // when
            val projectView = parser.parse(projectViewFilePath)

            // then

            projectView.importDepth shouldBe null
        }

        @Test
        fun `should return empty build flags section for file without build flags section`() {
            // given
            val projectViewFilePath = Path("/projectview/without/buildflags.bazelproject")

            // when
            val projectView = parser.parse(projectViewFilePath)

            // then

            projectView.buildFlags shouldBe null
        }

        @Test
        fun `should return empty build manual targets section for file without build manual targets section`() {
            // given
            val projectViewFilePath = Path("/projectview/without/build_manual_targets.bazelproject")

            // when
            val projectView = parser.parse(projectViewFilePath)

            // then

            projectView.buildManualTargets shouldBe null
        }

        @Test
        fun `should return empty directories section for file without directories section`() {
            // given
            val projectViewFilePath = Path("/projectview/without/directories.bazelproject")

            // when
            val projectView = parser.parse(projectViewFilePath)

            // then

            projectView.directories shouldBe null
        }

        @Test
        fun `should return empty derive_targets_from_directories section for file without derive_targets_from_directories section`() {
            // given
            val projectViewFilePath = Path("/projectview/without/derive_targets_from_directories.bazelproject")

            // when
            val projectView = parser.parse(projectViewFilePath)

            // then

            projectView.deriveTargetsFromDirectories shouldBe null
        }

        @Test
        fun `should parse empty file`() {
            // given
            val projectViewFilePath = Path("/projectview/empty.bazelproject")

            // when
            val projectView = parser.parse(projectViewFilePath)

            // then

            val expectedProjectView = ProjectView(
                targets = null,
                bazelBinary = null,
                buildFlags = null,
                buildManualTargets = null,
                directories = null,
                deriveTargetsFromDirectories = null,
                importDepth = null,
                enabledRules = null,
            )

            projectView shouldBe expectedProjectView
        }

        @Test
        fun `should parse file with all sections`() {
            // given
            val projectViewFilePath = Path("/projectview/file1.bazelproject")

            // when
            val projectView = parser.parse(projectViewFilePath)

            // then

            val expectedProjectView = ProjectView(
                targets = ProjectViewTargetsSection(
                    listOf(
                        BuildTargetIdentifier("//included_target1.1"),
                        BuildTargetIdentifier("//included_target1.2")
                    ),
                    listOf(
                        BuildTargetIdentifier("//excluded_target1.1")
                    )
                ),
                bazelBinary = ProjectViewBazelBinarySection(Path("path1/to/bazel")),
                buildFlags = ProjectViewBuildFlagsSection(
                    listOf(
                        "--build_flag1.1=value1.1", "--build_flag1.2=value1.2"
                    )
                ),
                buildManualTargets = ProjectViewBuildManualTargetsSection(false),
                directories = ProjectViewDirectoriesSection(
                    listOf(
                        Path("included_dir1.1"),
                        Path("included_dir1.2")
                    ),
                    listOf(
                        Path("excluded_dir1.1")
                    )
                ),
                deriveTargetsFromDirectories = ProjectViewDeriveTargetsFromDirectoriesSection(true),
                importDepth = ProjectViewImportDepthSection(1),
                enabledRules = null,
            )
            projectView shouldBe expectedProjectView
        }

        @Test
        fun `should parse file with single imported file without singleton values`() {
            // given
            val projectViewFilePath = Path("/projectview/file4ImportsFile1.bazelproject")

            // when
            val projectView = parser.parse(projectViewFilePath)

            // then

            val expectedProjectView = ProjectView(
                targets = ProjectViewTargetsSection(
                    listOf(
                        BuildTargetIdentifier("//included_target1.1"),
                        BuildTargetIdentifier("//included_target1.2"),
                        BuildTargetIdentifier("//included_target4.1")
                    ), listOf(
                        BuildTargetIdentifier("//excluded_target1.1"),
                        BuildTargetIdentifier("//excluded_target4.1"),
                        BuildTargetIdentifier("//excluded_target4.2")
                    )
                ),
                bazelBinary = ProjectViewBazelBinarySection(Path("path1/to/bazel")),
                buildFlags = ProjectViewBuildFlagsSection(
                    listOf(
                        "--build_flag1.1=value1.1",
                        "--build_flag1.2=value1.2",
                        "--build_flag4.1=value4.1",
                        "--build_flag4.2=value4.2",
                        "--build_flag4.3=value4.3",
                    )
                ),
                buildManualTargets = ProjectViewBuildManualTargetsSection(false),
                directories = ProjectViewDirectoriesSection(
                    listOf(
                        Path("included_dir1.1"),
                        Path("included_dir1.2"),
                        Path("included_dir4.1"),
                        Path("included_dir4.2")
                    ),
                    listOf(
                        Path("excluded_dir1.1"),
                        Path("excluded_dir4.1")
                    )
                ),
                deriveTargetsFromDirectories = ProjectViewDeriveTargetsFromDirectoriesSection(true),
                importDepth = ProjectViewImportDepthSection(1),
                enabledRules = null,
            )
            projectView shouldBe expectedProjectView
        }

        @Test
        fun `should parse file with single imported file with singleton values`() {
            // given
            val projectViewFilePath = Path("/projectview/file7ImportsFile1.bazelproject")

            // when
            val projectView = parser.parse(projectViewFilePath)

            // then
            print(projectView)

            val expectedProjectView = ProjectView(
                targets = ProjectViewTargetsSection(
                    listOf(
                        BuildTargetIdentifier("//included_target1.1"),
                        BuildTargetIdentifier("//included_target1.2"),
                        BuildTargetIdentifier("//included_target7.1")
                    ), listOf(
                        BuildTargetIdentifier("//excluded_target1.1"),
                        BuildTargetIdentifier("//excluded_target7.1"),
                        BuildTargetIdentifier("//excluded_target7.2")
                    )
                ),
                bazelBinary = ProjectViewBazelBinarySection(Path("path7/to/bazel")),
                buildFlags = ProjectViewBuildFlagsSection(
                    listOf(
                        "--build_flag1.1=value1.1",
                        "--build_flag1.2=value1.2",
                        "--build_flag7.1=value7.1",
                        "--build_flag7.2=value7.2",
                        "--build_flag7.3=value7.3",
                    )
                ),
                buildManualTargets = ProjectViewBuildManualTargetsSection(true),
                directories = ProjectViewDirectoriesSection(
                    listOf(
                        Path("included_dir1.1"),
                        Path("included_dir1.2")
                    ),
                    listOf(
                        Path("excluded_dir1.1")
                    )
                ),
                deriveTargetsFromDirectories = ProjectViewDeriveTargetsFromDirectoriesSection(false),
                importDepth = ProjectViewImportDepthSection(7),
                enabledRules = null,
            )
            projectView shouldBe expectedProjectView
        }

        @Test
        fun `should parse file with empty imported file`() {
            // given
            val projectViewFilePath = Path("/projectview/file8ImportsEmpty.bazelproject")

            // when
            val projectView = parser.parse(projectViewFilePath)

            // then

            val expectedProjectView = ProjectView(
                targets = ProjectViewTargetsSection(
                    listOf(BuildTargetIdentifier("//included_target8.1")), listOf(
                        BuildTargetIdentifier("//excluded_target8.1"), BuildTargetIdentifier("//excluded_target8.2")
                    )
                ),
                bazelBinary = ProjectViewBazelBinarySection(Path("path8/to/bazel")),
                buildFlags = ProjectViewBuildFlagsSection(
                    listOf(
                        "--build_flag8.1=value8.1",
                        "--build_flag8.2=value8.2",
                        "--build_flag8.3=value8.3",
                    )
                ),
                buildManualTargets = ProjectViewBuildManualTargetsSection(true),
                directories = ProjectViewDirectoriesSection(
                    listOf(
                        Path("included_dir8.1"),
                        Path("included_dir8.2")
                    ),
                    listOf(
                        Path("excluded_dir8.1"),
                    )
                ),
                deriveTargetsFromDirectories = ProjectViewDeriveTargetsFromDirectoriesSection(true),
                importDepth = ProjectViewImportDepthSection(8),
                enabledRules = null,
            )
            projectView shouldBe expectedProjectView
        }

        @Test
        fun `should parse file with three imported files`() {
            // given
            val projectViewFilePath = Path("/projectview/file5ImportsFile1File2File3.bazelproject")

            // when
            val projectView = parser.parse(projectViewFilePath)

            // then

            val expectedProjectView = ProjectView(
                targets = ProjectViewTargetsSection(
                    listOf(
                        BuildTargetIdentifier("//included_target1.1"),
                        BuildTargetIdentifier("//included_target1.2"),
                        BuildTargetIdentifier("//included_target2.1"),
                        BuildTargetIdentifier("//included_target3.1")
                    ), listOf(
                        BuildTargetIdentifier("//excluded_target1.1"),
                        BuildTargetIdentifier("//excluded_target2.1"),
                        BuildTargetIdentifier("//excluded_target5.1"),
                        BuildTargetIdentifier("//excluded_target5.2")
                    )
                ),
                bazelBinary = ProjectViewBazelBinarySection(Path("path3/to/bazel")),
                buildFlags = ProjectViewBuildFlagsSection(
                    listOf(
                        "--build_flag1.1=value1.1",
                        "--build_flag1.2=value1.2",
                        "--build_flag2.1=value2.1",
                        "--build_flag2.2=value2.2",
                        "--build_flag3.1=value3.1",
                        "--build_flag5.1=value5.1",
                        "--build_flag5.2=value5.2",
                    )
                ),
                buildManualTargets = ProjectViewBuildManualTargetsSection(false),
                directories = ProjectViewDirectoriesSection(
                    listOf(
                        Path("included_dir1.1"),
                        Path("included_dir1.2"),
                        Path("included_dir2.1"),
                        Path("included_dir3.1")
                    ),
                    listOf(
                        Path("excluded_dir1.1"),
                        Path("excluded_dir2.1"),
                        Path("excluded_dir3.1"),
                    )
                ),
                deriveTargetsFromDirectories = ProjectViewDeriveTargetsFromDirectoriesSection(false),
                importDepth = ProjectViewImportDepthSection(3),
                enabledRules = null,
            )
            projectView shouldBe expectedProjectView
        }

        @Test
        fun `should parse file with nested imported files`() {
            // given
            val projectViewFilePath = Path("/projectview/file6ImportsFile2File3File4.bazelproject")

            // when
            val projectView = parser.parse(projectViewFilePath)

            // then

            val expectedProjectView = ProjectView(
                targets = ProjectViewTargetsSection(
                    listOf(
                        BuildTargetIdentifier("//included_target2.1"),
                        BuildTargetIdentifier("//included_target3.1"),
                        BuildTargetIdentifier("//included_target1.1"),
                        BuildTargetIdentifier("//included_target1.2"),
                        BuildTargetIdentifier("//included_target4.1")
                    ), listOf(
                        BuildTargetIdentifier("//excluded_target2.1"),
                        BuildTargetIdentifier("//excluded_target1.1"),
                        BuildTargetIdentifier("//excluded_target4.1"),
                        BuildTargetIdentifier("//excluded_target4.2")
                    )
                ),
                bazelBinary = ProjectViewBazelBinarySection(Path("path1/to/bazel")),
                buildFlags = ProjectViewBuildFlagsSection(
                    listOf(
                        "--build_flag2.1=value2.1",
                        "--build_flag2.2=value2.2",
                        "--build_flag3.1=value3.1",
                        "--build_flag1.1=value1.1",
                        "--build_flag1.2=value1.2",
                        "--build_flag4.1=value4.1",
                        "--build_flag4.2=value4.2",
                        "--build_flag4.3=value4.3",
                    )
                ),
                buildManualTargets = ProjectViewBuildManualTargetsSection(false),
                directories = ProjectViewDirectoriesSection(
                    listOf(
                        Path("included_dir2.1"),
                        Path("included_dir3.1"),
                        Path("included_dir1.1"),
                        Path("included_dir1.2"),
                        Path("included_dir4.1"),
                        Path("included_dir4.2")
                    ),
                    listOf(
                        Path("excluded_dir2.1"),
                        Path("excluded_dir3.1"),
                        Path("excluded_dir1.1"),
                        Path("excluded_dir4.1"),
                    )
                ),
                deriveTargetsFromDirectories = ProjectViewDeriveTargetsFromDirectoriesSection(true),
                importDepth = ProjectViewImportDepthSection(1),
                enabledRules = null,
            )
            projectView shouldBe expectedProjectView
        }


        @Test
        fun `should parse enabled rules`() {
            // given
            val projectViewFilePath = Path("/projectview/enabled.bazelproject")

            // when
            val projectView = parser.parse(projectViewFilePath)

            // then

            val expectedProjectView = ProjectView(
                targets = null,
                bazelBinary = null,
                buildFlags = null,
                buildManualTargets = null,
                directories = null,
                deriveTargetsFromDirectories = null,
                importDepth = null,
                enabledRules = ProjectViewEnabledRulesSection(listOf("io_bazel_rules_scala", "rules_jvm", "rules_java")),
            )

            projectView shouldBe expectedProjectView
        }
    }
}
