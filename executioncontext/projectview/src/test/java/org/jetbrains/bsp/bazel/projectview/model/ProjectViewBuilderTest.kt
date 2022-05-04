package org.jetbrains.bsp.bazel.projectview.model

import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import io.kotest.matchers.shouldBe
import io.vavr.control.Try
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewTargetsSection
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.IOException
import io.vavr.collection.List
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBazelPathSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBuildFlagsSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewDebuggerAddressSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewJavaPathSection
import java.nio.file.Paths

@DisplayName("ProjectView.Builder(..) tests")
class ProjectViewBuilderTest {

    @Nested
    @DisplayName(".Builder(imports = ..) tests")
    inner class BuilderImportsTest {

        @Test
        fun `should return failure with first cause for builder with failure imports`() {
            // given
            val importedProjectViewTry1 = ProjectView.Builder().build()

            val importedProjectViewTry2 =
                Try.failure<ProjectView>(IOException("doesnt/exist/projectview2.bazelproject file does not exist!"))

            val importedProjectViewTry3 = ProjectView.Builder(imports = emptyList()).build()

            val importedProjectViewTry4 =
                Try.failure<ProjectView>(IOException("doesnt/exist/projectview4.bazelproject file does not exist!"))

            // when
            val projectViewTry =
                ProjectView.Builder(
                    imports =
                    listOf(
                        importedProjectViewTry1,
                        importedProjectViewTry2,
                        importedProjectViewTry3,
                        importedProjectViewTry4,
                    )
                ).build()

            // then
            projectViewTry.isFailure shouldBe true
            projectViewTry.cause::class shouldBe IOException::class
            projectViewTry.cause.message shouldBe "doesnt/exist/projectview2.bazelproject file does not exist!"
        }

        @Test
        fun `should return empty values for empty builder`() {
            // given & when
            val projectViewTry = ProjectView.Builder().build()

            // then
            projectViewTry.isSuccess shouldBe true
            val projectView = projectViewTry.get()

            val expectedProjectView = ProjectView(
                targets = null,
                bazelPath = null,
                debuggerAddress = null,
                javaPath = null,
                buildFlags = null,
            )
            projectView shouldBe expectedProjectView
        }
    }

    @Nested
    @DisplayName(".Builder(..) tests not strictly related to imports")
    inner class EntireBuilderTests {

        @Test
        fun `should build project view without imports`() {
            // given & when
            val projectViewTry =
                ProjectView.Builder(
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
                    bazelPath = ProjectViewBazelPathSection(Paths.get("path/to/bazel")),
                    debuggerAddress = ProjectViewDebuggerAddressSection("127.0.0.1:8000"),
                    javaPath = ProjectViewJavaPathSection(Paths.get("path/to/java")),
                    buildFlags = ProjectViewBuildFlagsSection(List.of("--build_flag1=value1", "--build_flag2=value2"))
                ).build()

            // then
            projectViewTry.isSuccess shouldBe true
            val projectView = projectViewTry.get()

            val expectedProjectView = ProjectView(
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
                bazelPath = ProjectViewBazelPathSection(Paths.get("path/to/bazel")),
                debuggerAddress = ProjectViewDebuggerAddressSection("127.0.0.1:8000"),
                javaPath = ProjectViewJavaPathSection(Paths.get("path/to/java")),
                buildFlags = ProjectViewBuildFlagsSection(List.of("--build_flag1=value1", "--build_flag2=value2"))
            )
            projectView shouldBe expectedProjectView
        }

        @Test
        fun `should return imported singleton values and list values`() {
            // given
            val importedProjectViewTry =
                ProjectView.Builder(
                    targets = ProjectViewTargetsSection(
                        List.of(
                            BuildTargetIdentifier("//included_target1.1"),
                            BuildTargetIdentifier("//included_target1.2"),
                            BuildTargetIdentifier("//included_target1.3")
                        ),
                        List.of(
                            BuildTargetIdentifier("//excluded_target1.1"),
                            BuildTargetIdentifier("//excluded_target1.2")
                        )
                    ),
                    bazelPath = ProjectViewBazelPathSection(Paths.get("path/to/bazel")),
                    debuggerAddress = ProjectViewDebuggerAddressSection("0.0.0.1:8000"),
                    javaPath = ProjectViewJavaPathSection(Paths.get("path/to/java")),
                    buildFlags = ProjectViewBuildFlagsSection(
                        List.of("--build_flag1=value1", "--build_flag2=value2")
                    )
                ).build()

            // when
            val projectViewTry = ProjectView.Builder(imports = listOf(importedProjectViewTry)).build()

            // then
            projectViewTry.isSuccess shouldBe true
            val projectView = projectViewTry.get()

            val expectedProjectView = ProjectView(
                targets = ProjectViewTargetsSection(
                    List.of(
                        BuildTargetIdentifier("//included_target1.1"),
                        BuildTargetIdentifier("//included_target1.2"),
                        BuildTargetIdentifier("//included_target1.3")
                    ),
                    List.of(
                        BuildTargetIdentifier("//excluded_target1.1"),
                        BuildTargetIdentifier("//excluded_target1.2")
                    )
                ),
                bazelPath = ProjectViewBazelPathSection(Paths.get("path/to/bazel")),
                debuggerAddress = ProjectViewDebuggerAddressSection("0.0.0.1:8000"),
                javaPath = ProjectViewJavaPathSection(Paths.get("path/to/java")),
                buildFlags = ProjectViewBuildFlagsSection(List.of("--build_flag1=value1", "--build_flag2=value2"))
            )
            projectView shouldBe expectedProjectView
        }

        @Test
        fun `should return singleton values and list values for empty import`() {
            // given
            val importedProjectViewTry = ProjectView.Builder().build()

            // when
            val projectViewTry =
                ProjectView.Builder(
                    imports = listOf(importedProjectViewTry),
                    targets = ProjectViewTargetsSection(
                        List.of(BuildTargetIdentifier("//included_target1")), List.of()
                    ),
                    bazelPath = ProjectViewBazelPathSection(Paths.get("path/to/bazel")),
                    debuggerAddress = ProjectViewDebuggerAddressSection("0.0.0.1:8000"),
                    javaPath = ProjectViewJavaPathSection(Paths.get("path/to/java")),
                    buildFlags = ProjectViewBuildFlagsSection(
                        List.of("--build_flag1=value1", "--build_flag2=value2")
                    ),
                ).build()

            // then
            projectViewTry.isSuccess shouldBe true
            val projectView = projectViewTry.get()

            val expectedProjectView = ProjectView(
                targets = ProjectViewTargetsSection(
                    List.of(BuildTargetIdentifier("//included_target1")), List.of()
                ),
                bazelPath = ProjectViewBazelPathSection(Paths.get("path/to/bazel")),
                debuggerAddress = ProjectViewDebuggerAddressSection("0.0.0.1:8000"),
                javaPath = ProjectViewJavaPathSection(Paths.get("path/to/java")),
                buildFlags = ProjectViewBuildFlagsSection(List.of("--build_flag1=value1", "--build_flag2=value2"))
            )
            projectView shouldBe expectedProjectView
        }

        @Test
        fun `should return current singleton values and combined list values`() {
            // given
            val importedProjectViewTry =
                ProjectView.Builder(
                    targets = ProjectViewTargetsSection(
                        List.of(
                            BuildTargetIdentifier("//included_target1.1"),
                            BuildTargetIdentifier("//included_target1.2"),
                            BuildTargetIdentifier("//included_target1.3")
                        ),
                        List.of(
                            BuildTargetIdentifier("//excluded_target1.1"),
                            BuildTargetIdentifier("//excluded_target1.2")
                        )
                    ),
                    bazelPath = ProjectViewBazelPathSection(Paths.get("imported/path/to/bazel")),
                    debuggerAddress = ProjectViewDebuggerAddressSection("0.0.0.1:8000"),
                    javaPath = ProjectViewJavaPathSection(Paths.get("imported/path/to/java")),
                    buildFlags = ProjectViewBuildFlagsSection(
                        List.of("--build_flag1.1=value1.1", "--build_flag1.2=value1.2")
                    )
                ).build()

            // when
            val projectViewTry =
                ProjectView.Builder(
                    imports = listOf(importedProjectViewTry),
                    targets = ProjectViewTargetsSection(
                        List.of(
                            BuildTargetIdentifier("//included_target2.1"),
                            BuildTargetIdentifier("//included_target2.2"),
                            BuildTargetIdentifier("//included_target2.3")
                        ),
                        List.of(
                            BuildTargetIdentifier("//excluded_target2.1"),
                            BuildTargetIdentifier("//excluded_target2.2")
                        )
                    ),
                    bazelPath = ProjectViewBazelPathSection(Paths.get("path/to/bazel")),
                    debuggerAddress = ProjectViewDebuggerAddressSection("127.0.0.1:8000"),
                    javaPath = ProjectViewJavaPathSection(Paths.get("path/to/java")),
                    buildFlags = ProjectViewBuildFlagsSection(
                        List.of("--build_flag2.1=value2.1", "--build_flag2.2=value2.2")
                    )
                ).build()

            // then
            projectViewTry.isSuccess shouldBe true
            val projectView = projectViewTry.get()

            val expectedProjectView = ProjectView(
                targets = ProjectViewTargetsSection(
                    List.of(
                        BuildTargetIdentifier("//included_target1.1"),
                        BuildTargetIdentifier("//included_target1.2"),
                        BuildTargetIdentifier("//included_target1.3"),
                        BuildTargetIdentifier("//included_target2.1"),
                        BuildTargetIdentifier("//included_target2.2"),
                        BuildTargetIdentifier("//included_target2.3")
                    ),
                    List.of(
                        BuildTargetIdentifier("//excluded_target1.1"),
                        BuildTargetIdentifier("//excluded_target1.2"),
                        BuildTargetIdentifier("//excluded_target2.1"),
                        BuildTargetIdentifier("//excluded_target2.2")
                    )
                ),
                bazelPath = ProjectViewBazelPathSection(Paths.get("path/to/bazel")),
                debuggerAddress = ProjectViewDebuggerAddressSection("127.0.0.1:8000"),
                javaPath = ProjectViewJavaPathSection(Paths.get("path/to/java")),
                buildFlags = ProjectViewBuildFlagsSection(
                    List.of(
                        "--build_flag1.1=value1.1",
                        "--build_flag1.2=value1.2",
                        "--build_flag2.1=value2.1",
                        "--build_flag2.2=value2.2"
                    )
                )
            )
            projectView shouldBe expectedProjectView
        }

        @Test
        fun `should return last singleton values and combined list values for three imports`() {
            // given
            val importedProjectViewTry1 =
                ProjectView.Builder(
                    imports = emptyList(),
                    targets = ProjectViewTargetsSection(
                        List.of(
                            BuildTargetIdentifier("//included_target1.1"),
                            BuildTargetIdentifier("//included_target1.2"),
                            BuildTargetIdentifier("//included_target1.3")
                        ),
                        List.of(
                            BuildTargetIdentifier("//excluded_target1.1"),
                            BuildTargetIdentifier("//excluded_target1.2")
                        )
                    ),
                    bazelPath = ProjectViewBazelPathSection(Paths.get("imported1/path/to/bazel")),
                    debuggerAddress = ProjectViewDebuggerAddressSection("0.0.0.1:8000"),
                    javaPath = ProjectViewJavaPathSection(Paths.get("imported1/path/to/java")),
                    buildFlags = ProjectViewBuildFlagsSection(
                        List.of("--build_flag1.1=value1.1", "--build_flag1.2=value1.2")
                    )
                ).build()

            val importedProjectViewTry2 =
                ProjectView.Builder(
                    targets = ProjectViewTargetsSection(
                        List.of(BuildTargetIdentifier("//included_target2.1")),
                        List.of(BuildTargetIdentifier("//excluded_target2.1"))
                    ),
                    buildFlags = ProjectViewBuildFlagsSection(List.of("--build_flag2.1=value2.1"))
                ).build()

            val importedProjectViewTry3 =
                ProjectView.Builder(
                    imports = emptyList(),
                    targets = ProjectViewTargetsSection(
                        List.of(
                            BuildTargetIdentifier("//included_target3.1"),
                            BuildTargetIdentifier("//included_target3.2")
                        ),
                        List.of()
                    ),
                    bazelPath = ProjectViewBazelPathSection(Paths.get("imported3/path/to/bazel")),
                    debuggerAddress = ProjectViewDebuggerAddressSection("0.0.0.3:8000"),
                    javaPath = ProjectViewJavaPathSection(Paths.get("imported3/path/to/java")),
                    buildFlags = ProjectViewBuildFlagsSection(
                        List.of("--build_flag3.1=value3.1")
                    )
                ).build()

            // when
            val projectViewTry = ProjectView.Builder(
                imports = listOf(importedProjectViewTry1, importedProjectViewTry2, importedProjectViewTry3),
                targets = ProjectViewTargetsSection(
                    List.of(
                        BuildTargetIdentifier("//included_target4.1"),
                        BuildTargetIdentifier("//included_target4.2"),
                        BuildTargetIdentifier("//included_target4.3")
                    ),
                    List.of(
                        BuildTargetIdentifier("//excluded_target4.1"),
                        BuildTargetIdentifier("//excluded_target4.2")
                    )
                ),
                buildFlags = ProjectViewBuildFlagsSection(
                    List.of("--build_flag4.1=value4.1", "--build_flag4.2=value4.2")
                )
            )
                .build()

            // then
            projectViewTry.isSuccess shouldBe true
            val projectView = projectViewTry.get()

            val expectedProjectView = ProjectView(
                targets = ProjectViewTargetsSection(
                    List.of(
                        BuildTargetIdentifier("//included_target1.1"),
                        BuildTargetIdentifier("//included_target1.2"),
                        BuildTargetIdentifier("//included_target1.3"),
                        BuildTargetIdentifier("//included_target2.1"),
                        BuildTargetIdentifier("//included_target3.1"),
                        BuildTargetIdentifier("//included_target3.2"),
                        BuildTargetIdentifier("//included_target4.1"),
                        BuildTargetIdentifier("//included_target4.2"),
                        BuildTargetIdentifier("//included_target4.3")
                    ),
                    List.of(
                        BuildTargetIdentifier("//excluded_target1.1"),
                        BuildTargetIdentifier("//excluded_target1.2"),
                        BuildTargetIdentifier("//excluded_target2.1"),
                        BuildTargetIdentifier("//excluded_target4.1"),
                        BuildTargetIdentifier("//excluded_target4.2")
                    )
                ),
                bazelPath = ProjectViewBazelPathSection(Paths.get("imported3/path/to/bazel")),
                debuggerAddress = ProjectViewDebuggerAddressSection("0.0.0.3:8000"),
                javaPath = ProjectViewJavaPathSection(Paths.get("imported3/path/to/java")),
                buildFlags = ProjectViewBuildFlagsSection(
                    List.of(
                        "--build_flag1.1=value1.1",
                        "--build_flag1.2=value1.2",
                        "--build_flag2.1=value2.1",
                        "--build_flag3.1=value3.1",
                        "--build_flag4.1=value4.1",
                        "--build_flag4.2=value4.2"
                    )
                )
            )
            projectView shouldBe expectedProjectView
        }

        @Test
        fun `should return last singleton values and combined list values for nested imports`() {
            // given
            val importedProjectViewTry1 =
                ProjectView.Builder(
                    imports = emptyList(),
                    targets = ProjectViewTargetsSection(
                        List.of(
                            BuildTargetIdentifier("//included_target1.1"),
                            BuildTargetIdentifier("//included_target1.2"),
                            BuildTargetIdentifier("//included_target1.3")
                        ),
                        List.of(
                            BuildTargetIdentifier("//excluded_target1.1"),
                            BuildTargetIdentifier("//excluded_target1.2")
                        )
                    ),
                    bazelPath = ProjectViewBazelPathSection(Paths.get("imported1/path/to/bazel")),
                    debuggerAddress = ProjectViewDebuggerAddressSection("0.0.0.1:8000"),
                    javaPath = ProjectViewJavaPathSection(Paths.get("imported1/path/to/java")),
                    buildFlags = ProjectViewBuildFlagsSection(
                        List.of("--build_flag1.1=value1.1", "--build_flag1.2=value1.2")
                    )
                ).build()

            val importedProjectViewTry2 =
                ProjectView.Builder(
                    targets = ProjectViewTargetsSection(
                        List.of(BuildTargetIdentifier("//included_target2.1")),
                        List.of(BuildTargetIdentifier("//excluded_target2.1"))
                    ),
                    buildFlags = ProjectViewBuildFlagsSection(List.of("--build_flag2.1=value2.1"))
                ).build()

            val importedProjectViewTry3 =
                ProjectView.Builder(
                    imports = listOf(importedProjectViewTry1, importedProjectViewTry2),
                    targets = ProjectViewTargetsSection(
                        List.of(
                            BuildTargetIdentifier("//included_target3.1"),
                            BuildTargetIdentifier("//included_target3.2")
                        ),
                        List.of()
                    ),
                    bazelPath = ProjectViewBazelPathSection(Paths.get("imported3/path/to/bazel")),
                    debuggerAddress = ProjectViewDebuggerAddressSection("0.0.0.3:8000"),
                    javaPath = ProjectViewJavaPathSection(Paths.get("imported3/path/to/java")),
                    buildFlags = ProjectViewBuildFlagsSection(List.of("--build_flag3.1=value3.1")),
                ).build()

            val importedProjectViewTry4 = ProjectView.Builder().build()

            // when
            val projectViewTry =
                ProjectView.Builder(
                    imports = listOf(importedProjectViewTry3, importedProjectViewTry4),
                    targets = ProjectViewTargetsSection(
                        List.of(
                            BuildTargetIdentifier("//included_target4.1"),
                            BuildTargetIdentifier("//included_target4.2"),
                            BuildTargetIdentifier("//included_target4.3")
                        ),
                        List.of(
                            BuildTargetIdentifier("//excluded_target4.1"),
                            BuildTargetIdentifier("//excluded_target4.2")
                        )
                    ),
                    buildFlags = ProjectViewBuildFlagsSection(
                        List.of("--build_flag4.1=value4.1", "--build_flag4.2=value4.2")
                    )
                ).build()

            // then
            projectViewTry.isSuccess shouldBe true
            val projectView = projectViewTry.get()

            val expectedProjectView = ProjectView(
                targets = ProjectViewTargetsSection(
                    List.of(
                        BuildTargetIdentifier("//included_target1.1"),
                        BuildTargetIdentifier("//included_target1.2"),
                        BuildTargetIdentifier("//included_target1.3"),
                        BuildTargetIdentifier("//included_target2.1"),
                        BuildTargetIdentifier("//included_target3.1"),
                        BuildTargetIdentifier("//included_target3.2"),
                        BuildTargetIdentifier("//included_target4.1"),
                        BuildTargetIdentifier("//included_target4.2"),
                        BuildTargetIdentifier("//included_target4.3")
                    ),
                    List.of(
                        BuildTargetIdentifier("//excluded_target1.1"),
                        BuildTargetIdentifier("//excluded_target1.2"),
                        BuildTargetIdentifier("//excluded_target2.1"),
                        BuildTargetIdentifier("//excluded_target4.1"),
                        BuildTargetIdentifier("//excluded_target4.2")
                    )
                ),
                bazelPath = ProjectViewBazelPathSection(Paths.get("imported3/path/to/bazel")),
                debuggerAddress = ProjectViewDebuggerAddressSection("0.0.0.3:8000"),
                javaPath = ProjectViewJavaPathSection(Paths.get("imported3/path/to/java")),
                buildFlags = ProjectViewBuildFlagsSection(
                    List.of(
                        "--build_flag1.1=value1.1",
                        "--build_flag1.2=value1.2",
                        "--build_flag2.1=value2.1",
                        "--build_flag3.1=value3.1",
                        "--build_flag4.1=value4.1",
                        "--build_flag4.2=value4.2"
                    )
                )
            )
            projectView shouldBe expectedProjectView
        }
    }
}
