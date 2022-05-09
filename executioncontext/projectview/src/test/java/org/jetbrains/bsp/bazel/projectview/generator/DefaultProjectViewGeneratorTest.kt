package org.jetbrains.bsp.bazel.projectview.generator

import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import io.vavr.collection.List
import io.kotest.matchers.shouldBe
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBazelPathSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBuildFlagsSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewDebuggerAddressSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewJavaPathSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewTargetsSection
import org.jetbrains.bsp.bazel.projectview.parser.ProjectViewParserImpl
import org.jetbrains.bsp.bazel.utils.dope.DopeTemp
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Paths

class DefaultProjectViewGeneratorTest {

    private lateinit var generator: DefaultProjectViewGenerator

    @BeforeEach
    fun beforeEach() {
        // given
        this.generator = DefaultProjectViewGenerator()
    }

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
            )

            // when
            val generatedString = generator.generatePrettyString(projectView)

            // then
            generatedString shouldBe "\n"
        }

        @Test
        fun `should return pretty string only with targets for project view only with targets`() {
            // given
            val projectView = ProjectView(
                targets = ProjectViewTargetsSection(
                    List.of(
                        BuildTargetIdentifier("//included_target1"),
                        BuildTargetIdentifier("//included_target2"),
                        BuildTargetIdentifier("//included_target3"),
                    ),
                    List.of(
                        BuildTargetIdentifier("//excluded_target1"),
                        BuildTargetIdentifier("//excluded_target2"),
                    )
                ),
                bazelPath = null,
                debuggerAddress = null,
                javaPath = null,
                buildFlags = null,
            )

            // when
            val generatedString = generator.generatePrettyString(projectView)

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
            )

            // when
            val generatedString = generator.generatePrettyString(projectView)

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
            )

            // when
            val generatedString = generator.generatePrettyString(projectView)

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
            )

            // when
            val generatedString = generator.generatePrettyString(projectView)

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
                    List.of(
                        "--build_flag1=value1",
                        "--build_flag2=value2",
                        "--build_flag3=value3",
                    )
                ),
            )

            // when
            val generatedString = generator.generatePrettyString(projectView)

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
        fun `should return pretty string with project view for project view with empty list sections`() {
            // given
            val projectView = ProjectView(
                targets = ProjectViewTargetsSection(List.of(), List.of()),
                bazelPath = ProjectViewBazelPathSection(Paths.get("/path/to/bazel")),
                debuggerAddress = ProjectViewDebuggerAddressSection("localhost:8000"),
                javaPath = ProjectViewJavaPathSection(Paths.get("/path/to/java")),
                buildFlags = ProjectViewBuildFlagsSection(List.of()),
            )

            // when
            val generatedString = generator.generatePrettyString(projectView)

            // then
            val expectedGeneratedString =
                """
                targets:

                bazel_path: /path/to/bazel
                
                debugger_address: localhost:8000
                
                java_path: /path/to/java

                build_flags:
                
                """.trimIndent()
            generatedString shouldBe expectedGeneratedString
        }

        @Test
        fun `should return pretty string with project view for partly filled project view`() {
            // given
            val projectView = ProjectView(
                targets = ProjectViewTargetsSection(
                    List.of(
                        BuildTargetIdentifier("//included_target1"),
                        BuildTargetIdentifier("//included_target2"),
                        BuildTargetIdentifier("//included_target3"),
                    ), List.of()
                ),
                bazelPath = null,
                debuggerAddress = null,
                javaPath = ProjectViewJavaPathSection(Paths.get("/path/to/java")),
                buildFlags = ProjectViewBuildFlagsSection(
                    List.of(
                        "--build_flag1=value1",
                        "--build_flag2=value2",
                        "--build_flag3=value3",
                    )
                ),
            )

            // when
            val generatedString = generator.generatePrettyString(projectView)

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
                    List.of(
                        BuildTargetIdentifier("//included_target1"),
                        BuildTargetIdentifier("//included_target2"),
                        BuildTargetIdentifier("//included_target3"),
                    ),
                    List.of(
                        BuildTargetIdentifier("//excluded_target1"),
                        BuildTargetIdentifier("//excluded_target2"),
                    )
                ),
                bazelPath = ProjectViewBazelPathSection(Paths.get("/path/to/bazel")),
                debuggerAddress = ProjectViewDebuggerAddressSection("localhost:8000"),
                javaPath = ProjectViewJavaPathSection(Paths.get("/path/to/java")),
                buildFlags = ProjectViewBuildFlagsSection(
                    List.of(
                        "--build_flag1=value1",
                        "--build_flag2=value2",
                        "--build_flag3=value3",
                    )
                ),
            )

            // when
            val generatedString = generator.generatePrettyString(projectView)

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
                    List.of(
                        BuildTargetIdentifier("//included_target1"),
                        BuildTargetIdentifier("//included_target2"),
                        BuildTargetIdentifier("//included_target3"),
                    ),
                    List.of(
                        BuildTargetIdentifier("//excluded_target1"),
                        BuildTargetIdentifier("//excluded_target2"),
                    )
                ),
                bazelPath = ProjectViewBazelPathSection(Paths.get("/path/to/bazel")),
                debuggerAddress = ProjectViewDebuggerAddressSection("localhost:8000"),
                javaPath = ProjectViewJavaPathSection(Paths.get("/path/to/java")),
                buildFlags = ProjectViewBuildFlagsSection(
                    List.of(
                        "--build_flag1=value1",
                        "--build_flag2=value2",
                        "--build_flag3=value3",
                    )
                ),
            )

            // when
            val result = generator.generatePrettyStringAndSaveInFile(projectView, filePath)

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
                    List.of(
                        BuildTargetIdentifier("//included_target1"),
                        BuildTargetIdentifier("//included_target2"),
                        BuildTargetIdentifier("//included_target3"),
                    ),
                    List.of(
                        BuildTargetIdentifier("//excluded_target1"),
                        BuildTargetIdentifier("//excluded_target2"),
                    )
                ),
                bazelPath = ProjectViewBazelPathSection(Paths.get("/path/to/bazel")),
                debuggerAddress = ProjectViewDebuggerAddressSection("localhost:8000"),
                javaPath = ProjectViewJavaPathSection(Paths.get("/path/to/java")),
                buildFlags = ProjectViewBuildFlagsSection(
                    List.of(
                        "--build_flag1=value1",
                        "--build_flag2=value2",
                        "--build_flag3=value3",
                    )
                ),
            )

            // when
            val result = generator.generatePrettyStringAndSaveInFile(projectView, filePath)

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
                
                """.trimIndent()
            Files.readString(filePath) shouldBe expectedFileContent
        }

        @Test
        fun `should return success and save project view with empty list sections in the file which should be parsable by the parser`() {
            // given
            val filePath = DopeTemp.createTempPath("path/to/projectview.bazelproject")

            val projectView = ProjectView(
                targets = ProjectViewTargetsSection(List.of(), List.of()),
                bazelPath = ProjectViewBazelPathSection(Paths.get("/path/to/bazel")),
                debuggerAddress = ProjectViewDebuggerAddressSection("localhost:8000"),
                javaPath = ProjectViewJavaPathSection(Paths.get("/path/to/java")),
                buildFlags = ProjectViewBuildFlagsSection(List.of()),
            )

            val parser = ProjectViewParserImpl()

            // when
            val result = generator.generatePrettyStringAndSaveInFile(projectView, filePath)
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
            )
            parsedProjectViewTry.get() shouldBe expectedProjectView
        }

        @Test
        fun `should return success and save partly filled project view in the file which should be parsable by the parser`() {
            // given
            val filePath = DopeTemp.createTempPath("path/to/projectview.bazelproject")

            val projectView = ProjectView(
                targets = ProjectViewTargetsSection(
                    List.of(
                        BuildTargetIdentifier("//included_target1"),
                        BuildTargetIdentifier("//included_target2"),
                        BuildTargetIdentifier("//included_target3"),
                    ), List.of()
                ),
                bazelPath = null,
                debuggerAddress = null,
                javaPath = ProjectViewJavaPathSection(Paths.get("/path/to/java")),
                buildFlags = ProjectViewBuildFlagsSection(
                    List.of(
                        "--build_flag1=value1",
                        "--build_flag2=value2",
                        "--build_flag3=value3",
                    )
                ),
            )

            val parser = ProjectViewParserImpl()

            // when
            val result = generator.generatePrettyStringAndSaveInFile(projectView, filePath)
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
                    List.of(
                        BuildTargetIdentifier("//included_target1"),
                        BuildTargetIdentifier("//included_target2"),
                        BuildTargetIdentifier("//included_target3"),
                    ),
                    List.of(
                        BuildTargetIdentifier("//excluded_target1"),
                        BuildTargetIdentifier("//excluded_target2"),
                    )
                ),
                bazelPath = ProjectViewBazelPathSection(Paths.get("/path/to/bazel")),
                debuggerAddress = ProjectViewDebuggerAddressSection("localhost:8000"),
                javaPath = ProjectViewJavaPathSection(Paths.get("/path/to/java")),
                buildFlags = ProjectViewBuildFlagsSection(
                    List.of(
                        "--build_flag1=value1",
                        "--build_flag2=value2",
                        "--build_flag3=value3",
                    )
                ),
            )

            val parser = ProjectViewParserImpl()

            // when
            val result = generator.generatePrettyStringAndSaveInFile(projectView, filePath)
            val parsedProjectViewTry = parser.parse(filePath)

            // then
            result.isSuccess shouldBe true
            parsedProjectViewTry.isSuccess shouldBe true

            parsedProjectViewTry.get() shouldBe projectView
        }
    }
}
