package org.jetbrains.bsp.bazel.install.cli

import com.google.common.net.HostAndPort
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.nio.file.Paths

class CliOptionsProviderTest {

    @Nested
    @DisplayName("cliOptions.workspaceRootDir tests")
    inner class WorkspaceRootDirTest {

        @Test
        fun `should return success and absolute path to provided dir if specified`() {
            // given
            val args = arrayOf("-d", "/path/to/dir")

            // when
            val provider = CliOptionsProvider(args)
            val cliOptionsTry = provider.getOptions()

            // then
            cliOptionsTry.isSuccess shouldBe true
            val cliOptions = cliOptionsTry.get()

            val expectedWorkspaceRootDir = Paths.get("/path/to/dir")
            cliOptions.workspaceRootDir shouldBe expectedWorkspaceRootDir
        }

        @Test
        fun `should return success and absolute path to working directory if not specified`() {
            // given
            val args = arrayOf<String>()

            // when
            val provider = CliOptionsProvider(args)
            val cliOptionsTry = provider.getOptions()

            // then
            cliOptionsTry.isSuccess shouldBe true
            val cliOptions = cliOptionsTry.get()

            val expectedWorkspaceRootDir = Paths.get("").toAbsolutePath()
            cliOptions.workspaceRootDir shouldBe expectedWorkspaceRootDir
        }

        @Test
        fun `should return success and absolute path to provided workspace root dir if not absolute specified`() {
            // given
            val args = arrayOf("-d", "path/to/dir")

            // when
            val provider = CliOptionsProvider(args)
            val cliOptionsTry = provider.getOptions()

            // then
            cliOptionsTry.isSuccess shouldBe true
            val cliOptions = cliOptionsTry.get()

            val expectedWorkspaceRootDir = Paths.get("path/to/dir").toAbsolutePath()
            cliOptions.workspaceRootDir shouldBe expectedWorkspaceRootDir
            cliOptions.workspaceRootDir.isAbsolute shouldBe true
        }

        @Test
        fun `should return success and absolute path to provided workspace root dir if relative specified`() {
            // given
            val args = arrayOf("-d", "../../path/to/dir")

            // when
            val provider = CliOptionsProvider(args)
            val cliOptionsTry = provider.getOptions()

            // then
            cliOptionsTry.isSuccess shouldBe true
            val cliOptions = cliOptionsTry.get()

            val expectedWorkspaceRootDir = Paths.get("")
                    .toAbsolutePath()
                    .parent
                    .parent
                    .resolve("path/to/dir")
            cliOptions.workspaceRootDir shouldBe expectedWorkspaceRootDir
            cliOptions.workspaceRootDir.isAbsolute shouldBe true
        }
    }

    @Nested
    @DisplayName("cliOptions.projectViewFilePath tests")
    inner class ProjectViewFilePathTest {

        @Test
        fun `should return success and null if path is not specified`() {
            // given
            val args = arrayOf<String>()

            // when
            val provider = CliOptionsProvider(args)
            val cliOptionsTry = provider.getOptions()

            // then
            cliOptionsTry.isSuccess shouldBe true
            val cliOptions = cliOptionsTry.get()

            cliOptions.projectViewFilePath shouldBe null
        }

        @Test
        fun `should return success and absolute path to provided project view file if absolute specified`() {
            // given
            val args = arrayOf("-p", "/path/to/projectview.bazelproject")

            // when
            val provider = CliOptionsProvider(args)
            val cliOptionsTry = provider.getOptions()

            // then
            cliOptionsTry.isSuccess shouldBe true
            val cliOptions = cliOptionsTry.get()

            val expectedProjectViewFilePath = Paths.get("/path/to/projectview.bazelproject")
            cliOptions.projectViewFilePath shouldBe expectedProjectViewFilePath
            cliOptions.projectViewFilePath?.isAbsolute shouldBe true
        }

        @Test
        fun `should return success and absolute path to provided project view file if not absolute specified`() {
            // given
            val args = arrayOf("-p", "path/to/projectview.bazelproject")

            // when
            val provider = CliOptionsProvider(args)
            val cliOptionsTry = provider.getOptions()

            // then
            cliOptionsTry.isSuccess shouldBe true
            val cliOptions = cliOptionsTry.get()

            val expectedProjectViewFilePath = Paths.get("path/to/projectview.bazelproject").toAbsolutePath()
            cliOptions.projectViewFilePath shouldBe expectedProjectViewFilePath
            cliOptions.projectViewFilePath?.isAbsolute shouldBe true
        }

        @Test
        fun `should return success and absolute path to provided project view file if relative specified`() {
            // given
            val args = arrayOf("-p", "../../path/to/projectview.bazelproject")

            // when
            val provider = CliOptionsProvider(args)
            val cliOptionsTry = provider.getOptions()

            // then
            cliOptionsTry.isSuccess shouldBe true
            val cliOptions = cliOptionsTry.get()

            val expectedProjectViewFilePath = Paths.get("")
                    .toAbsolutePath()
                    .parent
                    .parent
                    .resolve("path/to/projectview.bazelproject")
            cliOptions.projectViewFilePath shouldBe expectedProjectViewFilePath
            cliOptions.projectViewFilePath?.isAbsolute shouldBe true
        }
    }

    @Nested
    @DisplayName("cliOptions.projectViewCliOptions tests")
    inner class ProjectViewCliOptionsTest {

        @Test
        fun `should return success and null project view cli options if none of the generation flags has been specified`() {
            // given
            val args = arrayOf<String>()

            // when
            val provider = CliOptionsProvider(args)
            val cliOptionsTry = provider.getOptions()

            // then
            cliOptionsTry.isSuccess shouldBe true
            val cliOptions = cliOptionsTry.get()

            cliOptions.projectViewCliOptions shouldBe null
        }

        @Nested
        @DisplayName("cliOptions.projectViewCliOptions.javaPath tests")
        inner class JavaPathTest {

            @Test
            fun `should return success and absolute path to provided java file if java path is specified`() {
                // given
                val args = arrayOf("-j", "/path/to/java")

                // when
                val provider = CliOptionsProvider(args)
                val cliOptionsTry = provider.getOptions()

                // then
                cliOptionsTry.isSuccess shouldBe true
                val cliOptions = cliOptionsTry.get()

                val expectedJavaPath = Paths.get("/path/to/java")
                cliOptions.projectViewCliOptions?.javaPath shouldBe expectedJavaPath
            }

            @Test
            fun `should return success and absolute path to provided java file if not absolute specified`() {
                // given
                val args = arrayOf("-j", "path/to/java")

                // when
                val provider = CliOptionsProvider(args)
                val cliOptionsTry = provider.getOptions()

                // then
                cliOptionsTry.isSuccess shouldBe true
                val cliOptions = cliOptionsTry.get()

                val expectedJavaPath = Paths.get("path/to/java").toAbsolutePath()
                cliOptions.projectViewCliOptions?.javaPath shouldBe expectedJavaPath
                cliOptions.projectViewCliOptions?.javaPath?.isAbsolute shouldBe true
            }

            @Test
            fun `should return success and absolute path to provided java file if relative specified`() {
                // given
                val args = arrayOf("-j", "../../path/to/java")

                // when
                val provider = CliOptionsProvider(args)
                val cliOptionsTry = provider.getOptions()

                // then
                cliOptionsTry.isSuccess shouldBe true
                val cliOptions = cliOptionsTry.get()

                val expectedJavaPath = Paths.get("")
                        .toAbsolutePath()
                        .parent
                        .parent
                        .resolve("path/to/java")
                cliOptions.projectViewCliOptions?.javaPath shouldBe expectedJavaPath
                cliOptions.projectViewCliOptions?.javaPath?.isAbsolute shouldBe true
            }
        }

        @Nested
        @DisplayName("cliOptions.projectViewCliOptions.bazelPath tests")
        inner class BazelPathTest {

            @Test
            fun `should return success and null if path is not specified`() {
                // given
                val args = arrayOf<String>()

                // when
                val provider = CliOptionsProvider(args)
                val cliOptionsTry = provider.getOptions()

                // then
                cliOptionsTry.isSuccess shouldBe true
                val cliOptions = cliOptionsTry.get()

                cliOptions.projectViewCliOptions?.bazelPath shouldBe null
            }

            @Test
            fun `should return success and absolute path if bazel path is specified`() {
                // given
                val args = arrayOf("-b", "/path/to/bazel")

                // when
                val provider = CliOptionsProvider(args)
                val cliOptionsTry = provider.getOptions()

                // then
                cliOptionsTry.isSuccess shouldBe true
                val cliOptions = cliOptionsTry.get()

                val expectedBazelPath = Paths.get("/path/to/bazel")
                cliOptions.projectViewCliOptions?.bazelPath shouldBe expectedBazelPath
            }

            @Test
            fun `should return success and absolute path to provided java file if not absolute specified`() {
                // given
                val args = arrayOf("-b", "path/to/bazel")

                // when
                val provider = CliOptionsProvider(args)
                val cliOptionsTry = provider.getOptions()

                // then
                cliOptionsTry.isSuccess shouldBe true
                val cliOptions = cliOptionsTry.get()

                val expectedBazelPath = Paths.get("path/to/bazel").toAbsolutePath()
                cliOptions.projectViewCliOptions?.bazelPath shouldBe expectedBazelPath
                cliOptions.projectViewCliOptions?.bazelPath?.isAbsolute shouldBe true
            }

            @Test
            fun `should return success and absolute path to provided java file if relative specified`() {
                // given
                val args = arrayOf("-b", "../../path/to/bazel")

                // when
                val provider = CliOptionsProvider(args)
                val cliOptionsTry = provider.getOptions()

                // then
                cliOptionsTry.isSuccess shouldBe true
                val cliOptions = cliOptionsTry.get()

                val expectedBazelPath = Paths.get("")
                        .toAbsolutePath()
                        .parent
                        .parent
                        .resolve("path/to/bazel")
                cliOptions.projectViewCliOptions?.bazelPath shouldBe expectedBazelPath
                cliOptions.projectViewCliOptions?.bazelPath?.isAbsolute shouldBe true
            }
        }

        @Nested
        @DisplayName("cliOptions.projectViewCliOptions.debuggerAddress test")
        inner class DebuggerAddressTest {

            @Test
            fun `should return success if debugger address is specified`() {
                // given
                val args = arrayOf("-x", "host:8000")

                // when
                val provider = CliOptionsProvider(args)
                val cliOptionsTry = provider.getOptions()

                // then
                cliOptionsTry.isSuccess shouldBe true
                val cliOptions = cliOptionsTry.get()

                val expectedDebuggerAddress = HostAndPort.fromString("host:8000")
                cliOptions.projectViewCliOptions?.debuggerAddress shouldBe expectedDebuggerAddress
            }
        }

        @Nested
        @DisplayName("cliOptions.projectViewCliOptions.targets test")
        inner class TargetsTests {

            @Test
            fun `should return success if targets are specified`() {
                // given
                val args = arrayOf(
                        "-t",
                        "//included_target1",
                        "-//excluded_target1",
                        "//included_target2",
                        "//included_target3",
                        "-//excluded_target2",
                )

                // when
                val provider = CliOptionsProvider(args)
                val cliOptionsTry = provider.getOptions()

                // then
                cliOptionsTry.isSuccess shouldBe true
                val cliOptions = cliOptionsTry.get()

                val expectedTargets = listOf(
                        "//included_target1",
                        "-//excluded_target1",
                        "//included_target2",
                        "//included_target3",
                        "-//excluded_target2",
                )
                cliOptions.projectViewCliOptions?.targets shouldBe expectedTargets
            }
        }

        @Nested
        @DisplayName("cliOptions.projectViewCliOptions.buildFlags test")
        inner class BuildFlagsTest {

            @Test
            fun `should return success if build flags are specified`() {
                // given
                val args = arrayOf("-f", "--build_flag1=value1", "--build_flag1=value2", "--build_flag1=value3")

                // when
                val provider = CliOptionsProvider(args)
                val cliOptionsTry = provider.getOptions()

                // then
                cliOptionsTry.isSuccess shouldBe true
                val cliOptions = cliOptionsTry.get()

                val expectedBuildFlags = listOf("--build_flag1=value1", "--build_flag1=value2", "--build_flag1=value3")
                cliOptions.projectViewCliOptions?.buildFlags shouldBe expectedBuildFlags
            }
        }

        @Test
        fun `should return success if all flags are specified`() {
            // given
            val args = arrayOf(
                    "-d",
                    "/path/to/dir",
                    "-p",
                    "path/to/projectview.bazelproject",
                    "-j",
                    "/path/to/java",
                    "-b",
                    "/path/to/bazel",
                    "-x",
                    "host:8000",
                    "-t",
                    "//included_target1",
                    "-//excluded_target1",
                    "//included_target2",
                    "//included_target3",
                    "-//excluded_target2",
                    "-f",
                    "--build_flag1=value1",
                    "--build_flag1=value2",
                    "--build_flag1=value3"
            )

            // when
            val provider = CliOptionsProvider(args)
            val cliOptionsTry = provider.getOptions()

            // then
            cliOptionsTry.isSuccess shouldBe true
            val cliOptions = cliOptionsTry.get()

            val expectedWorkspaceRootDir = Paths.get("/path/to/dir")
            cliOptions.workspaceRootDir shouldBe expectedWorkspaceRootDir

            val expectedProjectViewFilePath = Paths.get("path/to/projectview.bazelproject").toAbsolutePath()
            cliOptions.projectViewFilePath shouldBe expectedProjectViewFilePath
            cliOptions.projectViewFilePath?.isAbsolute shouldBe true

            val expectedJavaPath = Paths.get("/path/to/java")
            cliOptions.projectViewCliOptions?.javaPath shouldBe expectedJavaPath

            val expectedBazelPath = Paths.get("/path/to/bazel")
            cliOptions.projectViewCliOptions?.bazelPath shouldBe expectedBazelPath

            val expectedDebuggerAddress = HostAndPort.fromString("host:8000")
            cliOptions.projectViewCliOptions?.debuggerAddress shouldBe expectedDebuggerAddress

            val expectedTargets = listOf(
                    "//included_target1",
                    "-//excluded_target1",
                    "//included_target2",
                    "//included_target3",
                    "-//excluded_target2",
            )
            cliOptions.projectViewCliOptions?.targets shouldBe expectedTargets

            val expectedBuildFlags = listOf("--build_flag1=value1", "--build_flag1=value2", "--build_flag1=value3")
            cliOptions.projectViewCliOptions?.buildFlags shouldBe expectedBuildFlags
        }

        @Test
        fun `should return success if half of all flags are specified`() {
            // given
            val args = arrayOf(
                    "-d",
                    "/path/to/dir",
                    "-j",
                    "/path/to/java",
                    "-x",
                    "host:8000",
                    "-f",
                    "--build_flag1=value1",
                    "--build_flag1=value2",
                    "--build_flag1=value3"
            )

            // when
            val provider = CliOptionsProvider(args)
            val cliOptionsTry = provider.getOptions()

            // then
            cliOptionsTry.isSuccess shouldBe true
            val cliOptions = cliOptionsTry.get()

            val expectedWorkspaceRootDir = Paths.get("/path/to/dir")
            cliOptions.workspaceRootDir shouldBe expectedWorkspaceRootDir

            val expectedJavaPath = Paths.get("/path/to/java")
            cliOptions.projectViewCliOptions?.javaPath shouldBe expectedJavaPath

            val expectedDebuggerAddress = HostAndPort.fromString("host:8000")
            cliOptions.projectViewCliOptions?.debuggerAddress shouldBe expectedDebuggerAddress

            val expectedBuildFlags = listOf("--build_flag1=value1", "--build_flag1=value2", "--build_flag1=value3")
            cliOptions.projectViewCliOptions?.buildFlags shouldBe expectedBuildFlags
        }
    }
}
