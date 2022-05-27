package org.jetbrains.bsp.bazel.projectview.parser

import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import io.kotest.matchers.shouldBe
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import org.jetbrains.bsp.bazel.projectview.model.sections.*
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
            val projectViewTry = parser.parse(projectViewFilePath)

            // then
            projectViewTry.isFailure shouldBe true
            projectViewTry.cause::class shouldBe NoSuchFileException::class
            projectViewTry.cause.message shouldBe "/does/not/exist.bazelproject"
        }

        @Test
        fun `should return failure for not existing imported file`() {
            // given
            val projectViewFilePath = Path("/projectview/file9ImportsNotExisting.bazelproject")

            // when
            val projectViewTry = parser.parse(projectViewFilePath)

            // then
            projectViewTry.isFailure shouldBe true
            projectViewTry.cause::class shouldBe NoSuchFileException::class
            projectViewTry.cause.message shouldBe "/projectview/does/not/exist.bazelproject"
        }

        @Test
        fun `should return empty targets section for file without targets section`() {
            // given
            val projectViewFilePath = Path("/projectview/without/targets.bazelproject")

            // when
            val projectViewTry = parser.parse(projectViewFilePath)

            // then
            projectViewTry.isSuccess shouldBe true
            val projectView = projectViewTry.get()

            projectView.targets shouldBe null
        }

        @Test
        fun `should return empty bazel path for file without bazel path section`() {
            // given
            val projectViewFilePath = Path("/projectview/without/bazelpath.bazelproject")

            // when
            val projectViewTry = parser.parse(projectViewFilePath)

            // then
            projectViewTry.isSuccess shouldBe true
            val projectView = projectViewTry.get()

            projectView.bazelPath shouldBe null
        }

        @Test
        fun `should return empty debugger address for file without debugger address section`() {
            // given
            val projectViewFilePath = Path("/projectview/without/debuggeraddress.bazelproject")

            // when
            val projectViewTry = parser.parse(projectViewFilePath)

            // then
            projectViewTry.isSuccess shouldBe true
            val projectView = projectViewTry.get()

            projectView.debuggerAddress shouldBe null
        }

        @Test
        fun `should return empty java path section for file without java path section`() {
            // given
            val projectViewFilePath = Path("/projectview/without/javapath.bazelproject")

            // when
            val projectViewTry = parser.parse(projectViewFilePath)

            // then
            projectViewTry.isSuccess shouldBe true
            val projectView = projectViewTry.get()

            projectView.javaPath shouldBe null
        }

        @Test
        fun `should return empty build flags section for file without build flags section`() {
            // given
            val projectViewFilePath = Path("/projectview/without/buildflags.bazelproject")

            // when
            val projectViewTry = parser.parse(projectViewFilePath)

            // then
            projectViewTry.isSuccess shouldBe true
            val projectView = projectViewTry.get()

            projectView.buildFlags shouldBe null
        }

        @Test
        fun `should return empty build manual targets section for file without build manual targets section`() {
            // given
            val projectViewFilePath = Path("/projectview/without/build_manual_targets.bazelproject")

            // when
            val projectViewTry = parser.parse(projectViewFilePath)

            // then
            projectViewTry.isSuccess shouldBe true
            val projectView = projectViewTry.get()

            projectView.buildManualTargets shouldBe null
        }

        @Test
        fun `should parse empty file`() {
            // given
            val projectViewFilePath = Path("/projectview/empty.bazelproject")

            // when
            val projectViewTry = parser.parse(projectViewFilePath)

            // then
            projectViewTry.isSuccess shouldBe true
            val projectView = projectViewTry.get()

            val expectedProjectView = ProjectView(
                targets = null,
                bazelPath = null,
                debuggerAddress = null,
                javaPath = null,
                buildFlags = null,
                buildManualTargets = null,
            )

            projectView shouldBe expectedProjectView
        }

        @Test
        fun `should parse file with all sections`() {
            // given
            val projectViewFilePath = Path("/projectview/file1.bazelproject")

            // when
            val projectViewTry = parser.parse(projectViewFilePath)

            // then
            projectViewTry.isSuccess shouldBe true
            val projectView = projectViewTry.get()

            val expectedProjectView = ProjectView(
                targets = ProjectViewTargetsSection(
                    listOf(
                        BuildTargetIdentifier("//included_target1.1"), BuildTargetIdentifier("//included_target1.2")
                    ), listOf(BuildTargetIdentifier("//excluded_target1.1"))
                ),
                bazelPath = ProjectViewBazelPathSection(Path("path1/to/bazel")),
                debuggerAddress = ProjectViewDebuggerAddressSection("0.0.0.1:8000"),
                javaPath = ProjectViewJavaPathSection(Path("path1/to/java")),
                buildFlags = ProjectViewBuildFlagsSection(
                    listOf(
                        "--build_flag1.1=value1.1", "--build_flag1.2=value1.2"
                    )
                ),
                buildManualTargets = ProjectViewBuildManualTargetsSection("false".toBoolean()),
            )
            projectView shouldBe expectedProjectView
        }

        @Test
        fun `should parse file with single imported file without singleton values`() {
            // given
            val projectViewFilePath = Path("/projectview/file4ImportsFile1.bazelproject")

            // when
            val projectViewTry = parser.parse(projectViewFilePath)

            // then
            projectViewTry.isSuccess shouldBe true
            val projectView = projectViewTry.get()

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
                bazelPath = ProjectViewBazelPathSection(Path("path1/to/bazel")),
                debuggerAddress = ProjectViewDebuggerAddressSection("0.0.0.1:8000"),
                javaPath = ProjectViewJavaPathSection(Path("path1/to/java")),
                buildFlags = ProjectViewBuildFlagsSection(
                    listOf(
                        "--build_flag1.1=value1.1",
                        "--build_flag1.2=value1.2",
                        "--build_flag4.1=value4.1",
                        "--build_flag4.2=value4.2",
                        "--build_flag4.3=value4.3",
                    )
                ),
                buildManualTargets = ProjectViewBuildManualTargetsSection("false".toBoolean()),
            )
            projectView shouldBe expectedProjectView
        }

        @Test
        fun `should parse file with single imported file with singleton values`() {
            // given
            val projectViewFilePath = Path("/projectview/file7ImportsFile1.bazelproject")

            // when
            val projectViewTry = parser.parse(projectViewFilePath)

            // then
            print(projectViewTry)
            projectViewTry.isSuccess shouldBe true
            val projectView = projectViewTry.get()

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
                bazelPath = ProjectViewBazelPathSection(Path("path7/to/bazel")),
                debuggerAddress = ProjectViewDebuggerAddressSection("0.0.0.7:8000"),
                javaPath = ProjectViewJavaPathSection(Path("path7/to/java")),
                buildFlags = ProjectViewBuildFlagsSection(
                    listOf(
                        "--build_flag1.1=value1.1",
                        "--build_flag1.2=value1.2",
                        "--build_flag7.1=value7.1",
                        "--build_flag7.2=value7.2",
                        "--build_flag7.3=value7.3",
                    )
                ),
                buildManualTargets = ProjectViewBuildManualTargetsSection("false".toBoolean()),
            )
            projectView shouldBe expectedProjectView
        }

        @Test
        fun `should parse file with empty imported file`() {
            // given
            val projectViewFilePath = Path("/projectview/file8ImportsEmpty.bazelproject")

            // when
            val projectViewTry = parser.parse(projectViewFilePath)

            // then
            projectViewTry.isSuccess shouldBe true
            val projectView = projectViewTry.get()

            val expectedProjectView = ProjectView(
                targets = ProjectViewTargetsSection(
                    listOf(BuildTargetIdentifier("//included_target8.1")), listOf(
                        BuildTargetIdentifier("//excluded_target8.1"), BuildTargetIdentifier("//excluded_target8.2")
                    )
                ),
                bazelPath = ProjectViewBazelPathSection(Path("path8/to/bazel")),
                debuggerAddress = ProjectViewDebuggerAddressSection("0.0.0.8:8000"),
                javaPath = ProjectViewJavaPathSection(Path("path8/to/java")),
                buildFlags = ProjectViewBuildFlagsSection(
                    listOf(
                        "--build_flag8.1=value8.1",
                        "--build_flag8.2=value8.2",
                        "--build_flag8.3=value8.3",
                    )
                ),
                buildManualTargets = ProjectViewBuildManualTargetsSection("false".toBoolean()),
            )
            projectView shouldBe expectedProjectView
        }

        @Test
        fun `should parse file with three imported files`() {
            // given
            val projectViewFilePath = Path("/projectview/file5ImportsFile1File2File3.bazelproject")

            // when
            val projectViewTry = parser.parse(projectViewFilePath)

            // then
            projectViewTry.isSuccess shouldBe true
            val projectView = projectViewTry.get()

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
                bazelPath = ProjectViewBazelPathSection(Path("path3/to/bazel")),
                debuggerAddress = ProjectViewDebuggerAddressSection("0.0.0.3:8000"),
                javaPath = ProjectViewJavaPathSection(Path("path3/to/java")),
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
                buildManualTargets = ProjectViewBuildManualTargetsSection("false".toBoolean()),
            )
            projectView shouldBe expectedProjectView
        }

        @Test
        fun `should parse file with nested imported files`() {
            // given
            val projectViewFilePath = Path("/projectview/file6ImportsFile2File3File4.bazelproject")

            // when
            val projectViewTry = parser.parse(projectViewFilePath)

            // then
            projectViewTry.isSuccess shouldBe true
            val projectView = projectViewTry.get()

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
                bazelPath = ProjectViewBazelPathSection(Path("path1/to/bazel")),
                debuggerAddress = ProjectViewDebuggerAddressSection("0.0.0.1:8000"),
                javaPath = ProjectViewJavaPathSection(Path("path1/to/java")),
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
                buildManualTargets = ProjectViewBuildManualTargetsSection("false".toBoolean()),
            )
            projectView shouldBe expectedProjectView
        }
    }
}
