package org.jetbrains.bsp.bazel.projectview.generator

import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import io.kotest.matchers.shouldBe
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import org.jetbrains.bsp.bazel.projectview.model.sections.*
import org.jetbrains.bsp.bazel.projectview.parser.DefaultProjectViewParser
import org.jetbrains.bsp.bazel.utils.dope.DopeTemp
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.Path

class DefaultProjectViewGeneratorTest {

    @Nested
    @DisplayName("fun generatePrettyString(projectView: ProjectView): String tests")
    inner class GeneratePrettyStringTest {

        @Test
        fun `should return empty line for project view with all null fields`() {
            // given
            val projectView = ProjectView(
                targets = null,
                bazelPath = null,
                debuggerAddress = null,
                javaPath = null,
                buildFlags = null,
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
                bazelPath = null,
                debuggerAddress = null,
                javaPath = null,
                buildFlags = null,
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
                bazelPath = ProjectViewBazelPathSection(Paths.get("/path/to/bazel")),
                debuggerAddress = null,
                javaPath = null,
                buildFlags = null,
                directories = null,
                deriveTargetsFromDirectories = null,
                importDepth = null,
            )

            // when
            val generatedString = DefaultProjectViewGenerator.generatePrettyString(projectView)

            // then
            val expectedGeneratedString =
                """
                bazel_path: /path/to/bazel

                """.trimIndent()
            generatedString shouldBe expectedGeneratedString
        }

        @Test
        fun `should return pretty string only with debugger address for project view only with debugger address`() {
            // given
            val projectView = ProjectView(
                targets = null,
                bazelPath = null,
                debuggerAddress = ProjectViewDebuggerAddressSection("localhost:8000"),
                javaPath = null,
                buildFlags = null,
                directories = null,
                deriveTargetsFromDirectories = null,
                importDepth = null,
            )

            // when
            val generatedString = DefaultProjectViewGenerator.generatePrettyString(projectView)

            // then
            val expectedGeneratedString =
                """
                debugger_address: localhost:8000

                """.trimIndent()
            generatedString shouldBe expectedGeneratedString
        }

        @Test
        fun `should return pretty string only with java path for project view only with java path`() {
            // given
            val projectView = ProjectView(
                targets = null,
                bazelPath = null,
                debuggerAddress = null,
                javaPath = ProjectViewJavaPathSection(Paths.get("/path/to/java")),
                buildFlags = null,
                directories = null,
                deriveTargetsFromDirectories = null,
                importDepth = null,
            )

            // when
            val generatedString = DefaultProjectViewGenerator.generatePrettyString(projectView)

            // then
            val expectedGeneratedString =
                """
                java_path: /path/to/java

                """.trimIndent()
            generatedString shouldBe expectedGeneratedString
        }

        @Test
        fun `should return pretty string only with build flags for project view only with build flags`() {
            // given
            val projectView = ProjectView(
                targets = null,
                bazelPath = null,
                debuggerAddress = null,
                javaPath = null,
                buildFlags = ProjectViewBuildFlagsSection(
                    listOf(
                        "--build_flag1=value1",
                        "--build_flag2=value2",
                        "--build_flag3=value3",
                    )
                ),
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
                    bazelPath = null,
                    debuggerAddress = null,
                    javaPath = null,
                    buildFlags = null,
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
                    bazelPath = null,
                    debuggerAddress = null,
                    javaPath = null,
                    buildFlags = null,
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
                bazelPath = null,
                debuggerAddress = null,
                javaPath = null,
                buildFlags = null,
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
        fun `should return pretty string with project view for project view with empty list sections`() {
            // given
            val projectView = ProjectView(
                targets = ProjectViewTargetsSection(emptyList(), emptyList()),
                bazelPath = ProjectViewBazelPathSection(Paths.get("/path/to/bazel")),
                debuggerAddress = ProjectViewDebuggerAddressSection("localhost:8000"),
                javaPath = ProjectViewJavaPathSection(Paths.get("/path/to/java")),
                buildFlags = ProjectViewBuildFlagsSection(emptyList()),
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

                bazel_path: /path/to/bazel

                debugger_address: localhost:8000

                java_path: /path/to/java

                build_flags:
                
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
                bazelPath = null,
                debuggerAddress = null,
                javaPath = ProjectViewJavaPathSection(Paths.get("/path/to/java")),
                buildFlags = ProjectViewBuildFlagsSection(
                    listOf(
                        "--build_flag1=value1",
                        "--build_flag2=value2",
                        "--build_flag3=value3",
                    )
                ),
                directories = null,
                deriveTargetsFromDirectories = null,
                importDepth = null
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

                java_path: /path/to/java

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
                bazelPath = ProjectViewBazelPathSection(Paths.get("/path/to/bazel")),
                debuggerAddress = ProjectViewDebuggerAddressSection("localhost:8000"),
                javaPath = ProjectViewJavaPathSection(Paths.get("/path/to/java")),
                buildFlags = ProjectViewBuildFlagsSection(
                    listOf(
                        "--build_flag1=value1",
                        "--build_flag2=value2",
                        "--build_flag3=value3",
                    )
                ),
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

                bazel_path: /path/to/bazel

                debugger_address: localhost:8000

                java_path: /path/to/java

                build_flags:
                    --build_flag1=value1
                    --build_flag2=value2
                    --build_flag3=value3

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
    @DisplayName("fun generatePrettyStringAndSaveInFile(projectView: ProjectView, filePath: Path): Try<Void> tests")
    inner class GeneratePrettyStringAndSaveInFileTest {

        @Test
        fun `should return success and save project view in the file`() {
            // given
            val filePath = DopeTemp.createTempPath("path/to/projectview.bazelproject")

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
                bazelPath = ProjectViewBazelPathSection(Paths.get("/path/to/bazel")),
                debuggerAddress = ProjectViewDebuggerAddressSection("localhost:8000"),
                javaPath = ProjectViewJavaPathSection(Paths.get("/path/to/java")),
                buildFlags = ProjectViewBuildFlagsSection(
                    listOf(
                        "--build_flag1=value1",
                        "--build_flag2=value2",
                        "--build_flag3=value3",
                    )
                ),
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
            val result = DefaultProjectViewGenerator.generatePrettyStringAndSaveInFile(projectView, filePath)

            // then
            result.isSuccess shouldBe true

            val expectedFileContent =
                """
                targets:
                    //included_target1
                    //included_target2
                    //included_target3
                    -//excluded_target1
                    -//excluded_target2

                bazel_path: /path/to/bazel

                debugger_address: localhost:8000

                java_path: /path/to/java

                build_flags:
                    --build_flag1=value1
                    --build_flag2=value2
                    --build_flag3=value3

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
            val filePath = DopeTemp.createTempFile("path/to/projectview.bazelproject")
            Files.writeString(filePath, "some random things, maybe previous project view")

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
                bazelPath = ProjectViewBazelPathSection(Paths.get("/path/to/bazel")),
                debuggerAddress = ProjectViewDebuggerAddressSection("localhost:8000"),
                javaPath = ProjectViewJavaPathSection(Paths.get("/path/to/java")),
                buildFlags = ProjectViewBuildFlagsSection(
                    listOf(
                        "--build_flag1=value1",
                        "--build_flag2=value2",
                        "--build_flag3=value3",
                    )
                ),
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
            val result = DefaultProjectViewGenerator.generatePrettyStringAndSaveInFile(projectView, filePath)

            // then
            result.isSuccess shouldBe true

            val expectedFileContent =
                """
                targets:
                    //included_target1
                    //included_target2
                    //included_target3
                    -//excluded_target1
                    -//excluded_target2

                bazel_path: /path/to/bazel

                debugger_address: localhost:8000

                java_path: /path/to/java

                build_flags:
                    --build_flag1=value1
                    --build_flag2=value2
                    --build_flag3=value3

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
            val filePath = DopeTemp.createTempPath("path/to/projectview.bazelproject")

            val projectView = ProjectView(
                targets = ProjectViewTargetsSection(emptyList(), emptyList()),
                bazelPath = ProjectViewBazelPathSection(Paths.get("/path/to/bazel")),
                debuggerAddress = ProjectViewDebuggerAddressSection("localhost:8000"),
                javaPath = ProjectViewJavaPathSection(Paths.get("/path/to/java")),
                buildFlags = ProjectViewBuildFlagsSection(emptyList()),
                directories = ProjectViewDirectoriesSection(emptyList(), emptyList()),
                deriveTargetsFromDirectories = ProjectViewDeriveTargetsFromDirectoriesSection(true),
                importDepth = ProjectViewImportDepthSection(3),
            )

            val parser = DefaultProjectViewParser()

            // when
            val result = DefaultProjectViewGenerator.generatePrettyStringAndSaveInFile(projectView, filePath)
            val parsedProjectViewTry = parser.parse(filePath)

            // then
            result.isSuccess shouldBe true
            parsedProjectViewTry.isSuccess shouldBe true

            val expectedProjectView = ProjectView(
                targets = null,
                bazelPath = ProjectViewBazelPathSection(Paths.get("/path/to/bazel")),
                debuggerAddress = ProjectViewDebuggerAddressSection("localhost:8000"),
                javaPath = ProjectViewJavaPathSection(Paths.get("/path/to/java")),
                buildFlags = null,
                directories = null,
                deriveTargetsFromDirectories = ProjectViewDeriveTargetsFromDirectoriesSection(true),
                importDepth = ProjectViewImportDepthSection(3),
            )
            parsedProjectViewTry.get() shouldBe expectedProjectView
        }

        @Test
        fun `should return success and save partly filled project view in the file which should be parsable by the parser`() {
            // given
            val filePath = DopeTemp.createTempPath("path/to/projectview.bazelproject")

            val projectView = ProjectView(
                targets = ProjectViewTargetsSection(
                    listOf(
                        BuildTargetIdentifier("//included_target1"),
                        BuildTargetIdentifier("//included_target2"),
                        BuildTargetIdentifier("//included_target3"),
                    ), emptyList()
                ),
                bazelPath = null,
                debuggerAddress = null,
                javaPath = ProjectViewJavaPathSection(Paths.get("/path/to/java")),
                buildFlags = ProjectViewBuildFlagsSection(
                    listOf(
                        "--build_flag1=value1",
                        "--build_flag2=value2",
                        "--build_flag3=value3",
                    )
                ),
                directories = null,
                deriveTargetsFromDirectories = ProjectViewDeriveTargetsFromDirectoriesSection(true),
                importDepth = null,
            )

            val parser = DefaultProjectViewParser()

            // when
            val result = DefaultProjectViewGenerator.generatePrettyStringAndSaveInFile(projectView, filePath)
            val parsedProjectViewTry = parser.parse(filePath)

            // then
            result.isSuccess shouldBe true
            parsedProjectViewTry.isSuccess shouldBe true

            parsedProjectViewTry.get() shouldBe projectView
        }

        @Test
        fun `should return success and save project view in the file which should be parsable by the parser`() {
            // given
            val filePath = DopeTemp.createTempPath("path/to/projectview.bazelproject")

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
                bazelPath = ProjectViewBazelPathSection(Paths.get("/path/to/bazel")),
                debuggerAddress = ProjectViewDebuggerAddressSection("localhost:8000"),
                javaPath = ProjectViewJavaPathSection(Paths.get("/path/to/java")),
                buildFlags = ProjectViewBuildFlagsSection(
                    listOf(
                        "--build_flag1=value1",
                        "--build_flag2=value2",
                        "--build_flag3=value3",
                    )
                ),
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
            val result = DefaultProjectViewGenerator.generatePrettyStringAndSaveInFile(projectView, filePath)
            val parsedProjectViewTry = parser.parse(filePath)

            // then
            result.isSuccess shouldBe true
            parsedProjectViewTry.isSuccess shouldBe true

            parsedProjectViewTry.get() shouldBe projectView
        }
    }
}
