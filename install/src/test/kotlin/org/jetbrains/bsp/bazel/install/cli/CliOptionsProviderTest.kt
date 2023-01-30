package org.jetbrains.bsp.bazel.install.cli

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

                val expectedDebuggerAddress = "host:8000"
                cliOptions.projectViewCliOptions?.debuggerAddress shouldBe expectedDebuggerAddress
            }
        }

        @Nested
        @DisplayName("cliOptions.projectViewCliOptions.importDepth test")
        inner class ImportDepthTest {

            @Test
            fun `should return success if import depth is specified`() {
                // given
                val args = arrayOf("-i", "1")

                // when
                val provider = CliOptionsProvider(args)
                val cliOptionsTry = provider.getOptions()

                // then
                cliOptionsTry.isSuccess shouldBe true
                val cliOptions = cliOptionsTry.get()

                val expectedImportDepth = 1
                cliOptions.projectViewCliOptions?.importDepth shouldBe expectedImportDepth
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
                        "//included_target2",
                        "//included_target3",
                )

                // when
                val provider = CliOptionsProvider(args)
                val cliOptionsTry = provider.getOptions()

                // then
                cliOptionsTry.isSuccess shouldBe true
                val cliOptions = cliOptionsTry.get()

                val expectedTargets = listOf(
                        "//included_target1",
                        "//included_target2",
                        "//included_target3",
                )
                cliOptions.projectViewCliOptions?.targets shouldBe expectedTargets
            }
        }

        @Nested
        @DisplayName("cliOptions.projectViewCliOptions.excludedTargets test")
        inner class ExcludedTargetsTests {

            @Test
            fun `should return success if excluded targets are specified`() {
                // given
                val args = arrayOf(
                    "--excluded-targets",
                    "//excluded_target1",
                    "//excluded_target2",
                )

                // when
                val provider = CliOptionsProvider(args)
                val cliOptionsTry = provider.getOptions()

                // then
                cliOptionsTry.isSuccess shouldBe true
                val cliOptions = cliOptionsTry.get()

                val expectedTargets = listOf(
                    "//excluded_target1",
                    "//excluded_target2",
                )
                cliOptions.projectViewCliOptions?.excludedTargets shouldBe expectedTargets
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

        @Nested
        @DisplayName("cliOptions.projectViewCliOptions.buildManualTargets test")
        inner class BuildManualTargetsTest {

            @Test
            fun `should return success if build manual targets are specified`() {
                // given
                val args = arrayOf("-m")

                // when
                val provider = CliOptionsProvider(args)
                val cliOptionsTry = provider.getOptions()

                // then
                cliOptionsTry.isSuccess shouldBe true
                val cliOptions = cliOptionsTry.get()

                val expectedBuildManualTargets = true
                cliOptions.projectViewCliOptions?.buildManualTargets shouldBe expectedBuildManualTargets
            }

        }

        @Test
        fun `should return null if build manual targets are not specified`() {
            // given
            val args = arrayOf<String>()

            // when
            val provider = CliOptionsProvider(args)
            val cliOptionsTry = provider.getOptions()

            // then
            cliOptionsTry.isSuccess shouldBe true
            val cliOptions = cliOptionsTry.get()

            cliOptions.projectViewCliOptions?.buildManualTargets shouldBe null

        }

        @Nested
        @DisplayName("cliOptions.projectViewCliOptions.directoriesOption test")
        inner class DirectoriesOptionTest {
            @Test
            fun `should return success if directories are specified`() {
                // given
                val args = arrayOf(
                        "-r",
                        "included_dir1",
                        "included_dir2",
                )

                // when
                val provider = CliOptionsProvider(args)
                val cliOptionsTry = provider.getOptions()

                // then
                cliOptionsTry.isSuccess shouldBe true
                val cliOptions = cliOptionsTry.get()

                val expectedDirs = listOf(
                        "included_dir1",
                        "included_dir2",
                )
                cliOptions.projectViewCliOptions?.directories shouldBe expectedDirs
            }
        }

        @Nested
        @DisplayName("cliOptions.projectViewCliOptions.excludedDirectories test")
        inner class ExcludedDirectoriesTest {
            @Test
            fun `should return success if excluded directories are specified`() {
                // given
                val args = arrayOf(
                    "--excluded-directories",
                    "excluded_dir1",
                    "excluded_dir2",
                    "excluded_dir3",
                )

                // when
                val provider = CliOptionsProvider(args)
                val cliOptionsTry = provider.getOptions()

                // then
                cliOptionsTry.isSuccess shouldBe true
                val cliOptions = cliOptionsTry.get()

                val expectedDirs = listOf(
                    "excluded_dir1",
                    "excluded_dir2",
                    "excluded_dir3",
                )
                cliOptions.projectViewCliOptions?.excludedDirectories shouldBe expectedDirs
            }
        }

        @Nested
        @DisplayName("cliOptions.projectViewCliOptions.deriveTargetsFlagOption test")
        inner class DeriveTargetsFlagOptionTest {
            @Test
            fun `should return success if deriveTargetsFlag is specified`() {
                // given
                val args = arrayOf(
                        "-v"
                )

                // when
                val provider = CliOptionsProvider(args)
                val cliOptionsTry = provider.getOptions()

                // then
                cliOptionsTry.isSuccess shouldBe true
                val cliOptions = cliOptionsTry.get()

                cliOptions.projectViewCliOptions?.deriveTargetsFromDirectories shouldBe true
            }

            @Test
            fun `should return success if deriveTargetsFlag is not specified`() {
                // given
                val args = emptyArray<String>()

                // when
                val provider = CliOptionsProvider(args)
                val cliOptionsTry = provider.getOptions()

                // then
                cliOptionsTry.isSuccess shouldBe true
                val cliOptions = cliOptionsTry.get()

                cliOptions.projectViewCliOptions?.deriveTargetsFromDirectories shouldBe null
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
                    "//included_target2",
                    "//included_target3",
                    "--excluded-targets",
                    "//excluded_target1",
                    "//excluded_target2",
                    "-f",
                    "--build_flag1=value1",
                    "--build_flag1=value2",
                    "--build_flag1=value3",
                    "-m",
                    "-r",
                    "included_dir1",
                    "included_dir2",
                    "--excluded-directories",
                    "excluded_dir1",
                    "-v"
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

            val expectedDebuggerAddress = "host:8000"
            cliOptions.projectViewCliOptions?.debuggerAddress shouldBe expectedDebuggerAddress

            val expectedTargets = listOf(
                    "//included_target1",
                    "//included_target2",
                    "//included_target3",
            )
            cliOptions.projectViewCliOptions?.targets shouldBe expectedTargets

            val expectedExcludedTargets = listOf(
                    "//excluded_target1",
                    "//excluded_target2",
            )
            cliOptions.projectViewCliOptions?.excludedTargets shouldBe expectedExcludedTargets

            val expectedBuildFlags = listOf("--build_flag1=value1", "--build_flag1=value2", "--build_flag1=value3")
            cliOptions.projectViewCliOptions?.buildFlags shouldBe expectedBuildFlags

            cliOptions.projectViewCliOptions?.buildManualTargets shouldBe true

            val expectedDirs = listOf(
                    "included_dir1",
                    "included_dir2",
            )
            cliOptions.projectViewCliOptions?.directories shouldBe expectedDirs

            val expectedExcludedDirs = listOf(
                    "excluded_dir1",
            )
            cliOptions.projectViewCliOptions?.excludedDirectories shouldBe expectedExcludedDirs

            cliOptions.projectViewCliOptions?.deriveTargetsFromDirectories shouldBe true
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

            val expectedDebuggerAddress = "host:8000"
            cliOptions.projectViewCliOptions?.debuggerAddress shouldBe expectedDebuggerAddress

            val expectedBuildFlags = listOf("--build_flag1=value1", "--build_flag1=value2", "--build_flag1=value3")
            cliOptions.projectViewCliOptions?.buildFlags shouldBe expectedBuildFlags
        }

    }
}
