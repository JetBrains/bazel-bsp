package org.jetbrains.bsp.bazel.projectview.generator

import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import io.vavr.collection.List
import io.kotest.matchers.shouldBe
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBuildFlagsSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewJavaPathSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewTargetsSection
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.nio.file.Paths

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
            )

            // when
            val generator = DefaultProjectViewGenerator()
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
            val generator = DefaultProjectViewGenerator()
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
            val generator = DefaultProjectViewGenerator()
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
            val generator = DefaultProjectViewGenerator()
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
                bazelPath = null,
                debuggerAddress = null,
                javaPath = ProjectViewJavaPathSection(Paths.get("/path/to/java")),
                buildFlags = ProjectViewBuildFlagsSection(List.of()),
            )

            // when
            val generator = DefaultProjectViewGenerator()
            val generatedString = generator.generatePrettyString(projectView)

            // then
            val expectedGeneratedString =
                """
                targets:

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
            val generator = DefaultProjectViewGenerator()
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
                bazelPath = null, // TODO
                debuggerAddress = null, // TODO
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
            val generator = DefaultProjectViewGenerator()
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

                java_path: /path/to/java
                
                build_flags:
                    --build_flag1=value1
                    --build_flag2=value2
                    --build_flag3=value3
                
                """.trimIndent()
            generatedString shouldBe expectedGeneratedString
        }
    }
}
