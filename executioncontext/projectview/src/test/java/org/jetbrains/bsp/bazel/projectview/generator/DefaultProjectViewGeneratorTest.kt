package org.jetbrains.bsp.bazel.projectview.generator

import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import io.kotest.matchers.shouldBe
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBazelBinarySection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBuildFlagsSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBuildManualTargetsSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewDeriveTargetsFromDirectoriesSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewDirectoriesSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewImportDepthSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewTargetsSection
import org.jetbrains.bsp.bazel.projectview.parser.DefaultProjectViewParser
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.Path
import kotlin.io.path.createTempDirectory
import kotlin.io.path.createTempFile
import kotlin.io.path.writeText

class DefaultProjectViewGeneratorTest {

    @Nested
    @DisplayName("fun generatePrettyString(projectView: ProjectView): String tests")
    inner class GeneratePrettyStringTest {

        @Test
        fun `should return empty line for project view with all null fields`() {
            // given
            val projectView = ProjectView(
                targets = null,
                bazelBinary = null,
                buildFlags = null,
                buildManualTargets = null,
                directories = null,
                deriveTargetsFromDirectories = null,
                importDepth = null,
            )

            // when
            val generatedString = DefaultProjectViewGenerator.generatePrettyString(projectView)

            // then
            generatedString shouldBe "\n"
        }

        @Test
        fun `should return pretty string only with targets for project view only with targets`() {
            // given
            val projectView = ProjectView(
                targets = ProjectViewTargetsSection(
                    listOf(
                        BuildTargetIdentifier("//included_target1"),
                        BuildTargetIdentifier("//included_target2"),
                        BuildTargetIdentifier("//included_target3"),
                    ),
                    listOf(
                        BuildTargetIdentifier("//excluded_target1"),
                        BuildTargetIdentifier("//excluded_target2"),
                    )
                ),
                bazelBinary = null,
                buildFlags = null,
                buildManualTargets = null,
                directories = null,
                deriveTargetsFromDirectories = null,
                importDepth = null,
            )

            // when
            val generatedString = DefaultProjectViewGenerator.generatePrettyString(projectView)

            // then
            val expectedGeneratedString =
                """
                targets:
                    //included_target1
                    //included_target2
                    //included_target3
                    -//excluded_target1
                    -//excluded_target2

                """.trimIndent()
            generatedString shouldBe expectedGeneratedString
        }

        @Test
        fun `should return pretty string only with bazel path for project view only with bazel path`() {
            // given
            val projectView = ProjectView(
                targets = null,
                bazelBinary = ProjectViewBazelBinarySection(Paths.get("/path/to/bazel")),
                buildFlags = null,
                buildManualTargets = null,
                directories = null,
                deriveTargetsFromDirectories = null,
                importDepth = null,
            )

            // when
            val generatedString = DefaultProjectViewGenerator.generatePrettyString(projectView)

            // then
            val expectedGeneratedString =
                """
                bazel_binary: /path/to/bazel

                """.trimIndent()
            generatedString shouldBe expectedGeneratedString
        }

        @Test
        fun `should return pretty string only with build flags for project view only with build flags`() {
            // given
            val projectView = ProjectView(
                targets = null,
                bazelBinary = null,
                buildFlags = ProjectViewBuildFlagsSection(
                    listOf(
                        "--build_flag1=value1",
                        "--build_flag2=value2",
                        "--build_flag3=value3",
                    )
                ),
                buildManualTargets = null,
                directories = null,
                deriveTargetsFromDirectories = null,
                importDepth = null,
            )

            // when
            val generatedString = DefaultProjectViewGenerator.generatePrettyString(projectView)

            // then
            val expectedGeneratedString =
                """
                build_flags:
                    --build_flag1=value1
                    --build_flag2=value2
                    --build_flag3=value3

                """.trimIndent()
            generatedString shouldBe expectedGeneratedString
        }

        @Test
        fun `should return pretty string only with directories for project view only with directories`() {
            // given
            val projectView = ProjectView(
                    targets = null,
                    bazelBinary = null,
                    buildFlags = null,
                    buildManualTargets = null,
                    directories = ProjectViewDirectoriesSection(
                            listOf(
                                    Path("included_dir1"),
                                    Path("included_dir2"),
                                    Path("included_dir3")
                            ),
                            listOf(
                                    Path("excluded_dir1"),
                                    Path("excluded_dir2")
                            )
                    ),
                    deriveTargetsFromDirectories = null,
                    importDepth = null,
            )

            // when
            val generatedString = DefaultProjectViewGenerator.generatePrettyString(projectView)

            // then
            val expectedGeneratedString =
                """
                directories:
                    included_dir1
                    included_dir2
                    included_dir3
                    -excluded_dir1
                    -excluded_dir2

                """.trimIndent()
            generatedString shouldBe expectedGeneratedString
        }

        @Test
        fun `should return pretty string only with derive_targets_from_directories for project view only with deriveTargetsFromDirectories`() {
            // given
            val projectView = ProjectView(
                    targets = null,
                    bazelBinary = null,
                    buildFlags = null,
                    buildManualTargets = null,
                    directories = null,
                    deriveTargetsFromDirectories = ProjectViewDeriveTargetsFromDirectoriesSection(true),
                    importDepth = null,
            )

            // when
            val generatedString = DefaultProjectViewGenerator.generatePrettyString(projectView)

            // then
            val expectedGeneratedString =
                """
                derive_targets_from_directories: true

                """.trimIndent()
            generatedString shouldBe expectedGeneratedString
        }

        @Test
        fun `should return pretty string only with import depth flag for project view only with import depth flag`() {
            // given
            val projectView = ProjectView(
                targets = null,
                bazelBinary = null,
                buildFlags = null,
                buildManualTargets = null,
                directories = null,
                deriveTargetsFromDirectories = null,
                importDepth = ProjectViewImportDepthSection(3),
            )

            // when
            val generatedString = DefaultProjectViewGenerator.generatePrettyString(projectView)

            // then
            val expectedGeneratedString =
                """
                import_depth: 3

                """.trimIndent()
            generatedString shouldBe expectedGeneratedString
        }

        @Test
        fun `should return pretty string only with manual tag for project view only with manual tag`() {
            // given
            val projectView = ProjectView(
                targets = null,
                bazelBinary = null,
                buildFlags = null,
                buildManualTargets = ProjectViewBuildManualTargetsSection(true),
                directories = null,
                deriveTargetsFromDirectories = null,
                importDepth = null,
            )

            // when
            val generatedString = DefaultProjectViewGenerator.generatePrettyString(projectView)

            // then
            val expectedGeneratedString =
                """
                |build_manual_targets: true
                |
                """.trimMargin()
            generatedString shouldBe expectedGeneratedString
        }

        @Test
        fun `should return pretty string with project view for project view with empty list sections`() {
            // given
            val projectView = ProjectView(
                targets = ProjectViewTargetsSection(emptyList(), emptyList()),
                bazelBinary = ProjectViewBazelBinarySection(Paths.get("/path/to/bazel")),
                buildFlags = ProjectViewBuildFlagsSection(emptyList()),
                buildManualTargets = ProjectViewBuildManualTargetsSection(true),
                directories = ProjectViewDirectoriesSection(emptyList(), emptyList()),
                deriveTargetsFromDirectories = ProjectViewDeriveTargetsFromDirectoriesSection(true),
                importDepth = ProjectViewImportDepthSection(3),
            )

            // when
            val generatedString = DefaultProjectViewGenerator.generatePrettyString(projectView)

            // then
            val expectedGeneratedString =
                """
                targets:

                bazel_binary: /path/to/bazel

                build_flags:
                
                build_manual_targets: true
                
                directories:
                
                derive_targets_from_directories: true

                import_depth: 3

                """.trimIndent()
            generatedString shouldBe expectedGeneratedString
        }

        @Test
        fun `should return pretty string with project view for partly filled project view`() {
            // given
            val projectView = ProjectView(
                targets = ProjectViewTargetsSection(
                    listOf(
                        BuildTargetIdentifier("//included_target1"),
                        BuildTargetIdentifier("//included_target2"),
                        BuildTargetIdentifier("//included_target3"),
                    ), emptyList()
                ),
                bazelBinary = null,
                buildFlags = ProjectViewBuildFlagsSection(
                    listOf(
                        "--build_flag1=value1",
                        "--build_flag2=value2",
                        "--build_flag3=value3",
                    )
                ),
                buildManualTargets = null,
                directories = null,
                deriveTargetsFromDirectories = null,
                importDepth = null,
            )

            // when
            val generatedString = DefaultProjectViewGenerator.generatePrettyString(projectView)

            // then
            val expectedGeneratedString =
                """
                targets:
                    //included_target1
                    //included_target2
                    //included_target3

                build_flags:
                    --build_flag1=value1
                    --build_flag2=value2
                    --build_flag3=value3

                """.trimIndent()
            generatedString shouldBe expectedGeneratedString
        }

        @Test
        fun `should return pretty string with project view for full project view`() {
            // given
            val projectView = ProjectView(
                targets = ProjectViewTargetsSection(
                    listOf(
                        BuildTargetIdentifier("//included_target1"),
                        BuildTargetIdentifier("//included_target2"),
                        BuildTargetIdentifier("//included_target3"),
                    ),
                    listOf(
                        BuildTargetIdentifier("//excluded_target1"),
                        BuildTargetIdentifier("//excluded_target2"),
                    )
                ),
                bazelBinary = ProjectViewBazelBinarySection(Paths.get("/path/to/bazel")),
                buildFlags = ProjectViewBuildFlagsSection(
                    listOf(
                        "--build_flag1=value1",
                        "--build_flag2=value2",
                        "--build_flag3=value3",
                    )
                ),
                buildManualTargets = ProjectViewBuildManualTargetsSection(true),
                directories = ProjectViewDirectoriesSection(
                        listOf(
                                Path("included_dir1"),
                                Path("included_dir2"),
                                Path("included_dir3")
                        ),
                        listOf(
                                Path("excluded_dir1"),
                                Path("excluded_dir2")
                        )
                ),
                deriveTargetsFromDirectories = ProjectViewDeriveTargetsFromDirectoriesSection(true),
                importDepth = ProjectViewImportDepthSection(3),
            )

            // when
            val generatedString = DefaultProjectViewGenerator.generatePrettyString(projectView)

            // then
            val expectedGeneratedString =
                """
                targets:
                    //included_target1
                    //included_target2
                    //included_target3
                    -//excluded_target1
                    -//excluded_target2

                bazel_binary: /path/to/bazel

                build_flags:
                    --build_flag1=value1
                    --build_flag2=value2
                    --build_flag3=value3

                build_manual_targets: true

                directories:
                    included_dir1
                    included_dir2
                    included_dir3
                    -excluded_dir1
                    -excluded_dir2

                derive_targets_from_directories: true

                import_depth: 3

                """.trimIndent()
            generatedString shouldBe expectedGeneratedString
        }
    }

    @Nested
    @DisplayName("fun generatePrettyStringAndSaveInFile(projectView: ProjectView, filePath: Path): Unit tests")
    inner class GeneratePrettyStringAndSaveInFileTest {

        private lateinit var tempRoot: Path

        @BeforeEach
        fun beforeEach() {
            tempRoot = createTempDirectory("test-temp-root")
        }

        @AfterEach
        fun afterEach() {
            tempRoot.toFile().deleteRecursively()
        }

        @Test
        fun `should return success and save project view in the file`() {
            // given
            val filePath = tempRoot.resolve("path/to/projectview.bazelproject")

            val projectView = ProjectView(
                targets = ProjectViewTargetsSection(
                    listOf(
                        BuildTargetIdentifier("//included_target1"),
                        BuildTargetIdentifier("//included_target2"),
                        BuildTargetIdentifier("//included_target3"),
                    ),
                    listOf(
                        BuildTargetIdentifier("//excluded_target1"),
                        BuildTargetIdentifier("//excluded_target2"),
                    )
                ),
                bazelBinary = ProjectViewBazelBinarySection(Paths.get("/path/to/bazel")),
                buildFlags = ProjectViewBuildFlagsSection(
                    listOf(
                        "--build_flag1=value1",
                        "--build_flag2=value2",
                        "--build_flag3=value3",
                    )
                ),
                buildManualTargets = ProjectViewBuildManualTargetsSection(true),
                directories = ProjectViewDirectoriesSection(
                        listOf(
                                Path("included_dir1"),
                                Path("included_dir2"),
                                Path("included_dir3")
                        ),
                        listOf(
                                Path("excluded_dir1"),
                                Path("excluded_dir2")
                        )
                ),
                deriveTargetsFromDirectories = ProjectViewDeriveTargetsFromDirectoriesSection(true),
                importDepth = ProjectViewImportDepthSection(3),
            )

            // when
            DefaultProjectViewGenerator.generatePrettyStringAndSaveInFile(projectView, filePath)

            // then
            val expectedFileContent =
                """
                targets:
                    //included_target1
                    //included_target2
                    //included_target3
                    -//excluded_target1
                    -//excluded_target2

                bazel_binary: /path/to/bazel

                build_flags:
                    --build_flag1=value1
                    --build_flag2=value2
                    --build_flag3=value3

                build_manual_targets: true

                directories:
                    included_dir1
                    included_dir2
                    included_dir3
                    -excluded_dir1
                    -excluded_dir2
               
                derive_targets_from_directories: true
                
                import_depth: 3
                
                """.trimIndent()
            Files.readString(filePath) shouldBe expectedFileContent
        }

        @Test
        fun `should return success and override project view in the file`() {
            // given
            val filePath = createTempFile("projectview", ".bazelproject")
            filePath.writeText("some random things, maybe previous project view")

            val projectView = ProjectView(
                targets = ProjectViewTargetsSection(
                    listOf(
                        BuildTargetIdentifier("//included_target1"),
                        BuildTargetIdentifier("//included_target2"),
                        BuildTargetIdentifier("//included_target3"),
                    ),
                    listOf(
                        BuildTargetIdentifier("//excluded_target1"),
                        BuildTargetIdentifier("//excluded_target2"),
                    )
                ),
                bazelBinary = ProjectViewBazelBinarySection(Paths.get("/path/to/bazel")),
                buildFlags = ProjectViewBuildFlagsSection(
                    listOf(
                        "--build_flag1=value1",
                        "--build_flag2=value2",
                        "--build_flag3=value3",
                    )
                ),
                buildManualTargets = ProjectViewBuildManualTargetsSection(true),
                directories = ProjectViewDirectoriesSection(
                        listOf(
                                Path("included_dir1"),
                                Path("included_dir2"),
                                Path("included_dir3")
                        ),
                        listOf(
                                Path("excluded_dir1"),
                                Path("excluded_dir2")
                        )
                ),
                deriveTargetsFromDirectories = ProjectViewDeriveTargetsFromDirectoriesSection(true),
                importDepth = ProjectViewImportDepthSection(3),
            )

            // when
            DefaultProjectViewGenerator.generatePrettyStringAndSaveInFile(projectView, filePath)

            // then
            val expectedFileContent =
                """
                targets:
                    //included_target1
                    //included_target2
                    //included_target3
                    -//excluded_target1
                    -//excluded_target2

                bazel_binary: /path/to/bazel

                build_flags:
                    --build_flag1=value1
                    --build_flag2=value2
                    --build_flag3=value3

                build_manual_targets: true

                directories:
                    included_dir1
                    included_dir2
                    included_dir3
                    -excluded_dir1
                    -excluded_dir2
               
                derive_targets_from_directories: true
                
                import_depth: 3
                
                """.trimIndent()
            Files.readString(filePath) shouldBe expectedFileContent
        }

        @Test
        fun `should return success and save project view with empty list sections in the file which should be parsable by the parser`() {
            // given
            val filePath = tempRoot.resolve("path/to/projectview.bazelproject")

            val projectView = ProjectView(
                targets = ProjectViewTargetsSection(emptyList(), emptyList()),
                bazelBinary = ProjectViewBazelBinarySection(Paths.get("/path/to/bazel")),
                buildFlags = ProjectViewBuildFlagsSection(emptyList()),
                buildManualTargets = ProjectViewBuildManualTargetsSection(true),
                directories = ProjectViewDirectoriesSection(emptyList(), emptyList()),
                deriveTargetsFromDirectories = ProjectViewDeriveTargetsFromDirectoriesSection(true),
                importDepth = ProjectViewImportDepthSection(3),
            )

            val parser = DefaultProjectViewParser()

            // when
            DefaultProjectViewGenerator.generatePrettyStringAndSaveInFile(projectView, filePath)
            val parsedProjectView = parser.parse(filePath)

            // then
            val expectedProjectView = ProjectView(
                targets = null,
                bazelBinary = ProjectViewBazelBinarySection(Paths.get("/path/to/bazel")),
                buildFlags = null,
                buildManualTargets = ProjectViewBuildManualTargetsSection(true),
                directories = null,
                deriveTargetsFromDirectories = ProjectViewDeriveTargetsFromDirectoriesSection(true),
                importDepth = ProjectViewImportDepthSection(3),
            )
            parsedProjectView shouldBe expectedProjectView
        }

        @Test
        fun `should return success and save partly filled project view in the file which should be parsable by the parser`() {
            // given
            val filePath = tempRoot.resolve("path/to/projectview.bazelproject")

            val projectView = ProjectView(
                targets = ProjectViewTargetsSection(
                    listOf(
                        BuildTargetIdentifier("//included_target1"),
                        BuildTargetIdentifier("//included_target2"),
                        BuildTargetIdentifier("//included_target3"),
                    ), emptyList()
                ),
                bazelBinary = null,
                buildFlags = ProjectViewBuildFlagsSection(
                    listOf(
                        "--build_flag1=value1",
                        "--build_flag2=value2",
                        "--build_flag3=value3",
                    )
                ),
                buildManualTargets = ProjectViewBuildManualTargetsSection(true),
                directories = null,
                deriveTargetsFromDirectories = ProjectViewDeriveTargetsFromDirectoriesSection(true),
                importDepth = null,
            )

            val parser = DefaultProjectViewParser()

            // when
            DefaultProjectViewGenerator.generatePrettyStringAndSaveInFile(projectView, filePath)
            val parsedProjectView = parser.parse(filePath)

            // then
            parsedProjectView shouldBe projectView
        }

        @Test
        fun `should return success and save project view in the file which should be parsable by the parser`() {
            // given
            val filePath = tempRoot.resolve("path/to/projectview.bazelproject")

            val projectView = ProjectView(
                targets = ProjectViewTargetsSection(
                    listOf(
                        BuildTargetIdentifier("//included_target1"),
                        BuildTargetIdentifier("//included_target2"),
                        BuildTargetIdentifier("//included_target3"),
                    ),
                    listOf(
                        BuildTargetIdentifier("//excluded_target1"),
                        BuildTargetIdentifier("//excluded_target2"),
                    )
                ),
                bazelBinary = ProjectViewBazelBinarySection(Paths.get("/path/to/bazel")),
                buildFlags = ProjectViewBuildFlagsSection(
                    listOf(
                        "--build_flag1=value1",
                        "--build_flag2=value2",
                        "--build_flag3=value3",
                    )
                ),
                buildManualTargets = ProjectViewBuildManualTargetsSection(false),
                directories = ProjectViewDirectoriesSection(
                        listOf(
                                Path("included_dir1"),
                                Path("included_dir2"),
                                Path("included_dir3")
                        ),
                        listOf(
                                Path("excluded_dir1"),
                                Path("excluded_dir2")
                        )
                ),
                deriveTargetsFromDirectories = ProjectViewDeriveTargetsFromDirectoriesSection(true),
                importDepth = ProjectViewImportDepthSection(3),
            )

            val parser = DefaultProjectViewParser()

            // when
            DefaultProjectViewGenerator.generatePrettyStringAndSaveInFile(projectView, filePath)
            val parsedProjectView = parser.parse(filePath)

            // then
            parsedProjectView shouldBe projectView
        }
    }
}
