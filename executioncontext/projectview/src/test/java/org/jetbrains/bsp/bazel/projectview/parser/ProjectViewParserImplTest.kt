package org.jetbrains.bsp.bazel.projectview.parser

import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import com.google.common.net.HostAndPort
import io.vavr.collection.List
import io.vavr.control.Option
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBazelPathSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBuildFlagsSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewDebuggerAddressSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewJavaPathSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewTargetsSection
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.FileNotFoundException
import java.nio.file.Paths

class ProjectViewParserImplTest {

    private lateinit var parser: ProjectViewParser

    @BeforeEach
    fun before() {
        // given
        parser = ProjectViewParserMockTestImpl()
    }

    @Nested
    @DisplayName("fun parse(projectViewFilePath): ProjectView tests")
    internal inner class ParseProjectViewFilePathTest {

        @Test
        fun `should return failure for not existing file`() {
            // given
            val projectViewFilePath = Paths.get("/does/not/exist.bazelproject")

            // when
            val projectViewTry = parser.parse(projectViewFilePath)

            // then
            assertThat(projectViewTry.isFailure).isTrue
            assertThat(projectViewTry.cause.javaClass).isEqualTo(FileNotFoundException::class.java)
            assertThat(projectViewTry.cause.message)
                .isEqualTo("/does/not/exist.bazelproject (No such file or directory)")
        }

        @Test
        fun `should return failure for not existing imported file`() {
            // given
            val projectViewFilePath = Paths.get("/projectview/file9ImportsNotExisting.bazelproject")

            // when
            val projectViewTry = parser.parse(projectViewFilePath)

            // then
            assertThat(projectViewTry.isFailure).isTrue
            assertThat(projectViewTry.cause.javaClass).isEqualTo(
                FileNotFoundException::class.java
            )
            assertThat(projectViewTry.cause.message)
                .isEqualTo("/projectview/does/not/exist.bazelproject (No such file or directory)")
        }

        @Test
        fun `should return empty targets section for file without targets section`() {
            // given
            val projectViewFilePath = Paths.get("/projectview/without/targets.bazelproject")

            // when
            val projectViewTry = parser.parse(projectViewFilePath)

            // then
            assertThat(projectViewTry.isSuccess).isTrue
            val projectView = projectViewTry.get()
            assertThat(projectView.targets).isEmpty()
        }

        @Test
        fun `should return empty bazel path for file without bazel path section`() {
            // given
            val projectViewFilePath = Paths.get("/projectview/without/bazelpath.bazelproject")

            // when
            val projectViewTry = parser.parse(projectViewFilePath)

            // then
            assertThat(projectViewTry.isSuccess).isTrue
            val projectView = projectViewTry.get()
            assertThat(projectView.bazelPath).isEmpty()
        }

        @Test
        fun `should return empty debugger address for file without debugger address section`() {
            // given
            val projectViewFilePath = Paths.get("/projectview/without/debuggeraddress.bazelproject")

            // when
            val projectViewTry = parser.parse(projectViewFilePath)

            // then
            assertThat(projectViewTry.isSuccess).isTrue
            val projectView = projectViewTry.get()
            assertThat(projectView.debuggerAddress).isEmpty()
        }

        @Test
        fun `should return empty java path section for file without java path section`() {
            // given
            val projectViewFilePath = Paths.get("/projectview/without/javapath.bazelproject")

            // when
            val projectViewTry = parser.parse(projectViewFilePath)

            // then
            assertThat(projectViewTry.isSuccess).isTrue
            val projectView = projectViewTry.get()
            assertThat(projectView.javaPath).isEmpty()
        }

        @Test
        fun `should return empty build flags section for file without build flags section`() {
            // given
            val projectViewFilePath = Paths.get("/projectview/without/buildflags.bazelproject")

            // when
            val projectViewTry = parser.parse(projectViewFilePath)

            // then
            assertThat(projectViewTry.isSuccess).isTrue
            val projectView = projectViewTry.get()
            assertThat(projectView.buildFlags).isEmpty()
        }

        @Test
        fun `should parse empty file`() {
            // given
            val projectViewFilePath = Paths.get("/projectview/empty.bazelproject")

            // when
            val projectViewTry = parser.parse(projectViewFilePath)

            // then
            val expectedProjectViewTry = ProjectView.builder()
                .targets(Option.none())
                .bazelPath(Option.none())
                .debuggerAddress(Option.none())
                .javaPath(Option.none())
                .build()
            assertThat(projectViewTry).isEqualTo(expectedProjectViewTry)
        }

        @Test
        fun `should parse file with all sections`() {
            // given
            val projectViewFilePath = Paths.get("/projectview/file1.bazelproject")

            // when
            val projectViewTry = parser.parse(projectViewFilePath)

            // then
            val expectedProjectViewTry = ProjectView.builder()
                .targets(
                    Option.of(
                        ProjectViewTargetsSection(
                            List.of(
                                BuildTargetIdentifier("//included_target1.1"),
                                BuildTargetIdentifier("//included_target1.2")
                            ),
                            List.of(BuildTargetIdentifier("//excluded_target1.1"))
                        )
                    )
                )
                .bazelPath(Option.of(ProjectViewBazelPathSection(Paths.get("path1/to/bazel"))))
                .debuggerAddress(
                    Option.of(
                        ProjectViewDebuggerAddressSection(
                            HostAndPort.fromString("0.0.0.1:8000")
                        )
                    )
                )
                .javaPath(Option.of(ProjectViewJavaPathSection(Paths.get("path1/to/java"))))
                .buildFlags(
                    Option.of(
                        ProjectViewBuildFlagsSection(
                            List.of(
                                "--build_flag1.1=value1.1",
                                "--build_flag1.2=value1.2"
                            )
                        )
                    )
                )
                .build()
            assertThat(projectViewTry).isEqualTo(expectedProjectViewTry)
        }

        @Test
        fun `should parse file with single imported file without singleton values`() {
            // given
            val projectViewFilePath = Paths.get("/projectview/file4ImportsFile1.bazelproject")

            // when
            val projectViewTry = parser.parse(projectViewFilePath)

            // then
            val expectedProjectViewTry = ProjectView.builder()
                .targets(
                    Option.of(
                        ProjectViewTargetsSection(
                            List.of(
                                BuildTargetIdentifier("//included_target1.1"),
                                BuildTargetIdentifier("//included_target1.2"),
                                BuildTargetIdentifier("//included_target4.1")
                            ),
                            List.of(
                                BuildTargetIdentifier("//excluded_target1.1"),
                                BuildTargetIdentifier("//excluded_target4.1"),
                                BuildTargetIdentifier("//excluded_target4.2")
                            )
                        )
                    )
                )
                .bazelPath(Option.of(ProjectViewBazelPathSection(Paths.get("path1/to/bazel"))))
                .debuggerAddress(
                    Option.of(
                        ProjectViewDebuggerAddressSection(
                            HostAndPort.fromString("0.0.0.1:8000")
                        )
                    )
                )
                .javaPath(Option.of(ProjectViewJavaPathSection(Paths.get("path1/to/java"))))
                .buildFlags(
                    Option.of(
                        ProjectViewBuildFlagsSection(
                            List.of(
                                "--build_flag1.1=value1.1",
                                "--build_flag1.2=value1.2",
                                "--build_flag4.1=value4.1",
                                "--build_flag4.2=value4.2",
                                "--build_flag4.3=value4.3",
                            )
                        )
                    )
                )
                .build()
            assertThat(projectViewTry).isEqualTo(expectedProjectViewTry)
        }

        @Test
        fun `should parse file with single imported file with singleton values`() {
            // given
            val projectViewFilePath = Paths.get("/projectview/file7ImportsFile1.bazelproject")

            // when
            val projectViewTry = parser.parse(projectViewFilePath)

            // then
            val expectedProjectViewTry = ProjectView.builder()
                .targets(
                    Option.of(
                        ProjectViewTargetsSection(
                            List.of(
                                BuildTargetIdentifier("//included_target1.1"),
                                BuildTargetIdentifier("//included_target1.2"),
                                BuildTargetIdentifier("//included_target7.1")
                            ),
                            List.of(
                                BuildTargetIdentifier("//excluded_target1.1"),
                                BuildTargetIdentifier("//excluded_target7.1"),
                                BuildTargetIdentifier("//excluded_target7.2")
                            )
                        )
                    )
                )
                .bazelPath(Option.of(ProjectViewBazelPathSection(Paths.get("path7/to/bazel"))))
                .debuggerAddress(
                    Option.of(
                        ProjectViewDebuggerAddressSection(
                            HostAndPort.fromString("0.0.0.7:8000")
                        )
                    )
                )
                .javaPath(Option.of(ProjectViewJavaPathSection(Paths.get("path7/to/java"))))
                .buildFlags(
                    Option.of(
                        ProjectViewBuildFlagsSection(
                            List.of(
                                "--build_flag1.1=value1.1",
                                "--build_flag1.2=value1.2",
                                "--build_flag7.1=value7.1",
                                "--build_flag7.2=value7.2",
                                "--build_flag7.3=value7.3",
                            )
                        )
                    )
                )
                .build()
            assertThat(projectViewTry).isEqualTo(expectedProjectViewTry)
        }

        @Test
        fun `should parse file with empty imported file`() {
            // given
            val projectViewFilePath = Paths.get("/projectview/file8ImportsEmpty.bazelproject")

            // when
            val projectViewTry = parser.parse(projectViewFilePath)

            // then
            val expectedProjectViewTry = ProjectView.builder()
                .targets(
                    Option.of(
                        ProjectViewTargetsSection(
                            List.of(BuildTargetIdentifier("//included_target8.1")),
                            List.of(
                                BuildTargetIdentifier("//excluded_target8.1"),
                                BuildTargetIdentifier("//excluded_target8.2")
                            )
                        )
                    )
                )
                .bazelPath(Option.of(ProjectViewBazelPathSection(Paths.get("path8/to/bazel"))))
                .debuggerAddress(
                    Option.of(
                        ProjectViewDebuggerAddressSection(
                            HostAndPort.fromString("0.0.0.8:8000")
                        )
                    )
                )
                .javaPath(Option.of(ProjectViewJavaPathSection(Paths.get("path8/to/java"))))
                .buildFlags(
                    Option.of(
                        ProjectViewBuildFlagsSection(
                            List.of(
                                "--build_flag8.1=value8.1",
                                "--build_flag8.2=value8.2",
                                "--build_flag8.3=value8.3",
                            )
                        )
                    )
                )
                .build()
            assertThat(projectViewTry).isEqualTo(expectedProjectViewTry)
        }

        @Test
        fun `should parse file with three imported files`() {
            // given
            val projectViewFilePath = Paths.get("/projectview/file5ImportsFile1File2File3.bazelproject")

            // when
            val projectViewTry = parser.parse(projectViewFilePath)

            // then
            val expectedProjectViewTry = ProjectView.builder()
                .targets(
                    Option.of(
                        ProjectViewTargetsSection(
                            List.of(
                                BuildTargetIdentifier("//included_target1.1"),
                                BuildTargetIdentifier("//included_target1.2"),
                                BuildTargetIdentifier("//included_target2.1"),
                                BuildTargetIdentifier("//included_target3.1")
                            ),
                            List.of(
                                BuildTargetIdentifier("//excluded_target1.1"),
                                BuildTargetIdentifier("//excluded_target2.1"),
                                BuildTargetIdentifier("//excluded_target5.1"),
                                BuildTargetIdentifier("//excluded_target5.2")
                            )
                        )
                    )
                )
                .bazelPath(Option.of(ProjectViewBazelPathSection(Paths.get("path3/to/bazel"))))
                .debuggerAddress(
                    Option.of(
                        ProjectViewDebuggerAddressSection(
                            HostAndPort.fromString("0.0.0.3:8000")
                        )
                    )
                )
                .javaPath(Option.of(ProjectViewJavaPathSection(Paths.get("path3/to/java"))))
                .buildFlags(
                    Option.of(
                        ProjectViewBuildFlagsSection(
                            List.of(
                                "--build_flag1.1=value1.1",
                                "--build_flag1.2=value1.2",
                                "--build_flag5.1=value5.1",
                                "--build_flag5.2=value5.2",
                            )
                        )
                    )
                )
                .buildFlags(
                    Option.of(
                        ProjectViewBuildFlagsSection(
                            List.of(
                                "--build_flag1.1=value1.1",
                                "--build_flag1.2=value1.2",
                                "--build_flag2.1=value2.1",
                                "--build_flag2.2=value2.2",
                                "--build_flag3.1=value3.1",
                                "--build_flag5.1=value5.1",
                                "--build_flag5.2=value5.2",
                            )
                        )
                    )
                )
                .build()
            assertThat(projectViewTry).isEqualTo(expectedProjectViewTry)
        }

        @Test
        fun `should parse file with nested imported files`() {
            // given
            val projectViewFilePath = Paths.get("/projectview/file6ImportsFile2File3File4.bazelproject")

            // when
            val projectViewTry = parser.parse(projectViewFilePath)

            // then
            val expectedProjectViewTry = ProjectView.builder()
                .targets(
                    Option.of(
                        ProjectViewTargetsSection(
                            List.of(
                                BuildTargetIdentifier("//included_target2.1"),
                                BuildTargetIdentifier("//included_target3.1"),
                                BuildTargetIdentifier("//included_target1.1"),
                                BuildTargetIdentifier("//included_target1.2"),
                                BuildTargetIdentifier("//included_target4.1")
                            ),
                            List.of(
                                BuildTargetIdentifier("//excluded_target2.1"),
                                BuildTargetIdentifier("//excluded_target1.1"),
                                BuildTargetIdentifier("//excluded_target4.1"),
                                BuildTargetIdentifier("//excluded_target4.2")
                            )
                        )
                    )
                )
                .bazelPath(Option.of(ProjectViewBazelPathSection(Paths.get("path1/to/bazel"))))
                .debuggerAddress(
                    Option.of(
                        ProjectViewDebuggerAddressSection(
                            HostAndPort.fromString("0.0.0.1:8000")
                        )
                    )
                )
                .javaPath(Option.of(ProjectViewJavaPathSection(Paths.get("path1/to/java"))))
                .buildFlags(
                    Option.of(
                        ProjectViewBuildFlagsSection(
                            List.of(
                                "--build_flag2.1=value2.1",
                                "--build_flag2.2=value2.2",
                                "--build_flag3.1=value3.1",
                                "--build_flag1.1=value1.1",
                                "--build_flag1.2=value1.2",
                                "--build_flag4.1=value4.1",
                                "--build_flag4.2=value4.2",
                                "--build_flag4.3=value4.3",
                            )
                        )
                    )
                )
                .build()
            assertThat(projectViewTry).isEqualTo(expectedProjectViewTry)
        }
    }

    @Nested
    @DisplayName("ProjectView parse(projectViewFilePath, defaultProjectViewFilePath) tests")
    internal inner class ParseProjectViewFilePathDefaultProjectViewFilePathTest {
        @Test
        fun `should return failure for not existing default file`() {
            // given
            val projectViewFilePath = Paths.get("/projectview/file1.bazelproject")
            val defaultProjectViewFilePath = Paths.get("/does/not/exist.bazelproject")

            // when
            val projectViewTry = parser.parse(projectViewFilePath, defaultProjectViewFilePath)

            // then
            assertThat(projectViewTry.isFailure).isTrue
            assertThat(projectViewTry.cause.javaClass).isEqualTo(
                FileNotFoundException::class.java
            )
            assertThat(projectViewTry.cause.message)
                .isEqualTo("/does/not/exist.bazelproject (No such file or directory)")
        }

        @Test
        fun `should return failure for not existing File`() {
            // given
            val projectViewFilePath = Paths.get("/does/not/exist.bazelproject")
            val defaultProjectViewFilePath = Paths.get("/projectview/file1.bazelproject")

            // when
            val projectViewTry = parser.parse(projectViewFilePath, defaultProjectViewFilePath)

            // then
            assertThat(projectViewTry.isFailure).isTrue
            assertThat(projectViewTry.cause.javaClass).isEqualTo(
                FileNotFoundException::class.java
            )
            assertThat(projectViewTry.cause.message)
                .isEqualTo("/does/not/exist.bazelproject (No such file or directory)")
        }

        @Test
        fun `should return failure for not existing default file and not existing file`() {
            // given
            val projectViewFilePath = Paths.get("/does/not/exist.bazelproject")
            val defaultProjectViewFilePath = Paths.get("/does/not/exist.bazelproject")

            // when
            val projectViewTry = parser.parse(projectViewFilePath, defaultProjectViewFilePath)

            // then
            assertThat(projectViewTry.isFailure).isTrue
            assertThat(projectViewTry.cause.javaClass).isEqualTo(
                FileNotFoundException::class.java
            )
            assertThat(projectViewTry.cause.message)
                .isEqualTo("/does/not/exist.bazelproject (No such file or directory)")
        }

        @Test
        fun `should return file1 for empty default file`() {
            // given
            val projectViewFilePath = Paths.get("/projectview/file1.bazelproject")
            val defaultProjectViewFilePath = Paths.get("/projectview/empty.bazelproject")

            // when
            val projectViewTry = parser.parse(projectViewFilePath, defaultProjectViewFilePath)

            // then
            val expectedProjectViewTry = ProjectView.builder()
                .targets(
                    Option.of(
                        ProjectViewTargetsSection(
                            List.of(
                                BuildTargetIdentifier("//included_target1.1"),
                                BuildTargetIdentifier("//included_target1.2")
                            ),
                            List.of(BuildTargetIdentifier("//excluded_target1.1"))
                        )
                    )
                )
                .bazelPath(Option.of(ProjectViewBazelPathSection(Paths.get("path1/to/bazel"))))
                .debuggerAddress(
                    Option.of(
                        ProjectViewDebuggerAddressSection(
                            HostAndPort.fromString("0.0.0.1:8000")
                        )
                    )
                )
                .javaPath(Option.of(ProjectViewJavaPathSection(Paths.get("path1/to/java"))))
                .buildFlags(
                    Option.of(
                        ProjectViewBuildFlagsSection(
                            List.of(
                                "--build_flag1.1=value1.1",
                                "--build_flag1.2=value1.2"
                            )
                        )
                    )
                )
                .build()
            assertThat(projectViewTry).isEqualTo(expectedProjectViewTry)
        }

        @Test
        fun `should return empty targets for default file without targets section`() {
            // given
            val projectViewFilePath = Paths.get("/projectview/empty.bazelproject")
            val defaultProjectViewFilePath = Paths.get("/projectview/without/targets.bazelproject")

            // when
            val projectViewTry = parser.parse(projectViewFilePath, defaultProjectViewFilePath)

            // then
            assertThat(projectViewTry.isSuccess).isTrue
            val projectView = projectViewTry.get()
            assertThat(projectView.targets).isEmpty()
        }

        @Test
        fun `should return empty for default file without bazel path section`() {
            // given
            val projectViewFilePath = Paths.get("/projectview/empty.bazelproject")
            val defaultProjectViewFilePath = Paths.get("/projectview/without/bazelpath.bazelproject")

            // when
            val projectViewTry = parser.parse(projectViewFilePath, defaultProjectViewFilePath)

            // then
            assertThat(projectViewTry.isSuccess).isTrue
            val projectView = projectViewTry.get()
            assertThat(projectView.bazelPath).isEmpty()
        }

        @Test
        fun `should return empty for default file without debugger address section`() {
            // given
            val projectViewFilePath = Paths.get("/projectview/empty.bazelproject")
            val defaultProjectViewFilePath = Paths.get("/projectview/without/debuggeraddress.bazelproject")

            // when
            val projectViewTry = parser.parse(projectViewFilePath, defaultProjectViewFilePath)

            // then
            assertThat(projectViewTry.isSuccess).isTrue
            val projectView = projectViewTry.get()
            assertThat(projectView.debuggerAddress).isEmpty()
        }

        @Test
        fun `should return empty for default file without java path section`() {
            // given
            val projectViewFilePath = Paths.get("/projectview/empty.bazelproject")
            val defaultProjectViewFilePath = Paths.get("/projectview/without/javapath.bazelproject")

            // when
            val projectViewTry = parser.parse(projectViewFilePath, defaultProjectViewFilePath)

            // then
            assertThat(projectViewTry.isSuccess).isTrue
            val projectView = projectViewTry.get()
            assertThat(projectView.javaPath).isEmpty()
        }

        @Test
        fun `should parse file and skip defaults`() {
            // given
            val projectViewFilePath = Paths.get("/projectview/file1.bazelproject")
            val defaultProjectViewFilePath = Paths.get("/projectview/file2.bazelproject")

            // when
            val projectViewTry = parser.parse(projectViewFilePath, defaultProjectViewFilePath)

            // then
            val expectedProjectViewTry = ProjectView.builder()
                .targets(
                    Option.of(
                        ProjectViewTargetsSection(
                            List.of(
                                BuildTargetIdentifier("//included_target1.1"),
                                BuildTargetIdentifier("//included_target1.2")
                            ),
                            List.of(BuildTargetIdentifier("//excluded_target1.1"))
                        )
                    )
                )
                .bazelPath(Option.of(ProjectViewBazelPathSection(Paths.get("path1/to/bazel"))))
                .debuggerAddress(
                    Option.of(
                        ProjectViewDebuggerAddressSection(
                            HostAndPort.fromString("0.0.0.1:8000")
                        )
                    )
                )
                .javaPath(Option.of(ProjectViewJavaPathSection(Paths.get("path1/to/java"))))
                .buildFlags(
                    Option.of(
                        ProjectViewBuildFlagsSection(
                            List.of(
                                "--build_flag1.1=value1.1",
                                "--build_flag1.2=value1.2"
                            )
                        )
                    )
                )
                .build()
            assertThat(projectViewTry).isEqualTo(expectedProjectViewTry)
        }

        @Test
        fun `should parse file and use defaults`() {
            // given
            val projectViewFilePath = Paths.get("/projectview/empty.bazelproject")
            val defaultProjectViewFilePath = Paths.get("/projectview/file1.bazelproject")

            // when
            val projectViewTry = parser.parse(projectViewFilePath, defaultProjectViewFilePath)

            // then
            val expectedProjectViewTry = ProjectView.builder()
                .targets(
                    Option.of(
                        ProjectViewTargetsSection(
                            List.of(
                                BuildTargetIdentifier("//included_target1.1"),
                                BuildTargetIdentifier("//included_target1.2")
                            ),
                            List.of(BuildTargetIdentifier("//excluded_target1.1"))
                        )
                    )
                )
                .bazelPath(Option.of(ProjectViewBazelPathSection(Paths.get("path1/to/bazel"))))
                .debuggerAddress(
                    Option.of(
                        ProjectViewDebuggerAddressSection(
                            HostAndPort.fromString("0.0.0.1:8000")
                        )
                    )
                )
                .javaPath(Option.of(ProjectViewJavaPathSection(Paths.get("path1/to/java"))))
                .buildFlags(
                    Option.of(
                        ProjectViewBuildFlagsSection(
                            List.of(
                                "--build_flag1.1=value1.1",
                                "--build_flag1.2=value1.2"
                            )
                        )
                    )
                )
                .build()
            assertThat(projectViewTry).isEqualTo(expectedProjectViewTry)
        }

        @Test
        fun `should parse file and use defaults with empty imported file`() {
            // given
            val projectViewFilePath = Paths.get("/projectview/empty.bazelproject")
            val defaultProjectViewFilePath = Paths.get("/projectview/file8ImportsEmpty.bazelproject")

            // when
            val projectViewTry = parser.parse(projectViewFilePath, defaultProjectViewFilePath)

            // then
            val expectedProjectViewTry = ProjectView.builder()
                .targets(
                    Option.of(
                        ProjectViewTargetsSection(
                            List.of(BuildTargetIdentifier("//included_target8.1")),
                            List.of(
                                BuildTargetIdentifier("//excluded_target8.1"),
                                BuildTargetIdentifier("//excluded_target8.2")
                            )
                        )
                    )
                )
                .bazelPath(Option.of(ProjectViewBazelPathSection(Paths.get("path8/to/bazel"))))
                .debuggerAddress(
                    Option.of(
                        ProjectViewDebuggerAddressSection(
                            HostAndPort.fromString("0.0.0.8:8000")
                        )
                    )
                )
                .javaPath(Option.of(ProjectViewJavaPathSection(Paths.get("path8/to/java"))))
                .buildFlags(
                    Option.of(
                        ProjectViewBuildFlagsSection(
                            List.of(
                                "--build_flag8.1=value8.1",
                                "--build_flag8.2=value8.2",
                                "--build_flag8.3=value8.3",
                            )
                        )
                    )
                )
                .build()
            assertThat(projectViewTry).isEqualTo(expectedProjectViewTry)
        }
    }
}
