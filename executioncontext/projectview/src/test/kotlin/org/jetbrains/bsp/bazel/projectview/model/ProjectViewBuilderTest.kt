package org.jetbrains.bsp.bazel.projectview.model

import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import io.kotest.matchers.shouldBe
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBazelBinarySection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBuildFlagsSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBuildManualTargetsSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewDeriveTargetsFromDirectoriesSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewDirectoriesSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewEnabledRulesSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewImportDepthSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewTargetsSection
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.nio.file.Paths
import kotlin.io.path.Path

@DisplayName("ProjectView.Builder(..) tests")
class ProjectViewBuilderTest {

    @Nested
    @DisplayName(".Builder(imports = ..) tests")
    inner class BuilderImportsTest {

        @Test
        fun `should return empty values for empty builder`() {
            // given & when
            val projectView = ProjectView.Builder().build()

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
    }

    @Nested
    @DisplayName(".Builder(..) tests not strictly related to imports")
    inner class EntireBuilderTests {

        @Test
        fun `should build project view without imports`() {
            // given & when
            val projectView =
                ProjectView.Builder(
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
                    bazelBinary = ProjectViewBazelBinarySection(Paths.get("path/to/bazel")),
                    buildFlags = ProjectViewBuildFlagsSection(listOf("--build_flag1=value1", "--build_flag2=value2")),
                    buildManualTargets = ProjectViewBuildManualTargetsSection(true),
                    directories = ProjectViewDirectoriesSection(
                            listOf(
                                    Path("included_dir1"), Path("included_dir2")
                            ),
                            listOf(Path("excluded_dir1"))
                    ),
                    deriveTargetsFromDirectories = ProjectViewDeriveTargetsFromDirectoriesSection(true),
                    importDepth = ProjectViewImportDepthSection(0),
                ).build()

            // then

            val expectedProjectView = ProjectView(
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
                bazelBinary = ProjectViewBazelBinarySection(Paths.get("path/to/bazel")),
                buildFlags = ProjectViewBuildFlagsSection(listOf("--build_flag1=value1", "--build_flag2=value2")),
                buildManualTargets = ProjectViewBuildManualTargetsSection(true),
                directories = ProjectViewDirectoriesSection(
                        listOf(
                                Path("included_dir1"), Path("included_dir2")
                        ),
                        listOf(Path("excluded_dir1"))
                ),
                deriveTargetsFromDirectories = ProjectViewDeriveTargetsFromDirectoriesSection(true),
                importDepth = ProjectViewImportDepthSection(0),
                enabledRules = null,
            )
            projectView shouldBe expectedProjectView
        }

        @Test
        fun `should return imported singleton values and list values`() {
            // given
            val importedProjectView =
                ProjectView.Builder(
                    targets = ProjectViewTargetsSection(
                        listOf(
                            BuildTargetIdentifier("//included_target1.1"),
                            BuildTargetIdentifier("//included_target1.2"),
                            BuildTargetIdentifier("//included_target1.3")
                        ),
                        listOf(
                            BuildTargetIdentifier("//excluded_target1.1"),
                            BuildTargetIdentifier("//excluded_target1.2")
                        )
                    ),
                    bazelBinary = ProjectViewBazelBinarySection(Paths.get("path/to/bazel")),
                    buildFlags = ProjectViewBuildFlagsSection(
                        listOf("--build_flag1=value1", "--build_flag2=value2")
                    ),
                    buildManualTargets = ProjectViewBuildManualTargetsSection(true),
                    directories = ProjectViewDirectoriesSection(
                        listOf(
                                Path("included_dir1"), Path("included_dir2")
                        ),
                        listOf(Path("excluded_dir1"))
                    ),
                    deriveTargetsFromDirectories = ProjectViewDeriveTargetsFromDirectoriesSection(true),
                    importDepth = ProjectViewImportDepthSection(0),
                )
            .build()

            // when
            val projectView = ProjectView.Builder(imports = listOf(importedProjectView)).build()

            // then

            val expectedProjectView = ProjectView(
                targets = ProjectViewTargetsSection(
                    listOf(
                        BuildTargetIdentifier("//included_target1.1"),
                        BuildTargetIdentifier("//included_target1.2"),
                        BuildTargetIdentifier("//included_target1.3")
                    ),
                    listOf(
                        BuildTargetIdentifier("//excluded_target1.1"),
                        BuildTargetIdentifier("//excluded_target1.2")
                    )
                ),
                bazelBinary = ProjectViewBazelBinarySection(Paths.get("path/to/bazel")),
                buildFlags = ProjectViewBuildFlagsSection(listOf("--build_flag1=value1", "--build_flag2=value2")),
                buildManualTargets = ProjectViewBuildManualTargetsSection(true),
                directories = ProjectViewDirectoriesSection(
                        listOf(
                                Path("included_dir1"), Path("included_dir2")
                        ),
                        listOf(Path("excluded_dir1"))
                ),
                deriveTargetsFromDirectories = ProjectViewDeriveTargetsFromDirectoriesSection(true),
                importDepth = ProjectViewImportDepthSection(0),
                enabledRules = null,
            )
            projectView shouldBe expectedProjectView
        }

        @Test
        fun `should return singleton values and list values for empty import`() {
            // given
            val importedProjectViewTry = ProjectView.Builder().build()

            // when
            val projectView =
                ProjectView.Builder(
                    imports = listOf(importedProjectViewTry),
                    targets = ProjectViewTargetsSection(
                        listOf(BuildTargetIdentifier("//included_target1")), emptyList()
                    ),
                    bazelBinary = ProjectViewBazelBinarySection(Paths.get("path/to/bazel")),
                    buildFlags = ProjectViewBuildFlagsSection(
                        listOf("--build_flag1=value1", "--build_flag2=value2")
                    ),
                    buildManualTargets = ProjectViewBuildManualTargetsSection(true),
                    directories = ProjectViewDirectoriesSection(
                            listOf(
                                    Path("included_dir1"), Path("included_dir2")
                            ),
                            listOf(Path("excluded_dir1"))
                    ),
                    deriveTargetsFromDirectories = ProjectViewDeriveTargetsFromDirectoriesSection(true),
                    importDepth = ProjectViewImportDepthSection(0),
                ).build()

            // then

            val expectedProjectView = ProjectView(
                targets = ProjectViewTargetsSection(
                    listOf(BuildTargetIdentifier("//included_target1")), emptyList()
                ),
                bazelBinary = ProjectViewBazelBinarySection(Paths.get("path/to/bazel")),
                buildFlags = ProjectViewBuildFlagsSection(listOf("--build_flag1=value1", "--build_flag2=value2")),
                buildManualTargets = ProjectViewBuildManualTargetsSection(true),
                directories = ProjectViewDirectoriesSection(
                        listOf(
                                Path("included_dir1"), Path("included_dir2")
                        ),
                        listOf(Path("excluded_dir1"))
                ),
                deriveTargetsFromDirectories = ProjectViewDeriveTargetsFromDirectoriesSection(true),
                importDepth = ProjectViewImportDepthSection(0),
                enabledRules = null,
            )
            projectView shouldBe expectedProjectView
        }

        @Test
        fun `should return current singleton values and combined list values`() {
            // given
            val importedProjectViewTry =
                ProjectView.Builder(
                    targets = ProjectViewTargetsSection(
                        listOf(
                            BuildTargetIdentifier("//included_target1.1"),
                            BuildTargetIdentifier("//included_target1.2"),
                            BuildTargetIdentifier("//included_target1.3")
                        ),
                        listOf(
                            BuildTargetIdentifier("//excluded_target1.1"),
                            BuildTargetIdentifier("//excluded_target1.2")
                        )
                    ),
                    bazelBinary = ProjectViewBazelBinarySection(Paths.get("imported/path/to/bazel")),
                    buildFlags = ProjectViewBuildFlagsSection(
                        listOf("--build_flag1.1=value1.1", "--build_flag1.2=value1.2")
                    ),
                    buildManualTargets = ProjectViewBuildManualTargetsSection(true),
                    directories = ProjectViewDirectoriesSection(
                            listOf(
                                    Path("included_dir1.1"), Path("included_dir1.2")
                            ),
                            listOf(Path("excluded_dir1.1"))
                    ),
                    deriveTargetsFromDirectories = ProjectViewDeriveTargetsFromDirectoriesSection(false),
                    importDepth = ProjectViewImportDepthSection(1),
                    enabledRules = ProjectViewEnabledRulesSection(listOf("rules_scala"))
            ).build()

            // when
            val projectView =
                ProjectView.Builder(
                    imports = listOf(importedProjectViewTry),
                    targets = ProjectViewTargetsSection(
                        listOf(
                            BuildTargetIdentifier("//included_target2.1"),
                            BuildTargetIdentifier("//included_target2.2"),
                            BuildTargetIdentifier("//included_target2.3")
                        ),
                        listOf(
                            BuildTargetIdentifier("//excluded_target2.1"),
                            BuildTargetIdentifier("//excluded_target2.2")
                        )
                    ),
                    bazelBinary = ProjectViewBazelBinarySection(Paths.get("path/to/bazel")),
                    buildFlags = ProjectViewBuildFlagsSection(
                        listOf("--build_flag2.1=value2.1", "--build_flag2.2=value2.2")
                    ),
                    buildManualTargets = ProjectViewBuildManualTargetsSection(true),
                    directories = ProjectViewDirectoriesSection(
                            listOf(
                                    Path("included_dir2.1"), Path("included_dir2.2")
                            ),
                            listOf(Path("excluded_dir2.1"))
                    ),
                    deriveTargetsFromDirectories = ProjectViewDeriveTargetsFromDirectoriesSection(false),
                    importDepth = ProjectViewImportDepthSection(2),
            ).build()

            // then

            val expectedProjectView = ProjectView(
                targets = ProjectViewTargetsSection(
                    listOf(
                        BuildTargetIdentifier("//included_target1.1"),
                        BuildTargetIdentifier("//included_target1.2"),
                        BuildTargetIdentifier("//included_target1.3"),
                        BuildTargetIdentifier("//included_target2.1"),
                        BuildTargetIdentifier("//included_target2.2"),
                        BuildTargetIdentifier("//included_target2.3")
                    ),
                    listOf(
                        BuildTargetIdentifier("//excluded_target1.1"),
                        BuildTargetIdentifier("//excluded_target1.2"),
                        BuildTargetIdentifier("//excluded_target2.1"),
                        BuildTargetIdentifier("//excluded_target2.2")
                    )
                ),
                bazelBinary = ProjectViewBazelBinarySection(Paths.get("path/to/bazel")),
                buildFlags = ProjectViewBuildFlagsSection(
                    listOf(
                        "--build_flag1.1=value1.1",
                        "--build_flag1.2=value1.2",
                        "--build_flag2.1=value2.1",
                        "--build_flag2.2=value2.2"
                    )
                ),
                buildManualTargets = ProjectViewBuildManualTargetsSection(true),
                directories = ProjectViewDirectoriesSection(
                        listOf(
                                Path("included_dir1.1"),
                                Path("included_dir1.2"),
                                Path("included_dir2.1"),
                                Path("included_dir2.2"),
                        ),
                        listOf(
                                Path("excluded_dir1.1"),
                                Path("excluded_dir2.1"),
                        )
                ),
                deriveTargetsFromDirectories = ProjectViewDeriveTargetsFromDirectoriesSection(false),
                importDepth = ProjectViewImportDepthSection(2),
                enabledRules = ProjectViewEnabledRulesSection(listOf("rules_scala"))
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
                        listOf(
                            BuildTargetIdentifier("//included_target1.1"),
                            BuildTargetIdentifier("//included_target1.2"),
                            BuildTargetIdentifier("//included_target1.3")
                        ),
                        listOf(
                            BuildTargetIdentifier("//excluded_target1.1"),
                            BuildTargetIdentifier("//excluded_target1.2")
                        )
                    ),
                    bazelBinary = ProjectViewBazelBinarySection(Paths.get("imported1/path/to/bazel")),
                    buildFlags = ProjectViewBuildFlagsSection(
                        listOf("--build_flag1.1=value1.1", "--build_flag1.2=value1.2")
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
                    importDepth = ProjectViewImportDepthSection(1),
                ).build()

            val importedProjectViewTry2 =
                ProjectView.Builder(
                    targets = ProjectViewTargetsSection(
                        listOf(BuildTargetIdentifier("//included_target2.1")),
                        listOf(BuildTargetIdentifier("//excluded_target2.1"))
                    ),
                    buildFlags = ProjectViewBuildFlagsSection(listOf("--build_flag2.1=value2.1")),
                    directories = ProjectViewDirectoriesSection(
                            listOf(
                                    Path("included_dir2.1"),
                                    Path("included_dir2.2")
                            ),
                            listOf(
                                    Path("excluded_dir2.1")
                            )
                    )
                ).build()

            val importedProjectViewTry3 =
                ProjectView.Builder(
                    imports = emptyList(),
                    targets = ProjectViewTargetsSection(
                        listOf(
                            BuildTargetIdentifier("//included_target3.1"),
                            BuildTargetIdentifier("//included_target3.2")
                        ),
                        emptyList()
                    ),
                    bazelBinary = ProjectViewBazelBinarySection(Paths.get("imported3/path/to/bazel")),
                    buildFlags = ProjectViewBuildFlagsSection(
                        listOf("--build_flag3.1=value3.1")
                    ),
                    directories = ProjectViewDirectoriesSection(
                            listOf(
                                    Path("included_dir3.1"),
                                    Path("included_dir3.2")
                            ),
                            listOf(
                                    Path("excluded_dir3.1")
                            )
                    ),
                    deriveTargetsFromDirectories = ProjectViewDeriveTargetsFromDirectoriesSection(false),
                    importDepth = ProjectViewImportDepthSection(3),
                ).build()

            // when
            val projectView = ProjectView.Builder(
                imports = listOf(importedProjectViewTry1, importedProjectViewTry2, importedProjectViewTry3),
                targets = ProjectViewTargetsSection(
                    listOf(
                        BuildTargetIdentifier("//included_target4.1"),
                        BuildTargetIdentifier("//included_target4.2"),
                        BuildTargetIdentifier("//included_target4.3")
                    ),
                    listOf(
                        BuildTargetIdentifier("//excluded_target4.1"),
                        BuildTargetIdentifier("//excluded_target4.2")
                    )
                ),
                buildFlags = ProjectViewBuildFlagsSection(
                    listOf("--build_flag4.1=value4.1", "--build_flag4.2=value4.2")
                ),
                directories = ProjectViewDirectoriesSection(
                        listOf(
                                Path("included_dir4.1"),
                                Path("included_dir4.2")
                        ),
                        listOf(
                                Path("excluded_dir4.1")
                        )
                ),
                deriveTargetsFromDirectories = ProjectViewDeriveTargetsFromDirectoriesSection(true),
            ).build()

            // then

            val expectedProjectView = ProjectView(
                targets = ProjectViewTargetsSection(
                    listOf(
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
                    listOf(
                        BuildTargetIdentifier("//excluded_target1.1"),
                        BuildTargetIdentifier("//excluded_target1.2"),
                        BuildTargetIdentifier("//excluded_target2.1"),
                        BuildTargetIdentifier("//excluded_target4.1"),
                        BuildTargetIdentifier("//excluded_target4.2")
                    )
                ),
                bazelBinary = ProjectViewBazelBinarySection(Paths.get("imported3/path/to/bazel")),
                buildFlags = ProjectViewBuildFlagsSection(
                    listOf(
                        "--build_flag1.1=value1.1",
                        "--build_flag1.2=value1.2",
                        "--build_flag2.1=value2.1",
                        "--build_flag3.1=value3.1",
                        "--build_flag4.1=value4.1",
                        "--build_flag4.2=value4.2"
                    )
                ),
                buildManualTargets = ProjectViewBuildManualTargetsSection(true),
                directories = ProjectViewDirectoriesSection(
                        listOf(
                                Path("included_dir1.1"),
                                Path("included_dir1.2"),
                                Path("included_dir2.1"),
                                Path("included_dir2.2"),
                                Path("included_dir3.1"),
                                Path("included_dir3.2"),
                                Path("included_dir4.1"),
                                Path("included_dir4.2")
                        ),
                        listOf(
                                Path("excluded_dir1.1"),
                                Path("excluded_dir2.1"),
                                Path("excluded_dir3.1"),
                                Path("excluded_dir4.1")
                        )
                ),
                deriveTargetsFromDirectories = ProjectViewDeriveTargetsFromDirectoriesSection(true),
                importDepth = ProjectViewImportDepthSection(3),
                enabledRules = null,
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
                        listOf(
                            BuildTargetIdentifier("//included_target1.1"),
                            BuildTargetIdentifier("//included_target1.2"),
                            BuildTargetIdentifier("//included_target1.3")
                        ),
                        listOf(
                            BuildTargetIdentifier("//excluded_target1.1"),
                            BuildTargetIdentifier("//excluded_target1.2")
                        )
                    ),
                    bazelBinary = ProjectViewBazelBinarySection(Paths.get("imported1/path/to/bazel")),
                    buildFlags = ProjectViewBuildFlagsSection(
                        listOf("--build_flag1.1=value1.1", "--build_flag1.2=value1.2")
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
                    deriveTargetsFromDirectories = ProjectViewDeriveTargetsFromDirectoriesSection(true),
                    importDepth = ProjectViewImportDepthSection(1),
                ).build()

            val importedProjectViewTry2 =
                ProjectView.Builder(
                    targets = ProjectViewTargetsSection(
                        listOf(BuildTargetIdentifier("//included_target2.1")),
                        listOf(BuildTargetIdentifier("//excluded_target2.1"))
                    ),
                    buildFlags = ProjectViewBuildFlagsSection(listOf("--build_flag2.1=value2.1")),
                    directories = ProjectViewDirectoriesSection(
                            listOf(
                                    Path("included_dir2.1"),
                                    Path("included_dir2.2")
                            ),
                            listOf(
                                    Path("excluded_dir2.1")
                            )
                    )
                ).build()

            val importedProjectViewTry3 =
                ProjectView.Builder(
                    imports = listOf(importedProjectViewTry1, importedProjectViewTry2),
                    targets = ProjectViewTargetsSection(
                        listOf(
                            BuildTargetIdentifier("//included_target3.1"),
                            BuildTargetIdentifier("//included_target3.2")
                        ),
                        emptyList()
                    ),
                    bazelBinary = ProjectViewBazelBinarySection(Paths.get("imported3/path/to/bazel")),
                    buildFlags = ProjectViewBuildFlagsSection(listOf("--build_flag3.1=value3.1")),
                    buildManualTargets = ProjectViewBuildManualTargetsSection(true),
                    directories = ProjectViewDirectoriesSection(
                            listOf(
                                    Path("included_dir3.1"),
                                    Path("included_dir3.2")
                            ),
                            listOf(
                                    Path("excluded_dir3.1")
                            )
                    ),
                    deriveTargetsFromDirectories = ProjectViewDeriveTargetsFromDirectoriesSection(true),
                    importDepth = ProjectViewImportDepthSection(3),
                ).build()

            val importedProjectViewTry4 = ProjectView.Builder().build()

            // when
            val projectView =
                ProjectView.Builder(
                    imports = listOf(importedProjectViewTry3, importedProjectViewTry4),
                    targets = ProjectViewTargetsSection(
                        listOf(
                            BuildTargetIdentifier("//included_target4.1"),
                            BuildTargetIdentifier("//included_target4.2"),
                            BuildTargetIdentifier("//included_target4.3")
                        ),
                        listOf(
                            BuildTargetIdentifier("//excluded_target4.1"),
                            BuildTargetIdentifier("//excluded_target4.2")
                        )
                    ),
                    buildFlags = ProjectViewBuildFlagsSection(
                        listOf("--build_flag4.1=value4.1", "--build_flag4.2=value4.2")
                    ),
                    directories = ProjectViewDirectoriesSection(
                            listOf(
                                    Path("included_dir4.1"),
                                    Path("included_dir4.2")
                            ),
                            listOf(
                                    Path("excluded_dir4.1")
                            )
                    ),
                    deriveTargetsFromDirectories = ProjectViewDeriveTargetsFromDirectoriesSection(true)
                ).build()

            // then

            val expectedProjectView = ProjectView(
                targets = ProjectViewTargetsSection(
                    listOf(
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
                    listOf(
                        BuildTargetIdentifier("//excluded_target1.1"),
                        BuildTargetIdentifier("//excluded_target1.2"),
                        BuildTargetIdentifier("//excluded_target2.1"),
                        BuildTargetIdentifier("//excluded_target4.1"),
                        BuildTargetIdentifier("//excluded_target4.2")
                    )
                ),
                bazelBinary = ProjectViewBazelBinarySection(Paths.get("imported3/path/to/bazel")),
                buildFlags = ProjectViewBuildFlagsSection(
                    listOf(
                        "--build_flag1.1=value1.1",
                        "--build_flag1.2=value1.2",
                        "--build_flag2.1=value2.1",
                        "--build_flag3.1=value3.1",
                        "--build_flag4.1=value4.1",
                        "--build_flag4.2=value4.2"
                    )
                ),
                buildManualTargets = ProjectViewBuildManualTargetsSection(true),
                directories = ProjectViewDirectoriesSection(
                        listOf(
                                Path("included_dir1.1"),
                                Path("included_dir1.2"),
                                Path("included_dir2.1"),
                                Path("included_dir2.2"),
                                Path("included_dir3.1"),
                                Path("included_dir3.2"),
                                Path("included_dir4.1"),
                                Path("included_dir4.2")
                        ),
                        listOf(
                                Path("excluded_dir1.1"),
                                Path("excluded_dir2.1"),
                                Path("excluded_dir3.1"),
                                Path("excluded_dir4.1")
                        )
                ),
                deriveTargetsFromDirectories = ProjectViewDeriveTargetsFromDirectoriesSection(true),
                importDepth = ProjectViewImportDepthSection(3),
                enabledRules = null,
            )
            projectView shouldBe expectedProjectView
        }
    }
}
