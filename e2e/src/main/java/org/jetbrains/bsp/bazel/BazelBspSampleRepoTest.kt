package org.jetbrains.bsp.bazel

import ch.epfl.scala.bsp4j.*
import org.jetbrains.bsp.bazel.base.BazelBspTestBaseScenario
import org.jetbrains.bsp.bazel.base.BazelBspTestScenarioStep
import java.time.Duration
import java.util.stream.Collectors

 class BazelBspSampleRepoTest : BazelBspTestBaseScenario(REPO_NAME) {
    override fun getScenarioSteps(): List<BazelBspTestScenarioStep> =
        listOf(
            resolveProject(),
            compareWorkspaceTargetsResults(),
            sourcesResults(),
            resourcesResults(),
            inverseSourcesResults(),
            dependencySourcesResults(),
            scalaMainClasses(),
            scalaTestClasses(),
            jvmRunEnvironment(),
            jvmTestEnvironment() // TODO
            //  new BazelBspServerSingleTest(
            //      "targets run unsuccessfully",
            //      client::testTargetsRunUnsuccessfully),
            //  new BazelBspServerSingleTest(
            //      "targets test unsuccessfully",
            //      client::testTargetsTestUnsuccessfully),
            //      new BazelBspServerSingleTest(
            //          "target capabilities",
            //          client::testTargetCapabilities)
        )

    private fun resolveProject(): BazelBspTestScenarioStep = BazelBspTestScenarioStep(
            "resolve project"
        ) { testClient.testResolveProject(Duration.ofMinutes(2)) }

    private fun compareWorkspaceTargetsResults(): BazelBspTestScenarioStep = BazelBspTestScenarioStep(
            "compare workspace targets results"
        ) {
            testClient.testWorkspaceTargets(
                Duration.ofSeconds(30), expectedWorkspaceBuildTargetsResult
            )
        }

    private fun sourcesResults(): BazelBspTestScenarioStep {
        val targetWithoutJvmFlagsExampleScala = SourceItem(
            "file://\$WORKSPACE/target_without_jvm_flags/Example.scala", SourceItemKind.FILE, false
        )
        val targetWithoutJvmFlagsSources = SourcesItem(
            BuildTargetIdentifier("//target_without_jvm_flags:binary"),
            listOf(targetWithoutJvmFlagsExampleScala)
        )
        targetWithoutJvmFlagsSources.roots = listOf("file://\$WORKSPACE/target_without_jvm_flags/")
        val targetWithoutArgsExampleScala = SourceItem(
            "file://\$WORKSPACE/target_without_args/Example.scala", SourceItemKind.FILE, false
        )
        val targetWithoutArgsSources = SourcesItem(
            BuildTargetIdentifier("//target_without_args:binary"),
            listOf(targetWithoutArgsExampleScala)
        )
        targetWithoutArgsSources.roots = listOf("file://\$WORKSPACE/target_without_args/")
        val targetWithoutMainClassExampleScala = SourceItem(
            "file://\$WORKSPACE/target_without_main_class/Example.scala",
            SourceItemKind.FILE,
            false
        )
        val targetWithoutMainClassSources = SourcesItem(
            BuildTargetIdentifier("//target_without_main_class:library"),
            listOf(targetWithoutMainClassExampleScala)
        )
        targetWithoutMainClassSources.roots =
            listOf("file://\$WORKSPACE/target_without_main_class/")
        val targetWithResourcesJavaBinaryJava = SourceItem(
            "file://\$WORKSPACE/target_with_resources/JavaBinary.java", SourceItemKind.FILE, false
        )
        val targetWithResourcesSources = SourcesItem(
            BuildTargetIdentifier("//target_with_resources:java_binary"),
            listOf(targetWithResourcesJavaBinaryJava)
        )
        targetWithResourcesSources.roots = listOf("file://\$WORKSPACE/target_with_resources/")
        val targetWithDependencyJavaBinaryJava = SourceItem(
            "file://\$WORKSPACE/target_with_dependency/JavaBinary.java", SourceItemKind.FILE, false
        )
        val targetWithDependencySources = SourcesItem(
            BuildTargetIdentifier("//target_with_dependency:java_binary"),
            listOf(targetWithDependencyJavaBinaryJava)
        )
        targetWithDependencySources.roots = listOf("file://\$WORKSPACE/target_with_dependency/")
        val scalaTargetsScalaBinaryScala = SourceItem(
            "file://\$WORKSPACE/scala_targets/ScalaBinary.scala", SourceItemKind.FILE, false
        )
        val scalaTargetsScalaBinarySources = SourcesItem(
            BuildTargetIdentifier("//scala_targets:scala_binary"),
            listOf(scalaTargetsScalaBinaryScala)
        )
        scalaTargetsScalaBinarySources.roots = listOf("file://\$WORKSPACE/scala_targets/")
        val scalaTargetsScalaTestScala = SourceItem(
            "file://\$WORKSPACE/scala_targets/ScalaTest.scala", SourceItemKind.FILE, false
        )
        val scalaTargetsScalaTestSources = SourcesItem(
            BuildTargetIdentifier("//scala_targets:scala_test"),
            listOf(scalaTargetsScalaTestScala)
        )
        scalaTargetsScalaTestSources.roots = listOf("file://\$WORKSPACE/scala_targets/")
        val javaTargetsJavaBinaryJava = SourceItem(
            "file://\$WORKSPACE/java_targets/JavaBinary.java", SourceItemKind.FILE, false
        )
        val javaTargetsJavaBinarySources = SourcesItem(
            BuildTargetIdentifier("//java_targets:java_binary"),
            listOf(javaTargetsJavaBinaryJava)
        )
        javaTargetsJavaBinarySources.roots = listOf("file://\$WORKSPACE/java_targets/")
        val javaTargetsJavaLibraryJava = SourceItem(
            "file://\$WORKSPACE/java_targets/JavaLibrary.java", SourceItemKind.FILE, false
        )
        val javaTargetsJavaLibrarySources = SourcesItem(
            BuildTargetIdentifier("//java_targets:java_library"),
            listOf(javaTargetsJavaLibraryJava)
        )
        javaTargetsJavaLibrarySources.roots = listOf("file://\$WORKSPACE/java_targets/")
        val javaTargetsSubpackageJavaLibraryJava = SourceItem(
            "file://\$WORKSPACE/java_targets/subpackage/JavaLibrary2.java",
            SourceItemKind.FILE,
            false
        )
        val javaTargetsSubpackageJavaLibrarySources = SourcesItem(
            BuildTargetIdentifier("//java_targets/subpackage:java_library"),
            listOf(javaTargetsSubpackageJavaLibraryJava)
        )
        javaTargetsSubpackageJavaLibrarySources.roots =
            listOf("file://\$WORKSPACE/java_targets/subpackage/")
        val bspWorkspaceRoot = SourcesItem(BuildTargetIdentifier("bsp-workspace-root"), emptyList())
        bspWorkspaceRoot.roots = emptyList()
        val javaTargetsJavaLibraryExportedSources = SourcesItem(
            BuildTargetIdentifier("//java_targets:java_library_exported"), emptyList()
        )
        javaTargetsJavaLibraryExportedSources.roots = emptyList()
        val manualTargetTestJavaFile = SourceItem(
            "file://\$WORKSPACE/manual_target/TestJavaFile.java", SourceItemKind.FILE, false
        )
        val manualTargetTestJavaFileSources = SourcesItem(
            BuildTargetIdentifier("//manual_target:java_library"),
            listOf(manualTargetTestJavaFile)
        )
        manualTargetTestJavaFileSources.roots = listOf("file://\$WORKSPACE/manual_target/")
        val manualTargetTestScalaFile = SourceItem(
            "file://\$WORKSPACE/manual_target/TestScalaFile.scala", SourceItemKind.FILE, false
        )
        val manualTargetTestScalaFileSources = SourcesItem(
            BuildTargetIdentifier("//manual_target:scala_library"),
            listOf(manualTargetTestScalaFile)
        )
        manualTargetTestScalaFileSources.roots = listOf("file://\$WORKSPACE/manual_target/")
        val manualTargetTestJavaTest =
            SourceItem("file://\$WORKSPACE/manual_target/JavaTest.java", SourceItemKind.FILE, false)
        val manualTargetTestJavaTestSources = SourcesItem(
            BuildTargetIdentifier("//manual_target:java_test"),
            listOf(manualTargetTestJavaTest)
        )
        manualTargetTestJavaTestSources.roots = listOf("file://\$WORKSPACE/manual_target/")
        val manualTargetTestScalaTest = SourceItem(
            "file://\$WORKSPACE/manual_target/ScalaTest.scala", SourceItemKind.FILE, false
        )
        val manualTargetTestScalaTestSources = SourcesItem(
            BuildTargetIdentifier("//manual_target:scala_test"),
            listOf(manualTargetTestScalaTest)
        )
        manualTargetTestScalaTestSources.roots = listOf("file://\$WORKSPACE/manual_target/")
        val manualTargetTestJavaBinary = SourceItem(
            "file://\$WORKSPACE/manual_target/TestJavaBinary.java", SourceItemKind.FILE, false
        )
        val manualTargetTestJavaBinarySources = SourcesItem(
            BuildTargetIdentifier("//manual_target:java_binary"),
            listOf(manualTargetTestJavaBinary)
        )
        manualTargetTestJavaBinarySources.roots = listOf("file://\$WORKSPACE/manual_target/")
        val manualTargetTestScalaBinary = SourceItem(
            "file://\$WORKSPACE/manual_target/TestScalaBinary.scala", SourceItemKind.FILE, false
        )
        val manualTargetTestScalaBinarySources = SourcesItem(
            BuildTargetIdentifier("//manual_target:scala_binary"),
            listOf(manualTargetTestScalaBinary)
        )
        manualTargetTestScalaBinarySources.roots = listOf("file://\$WORKSPACE/manual_target/")
        val sourcesParams = SourcesParams(expectedTargetIdentifiers)
        val expectedSourcesResult = SourcesResult(
            listOf(
                targetWithoutArgsSources,
                targetWithoutJvmFlagsSources,
                targetWithoutMainClassSources,
                targetWithResourcesSources,
                targetWithDependencySources,
                scalaTargetsScalaBinarySources,
                scalaTargetsScalaTestSources,
                javaTargetsJavaBinarySources,
                javaTargetsJavaLibrarySources,
                javaTargetsSubpackageJavaLibrarySources,
                bspWorkspaceRoot,
                javaTargetsJavaLibraryExportedSources,
                manualTargetTestJavaFileSources,
                manualTargetTestScalaFileSources,
                manualTargetTestJavaBinarySources,
                manualTargetTestScalaBinarySources,
                manualTargetTestJavaTestSources,
                manualTargetTestScalaTestSources
            )
        )
        return BazelBspTestScenarioStep(
            "sources results"
        ) { testClient.testSources(Duration.ofSeconds(30), sourcesParams, expectedSourcesResult) }
    }

    private fun resourcesResults(): BazelBspTestScenarioStep {
        val bspWorkspaceRoot = ResourcesItem(
            BuildTargetIdentifier("bsp-workspace-root"), listOf("file://\$WORKSPACE/")
        )
        val targetWithResources = ResourcesItem(
            BuildTargetIdentifier("//target_with_resources:java_binary"),
            listOf(
                "file://\$WORKSPACE/target_with_resources/file1.txt",
                "file://\$WORKSPACE/target_with_resources/file2.txt"
            )
        )
        val javaTargetsSubpackageJavaLibrary = ResourcesItem(
            BuildTargetIdentifier("//java_targets/subpackage:java_library"), emptyList()
        )
        val javaTargetsJavaBinary =
            ResourcesItem(BuildTargetIdentifier("//java_targets:java_binary"), emptyList())
        val javaTargetsJavaLibrary =
            ResourcesItem(BuildTargetIdentifier("//java_targets:java_library"), emptyList())
        val javaTargetsJavaLibraryExported = ResourcesItem(
            BuildTargetIdentifier("//java_targets:java_library_exported"), emptyList()
        )
        val scalaTargetsScalaBinary =
            ResourcesItem(BuildTargetIdentifier("//scala_targets:scala_binary"), emptyList())
        val scalaTargetsScalaTest =
            ResourcesItem(BuildTargetIdentifier("//scala_targets:scala_test"), emptyList())
        val targetWithDependencyJavaBinary = ResourcesItem(
            BuildTargetIdentifier("//target_with_dependency:java_binary"), emptyList()
        )
        val targetWithoutArgsBinary =
            ResourcesItem(BuildTargetIdentifier("//target_without_args:binary"), emptyList())
        val targetWithoutJvmFlagsBinary = ResourcesItem(
            BuildTargetIdentifier("//target_without_jvm_flags:binary"), emptyList()
        )
        val targetWithoutMainClassLibrary = ResourcesItem(
            BuildTargetIdentifier("//target_without_main_class:library"), emptyList()
        )
        val manualTargetJavaLibrary =
            ResourcesItem(BuildTargetIdentifier("//manual_target:java_library"), emptyList())
        val manualTargetScalaLibrary =
            ResourcesItem(BuildTargetIdentifier("//manual_target:scala_library"), emptyList())
        val manualTargetJavaBinary =
            ResourcesItem(BuildTargetIdentifier("//manual_target:java_binary"), emptyList())
        val manualTargetScalaBinary =
            ResourcesItem(BuildTargetIdentifier("//manual_target:scala_binary"),emptyList())
        val manualTargetJavaTest =
            ResourcesItem(BuildTargetIdentifier("//manual_target:java_test"), emptyList())
        val manualTargetScalaTest =
            ResourcesItem(BuildTargetIdentifier("//manual_target:scala_test"), emptyList())
        val expectedResourcesResult = ResourcesResult(
            listOf(
                bspWorkspaceRoot,
                targetWithResources,
                javaTargetsSubpackageJavaLibrary,
                javaTargetsJavaBinary,
                javaTargetsJavaLibrary,
                javaTargetsJavaLibraryExported,
                scalaTargetsScalaBinary,
                scalaTargetsScalaTest,
                targetWithDependencyJavaBinary,
                targetWithoutArgsBinary,
                targetWithoutJvmFlagsBinary,
                targetWithoutMainClassLibrary,
                manualTargetJavaLibrary,
                manualTargetScalaLibrary,
                manualTargetJavaBinary,
                manualTargetScalaBinary,
                manualTargetJavaTest,
                manualTargetScalaTest
            )
        )
        val resourcesParams = ResourcesParams(expectedTargetIdentifiers)
        return BazelBspTestScenarioStep(
            "resources results"
        ) {
            testClient.testResources(
                Duration.ofSeconds(30), resourcesParams, expectedResourcesResult
            )
        }
    }

    private fun inverseSourcesResults(): BazelBspTestScenarioStep {
        val inverseSourcesDocument = TextDocumentIdentifier("file://\$WORKSPACE/java_targets/JavaBinary.java")
        val expectedInverseSourcesResult =
            InverseSourcesResult(listOf(BuildTargetIdentifier("//java_targets:java_binary")))
        val inverseSourcesParams = InverseSourcesParams(inverseSourcesDocument)
        return BazelBspTestScenarioStep(
            "inverse sources results"
        ) {
            testClient.testInverseSources(
                Duration.ofSeconds(30), inverseSourcesParams, expectedInverseSourcesResult
            )
        }
    }

    // FIXME: Is it even correct? Here when queried for *all* targets we return only the ones that
    //   are actually Scala ones. It kinda makes sense, but it seems to be inconsistent.
    private fun scalaMainClasses(): BazelBspTestScenarioStep {
        val scalaMainClassesParams = ScalaMainClassesParams(expectedTargetIdentifiers)
        val scalaTargetsScalaBinary = ScalaMainClassesItem(
            BuildTargetIdentifier("//scala_targets:scala_binary"),
            listOf(
                ScalaMainClass(
                    "example.Example", listOf("arg1", "arg2"), listOf("-Xms2G -Xmx5G")
                )
            )
        )
        val targetWithoutJvmFlagsBinary = ScalaMainClassesItem(
            BuildTargetIdentifier("//target_without_jvm_flags:binary"),
            listOf(ScalaMainClass("example.Example", listOf("arg1", "arg2"), emptyList()))
        )
        val targetWithoutArgsBinary = ScalaMainClassesItem(
            BuildTargetIdentifier("//target_without_args:binary"),
            listOf(
                ScalaMainClass(
                    "example.Example",
                    emptyList(),
                    listOf("-Xms2G -Xmx5G")
                )
            )
        )

        // FIXME: I'd like to add a test case where target's environment variables field is non-null.
        //  But how can I even populate it?
        //  Apparently it is populated in JVM run environment?
        val expectedScalaMainClassesResult = ScalaMainClassesResult(
            listOf(scalaTargetsScalaBinary, targetWithoutJvmFlagsBinary, targetWithoutArgsBinary)
        )
        return BazelBspTestScenarioStep(
            "Scala main classes"
        ) {
            testClient.testScalaMainClasses(
                Duration.ofSeconds(30), scalaMainClassesParams, expectedScalaMainClassesResult
            )
        }
    }

    // FIXME: Re-add a spec2 test target. But that requires messing with the bazel toolchain
    //  See:
    // https://github.com/bazelbuild/rules_scala/tree/9b85affa2e08a350a4315882b602eda55b262356/examples/testing/multi_frameworks_toolchain
    //  See: https://github.com/JetBrains/bazel-bsp/issues/96
    private fun scalaTestClasses(): BazelBspTestScenarioStep {
        val scalaTestClassesParams = ScalaTestClassesParams(expectedTargetIdentifiers)
        val scalaTagetsScalaTest = ScalaTestClassesItem(
            BuildTargetIdentifier("//scala_targets:scala_test"),
            listOf("io.bazel.rulesscala.scala_test.Runner")
        )
        val expectedScalaTestClassesResult = ScalaTestClassesResult(listOf(scalaTagetsScalaTest))
        return BazelBspTestScenarioStep(
            "Scala test classes"
        ) {
            testClient.testScalaTestClasses(
                Duration.ofSeconds(30), scalaTestClassesParams, expectedScalaTestClassesResult
            )
        }
    }

    private fun dependencySourcesResults(): BazelBspTestScenarioStep {
        val javaTargetsJavaBinary = DependencySourcesItem(
            BuildTargetIdentifier("//java_targets:java_binary"),
            listOf(
                "file://\$BAZEL_CACHE/bazel-out/k8-fastbuild/bin/java_targets/java_binary-src.jar"
            )
        )
        val javaTargetsJavaLibrary = DependencySourcesItem(
            BuildTargetIdentifier("//java_targets:java_library"),
            listOf(
                "file://\$BAZEL_CACHE/bazel-out/k8-fastbuild/bin/java_targets/libjava_library-src.jar"
            )
        )
        val targetWithDependencyJavaBinary = DependencySourcesItem(
            BuildTargetIdentifier("//target_with_dependency:java_binary"),
            listOf(
                "file://\$BAZEL_CACHE/bazel-out/k8-fastbuild/bin/java_targets/libjava_library_exported-src.jar",
                "file://\$BAZEL_CACHE/bazel-out/k8-fastbuild/bin/java_targets/subpackage/libjava_library-src.jar",
                "file://\$BAZEL_CACHE/bazel-out/k8-fastbuild/bin/target_with_dependency/java_binary-src.jar",
                "file://\$BAZEL_CACHE/external/guava/guava-28.0-jre-src.jar"
            )
        )
        val javaTargetsSubpackageJavaLibrary = DependencySourcesItem(
            BuildTargetIdentifier("//java_targets/subpackage:java_library"),
            listOf(
                "file://\$BAZEL_CACHE/bazel-out/k8-fastbuild/bin/java_targets/subpackage/libjava_library-src.jar"
            )
        )
        val scalaTargetsScalaBinary = DependencySourcesItem(
            BuildTargetIdentifier("//scala_targets:scala_binary"),
            listOf(
                "file://\$BAZEL_CACHE/bazel-out/k8-fastbuild/bin/scala_targets/scala_binary-src.jar"
            )
        )
        val scalaTargetsScalaTest = DependencySourcesItem(
            BuildTargetIdentifier("//scala_targets:scala_test"),
            listOf(
                "file://\$BAZEL_CACHE/bazel-out/k8-fastbuild/bin/scala_targets/scala_test-src.jar",
                "file://\$BAZEL_CACHE/external/io_bazel_rules_scala_scalactic/scalactic_2.12-3.2.9-src.jar",
                "file://\$BAZEL_CACHE/external/io_bazel_rules_scala_scalatest/scalatest_2.12-3.2.9-src.jar",
                "file://\$BAZEL_CACHE/external/io_bazel_rules_scala_scalatest_compatible/scalatest-compatible-3.2.9-src.jar",
                "file://\$BAZEL_CACHE/external/io_bazel_rules_scala_scalatest_core/scalatest-core_2.12-3.2.9-src.jar",
                "file://\$BAZEL_CACHE/external/io_bazel_rules_scala_scalatest_featurespec/scalatest-featurespec_2.12-3.2.9-src.jar",
                "file://\$BAZEL_CACHE/external/io_bazel_rules_scala_scalatest_flatspec/scalatest-flatspec_2.12-3.2.9-src.jar",
                "file://\$BAZEL_CACHE/external/io_bazel_rules_scala_scalatest_freespec/scalatest-freespec_2.12-3.2.9-src.jar",
                "file://\$BAZEL_CACHE/external/io_bazel_rules_scala_scalatest_funspec/scalatest-funspec_2.12-3.2.9-src.jar",
                "file://\$BAZEL_CACHE/external/io_bazel_rules_scala_scalatest_funsuite/scalatest-funsuite_2.12-3.2.9-src.jar",
                "file://\$BAZEL_CACHE/external/io_bazel_rules_scala_scalatest_matchers_core/scalatest-matchers-core_2.12-3.2.9-src.jar",
                "file://\$BAZEL_CACHE/external/io_bazel_rules_scala_scalatest_mustmatchers/scalatest-mustmatchers_2.12-3.2.9-src.jar",
                "file://\$BAZEL_CACHE/external/io_bazel_rules_scala_scalatest_shouldmatchers/scalatest-shouldmatchers_2.12-3.2.9-src.jar"
            )
        )
        val targetWithResourcesJavaBinary = DependencySourcesItem(
            BuildTargetIdentifier("//target_with_resources:java_binary"),
            listOf(
                "file://\$BAZEL_CACHE/bazel-out/k8-fastbuild/bin/target_with_resources/java_binary-src.jar"
            )
        )
        val targetWithoutArgsBinary = DependencySourcesItem(
            BuildTargetIdentifier("//target_without_args:binary"),
            listOf(
                "file://\$BAZEL_CACHE/bazel-out/k8-fastbuild/bin/target_without_args/binary-src.jar"
            )
        )
        val targetWithoutJvmFlagsBinary = DependencySourcesItem(
            BuildTargetIdentifier("//target_without_jvm_flags:binary"),
            listOf(
                "file://\$BAZEL_CACHE/bazel-out/k8-fastbuild/bin/target_without_jvm_flags/binary-src.jar"
            )
        )
        val targetWithoutMainClassLibrary = DependencySourcesItem(
            BuildTargetIdentifier("//target_without_main_class:library"),
            listOf(
                "file://\$BAZEL_CACHE/bazel-out/k8-fastbuild/bin/target_without_main_class/library-src.jar"
            )
        )
        val javaTargetsJavaLibraryExported = DependencySourcesItem(
            BuildTargetIdentifier("//java_targets:java_library_exported"),emptyList()
        )
        val bspWorkspaceRoot = DependencySourcesItem(BuildTargetIdentifier("bsp-workspace-root"), emptyList())
        val manualTargetJavaLibrary = DependencySourcesItem(
            BuildTargetIdentifier("//manual_target:java_library"), emptyList()
        )
        val manualTargetScalaLibrary = DependencySourcesItem(
            BuildTargetIdentifier("//manual_target:scala_library"), emptyList()
        )
        val manualTargetJavaBinary = DependencySourcesItem(
            BuildTargetIdentifier("//manual_target:java_binary"), emptyList()
        )
        val manualTargetScalaBinary = DependencySourcesItem(
            BuildTargetIdentifier("//manual_target:scala_binary"), emptyList()
        )
        val manualTargetJavaTest = DependencySourcesItem(
            BuildTargetIdentifier("//manual_target:java_test"), emptyList()
        )
        val manualTargetScalaTest = DependencySourcesItem(
            BuildTargetIdentifier("//manual_target:scala_test"), emptyList()
        )
        val expectedDependencies = DependencySourcesResult(
            listOf(
                javaTargetsJavaBinary,
                javaTargetsJavaLibrary,
                targetWithDependencyJavaBinary,
                javaTargetsSubpackageJavaLibrary,
                scalaTargetsScalaBinary,
                scalaTargetsScalaTest,
                targetWithResourcesJavaBinary,
                targetWithoutArgsBinary,
                targetWithoutJvmFlagsBinary,
                targetWithoutMainClassLibrary,
                javaTargetsJavaLibraryExported,
                bspWorkspaceRoot,
                manualTargetJavaLibrary,
                manualTargetScalaLibrary,
                manualTargetJavaBinary,
                manualTargetScalaBinary,
                manualTargetJavaTest,
                manualTargetScalaTest
            )
        )
        val dependencySourcesParams = DependencySourcesParams(expectedTargetIdentifiers)
        return BazelBspTestScenarioStep(
            "dependency sources results"
        ) {
            testClient.testDependencySources(
                Duration.ofSeconds(30), dependencySourcesParams, expectedDependencies
            )
        }
    }

    // FIXME: Environment is always empty for now until we figure out how to handle it.
    // FIXME: Working directory is not an URI???
    // FIXME: Should this return only targets which are runnable?
    private fun jvmRunEnvironment(): BazelBspTestScenarioStep {
        val params = JvmRunEnvironmentParams(expectedTargetIdentifiers)
        val expectedResult = JvmRunEnvironmentResult(
            listOf(
                JvmEnvironmentItem(
                    BuildTargetIdentifier("//java_targets:java_binary"),
                    listOf(
                        "file://\$BAZEL_CACHE/bazel-out/k8-fastbuild/bin/java_targets/java_binary.jar"
                    ),
                    emptyList(),
                    "\$WORKSPACE",
                    mapOf()
                ),
                JvmEnvironmentItem(
                    BuildTargetIdentifier("//java_targets:java_library"),
                    listOf(
                        "file://\$BAZEL_CACHE/bazel-out/k8-fastbuild/bin/java_targets/libjava_library.jar"
                    ),
                    emptyList(),
                    "\$WORKSPACE",
                    mapOf()
                ),
                JvmEnvironmentItem(
                    BuildTargetIdentifier("//target_with_dependency:java_binary"),
                    listOf(
                        "file://\$BAZEL_CACHE/bazel-out/k8-fastbuild/bin/target_with_dependency/java_binary.jar",
                        "file://\$BAZEL_CACHE/bazel-out/k8-fastbuild/bin/java_targets/subpackage/libjava_library.jar",
                        "file://\$BAZEL_CACHE/external/guava/guava-28.0-jre.jar"
                    ),
                    emptyList(),
                    "\$WORKSPACE",
                    mapOf()
                ),
                JvmEnvironmentItem(
                    BuildTargetIdentifier("//java_targets/subpackage:java_library"),
                    listOf(
                        "file://\$BAZEL_CACHE/bazel-out/k8-fastbuild/bin/java_targets/subpackage/libjava_library.jar"
                    ),
                    emptyList(),
                    "\$WORKSPACE",
                    mapOf()
                ),
                JvmEnvironmentItem(
                    BuildTargetIdentifier("//scala_targets:scala_binary"),
                    listOf(
                        "file://\$BAZEL_CACHE/bazel-out/k8-fastbuild/bin/scala_targets/scala_binary.jar",
                        "file://\$BAZEL_CACHE/external/io_bazel_rules_scala_scala_library/scala-library-2.12.14.jar",
                        "file://\$BAZEL_CACHE/external/io_bazel_rules_scala_scala_reflect/scala-reflect-2.12.14.jar"
                    ),
                    listOf("-Xms2G -Xmx5G"),
                    "\$WORKSPACE",
                    mapOf()
                ),
                JvmEnvironmentItem(
                    BuildTargetIdentifier("//scala_targets:scala_test"),
                    listOf(
                        "file://\$BAZEL_CACHE/bazel-out/k8-fastbuild/bin/scala_targets/scala_test.jar",
                        "file://\$BAZEL_CACHE/external/io_bazel_rules_scala_scala_library/scala-library-2.12.14.jar",
                        "file://\$BAZEL_CACHE/external/io_bazel_rules_scala_scala_reflect/scala-reflect-2.12.14.jar",
                        "file://\$BAZEL_CACHE/external/io_bazel_rules_scala_scalactic/scalactic_2.12-3.2.9.jar",
                        "file://\$BAZEL_CACHE/external/io_bazel_rules_scala_scalatest/scalatest_2.12-3.2.9.jar",
                        "file://\$BAZEL_CACHE/external/io_bazel_rules_scala_scalatest_compatible/scalatest-compatible-3.2.9.jar",
                        "file://\$BAZEL_CACHE/external/io_bazel_rules_scala_scalatest_core/scalatest-core_2.12-3.2.9.jar",
                        "file://\$BAZEL_CACHE/external/io_bazel_rules_scala_scalatest_featurespec/scalatest-featurespec_2.12-3.2.9.jar",
                        "file://\$BAZEL_CACHE/external/io_bazel_rules_scala_scalatest_flatspec/scalatest-flatspec_2.12-3.2.9.jar",
                        "file://\$BAZEL_CACHE/external/io_bazel_rules_scala_scalatest_freespec/scalatest-freespec_2.12-3.2.9.jar",
                        "file://\$BAZEL_CACHE/external/io_bazel_rules_scala_scalatest_funspec/scalatest-funspec_2.12-3.2.9.jar",
                        "file://\$BAZEL_CACHE/external/io_bazel_rules_scala_scalatest_funsuite/scalatest-funsuite_2.12-3.2.9.jar",
                        "file://\$BAZEL_CACHE/external/io_bazel_rules_scala_scalatest_matchers_core/scalatest-matchers-core_2.12-3.2.9.jar",
                        "file://\$BAZEL_CACHE/external/io_bazel_rules_scala_scalatest_mustmatchers/scalatest-mustmatchers_2.12-3.2.9.jar",
                        "file://\$BAZEL_CACHE/external/io_bazel_rules_scala_scalatest_shouldmatchers/scalatest-shouldmatchers_2.12-3.2.9.jar"
                    ),
                    emptyList(),
                    "\$WORKSPACE",
                    mapOf()
                ),
                JvmEnvironmentItem(
                    BuildTargetIdentifier("//target_with_resources:java_binary"),
                    listOf(
                        "file://\$BAZEL_CACHE/bazel-out/k8-fastbuild/bin/target_with_resources/java_binary.jar"
                    ),
                    emptyList(),
                    "\$WORKSPACE",
                    mapOf()
                ),
                JvmEnvironmentItem(
                    BuildTargetIdentifier("//target_without_args:binary"),
                    listOf(
                        "file://\$BAZEL_CACHE/bazel-out/k8-fastbuild/bin/target_without_args/binary.jar",
                        "file://\$BAZEL_CACHE/external/io_bazel_rules_scala_scala_library/scala-library-2.12.14.jar",
                        "file://\$BAZEL_CACHE/external/io_bazel_rules_scala_scala_reflect/scala-reflect-2.12.14.jar"
                    ),
                    listOf("-Xms2G -Xmx5G"),
                    "\$WORKSPACE",
                    mapOf()
                ),
                JvmEnvironmentItem(
                    BuildTargetIdentifier("//target_without_jvm_flags:binary"),
                    listOf(
                        "file://\$BAZEL_CACHE/bazel-out/k8-fastbuild/bin/target_without_jvm_flags/binary.jar",
                        "file://\$BAZEL_CACHE/external/io_bazel_rules_scala_scala_library/scala-library-2.12.14.jar",
                        "file://\$BAZEL_CACHE/external/io_bazel_rules_scala_scala_reflect/scala-reflect-2.12.14.jar"
                    ),
                    emptyList(),
                    "\$WORKSPACE",
                    mapOf()
                ),
                JvmEnvironmentItem(
                    BuildTargetIdentifier("//target_without_main_class:library"),
                    listOf(
                        "file://\$BAZEL_CACHE/bazel-out/k8-fastbuild/bin/target_without_main_class/library.jar",
                        "file://\$BAZEL_CACHE/external/io_bazel_rules_scala_scala_library/scala-library-2.12.14.jar",
                        "file://\$BAZEL_CACHE/external/io_bazel_rules_scala_scala_reflect/scala-reflect-2.12.14.jar"
                    ),
                    emptyList(),
                    "\$WORKSPACE",
                    mapOf()
                )
            )
        )
        return BazelBspTestScenarioStep(
            "jvm run environment results"
        ) { testClient.testJvmRunEnvironment(Duration.ofSeconds(30), params, expectedResult) }
    }

    // FIXME: Should this return only targets that are actually testable?
    private fun jvmTestEnvironment(): BazelBspTestScenarioStep {
        val params = JvmTestEnvironmentParams(expectedTargetIdentifiers)
        val expectedResult = JvmTestEnvironmentResult(
            listOf(
                JvmEnvironmentItem(
                    BuildTargetIdentifier("//java_targets:java_binary"),
                    listOf(
                        "file://\$BAZEL_CACHE/bazel-out/k8-fastbuild/bin/java_targets/java_binary.jar"
                    ),
                    emptyList(),
                    "\$WORKSPACE",
                    mapOf()
                ),
                JvmEnvironmentItem(
                    BuildTargetIdentifier("//java_targets:java_library"),
                    listOf(
                        "file://\$BAZEL_CACHE/bazel-out/k8-fastbuild/bin/java_targets/libjava_library.jar"
                    ),
                    emptyList(),
                    "\$WORKSPACE",
                    mapOf()
                ),
                JvmEnvironmentItem(
                    BuildTargetIdentifier("//target_with_dependency:java_binary"),
                    listOf(
                        "file://\$BAZEL_CACHE/bazel-out/k8-fastbuild/bin/target_with_dependency/java_binary.jar",
                        "file://\$BAZEL_CACHE/bazel-out/k8-fastbuild/bin/java_targets/subpackage/libjava_library.jar",
                        "file://\$BAZEL_CACHE/external/guava/guava-28.0-jre.jar"
                    ),
                    emptyList(),
                    "\$WORKSPACE",
                    mapOf()
                ),
                JvmEnvironmentItem(
                    BuildTargetIdentifier("//java_targets/subpackage:java_library"),
                    listOf(
                        "file://\$BAZEL_CACHE/bazel-out/k8-fastbuild/bin/java_targets/subpackage/libjava_library.jar"
                    ),
                    emptyList(),
                    "\$WORKSPACE",
                    mapOf()
                ),
                JvmEnvironmentItem(
                    BuildTargetIdentifier("//scala_targets:scala_binary"),
                    listOf(
                        "file://\$BAZEL_CACHE/bazel-out/k8-fastbuild/bin/scala_targets/scala_binary.jar",
                        "file://\$BAZEL_CACHE/external/io_bazel_rules_scala_scala_library/scala-library-2.12.14.jar",
                        "file://\$BAZEL_CACHE/external/io_bazel_rules_scala_scala_reflect/scala-reflect-2.12.14.jar"
                    ),
                    listOf("-Xms2G -Xmx5G"),
                    "\$WORKSPACE",
                    mapOf()
                ),
                JvmEnvironmentItem(
                    BuildTargetIdentifier("//scala_targets:scala_test"),
                    listOf(
                        "file://\$BAZEL_CACHE/bazel-out/k8-fastbuild/bin/scala_targets/scala_test.jar",
                        "file://\$BAZEL_CACHE/external/io_bazel_rules_scala_scala_library/scala-library-2.12.14.jar",
                        "file://\$BAZEL_CACHE/external/io_bazel_rules_scala_scala_reflect/scala-reflect-2.12.14.jar",
                        "file://\$BAZEL_CACHE/external/io_bazel_rules_scala_scalactic/scalactic_2.12-3.2.9.jar",
                        "file://\$BAZEL_CACHE/external/io_bazel_rules_scala_scalatest/scalatest_2.12-3.2.9.jar",
                        "file://\$BAZEL_CACHE/external/io_bazel_rules_scala_scalatest_compatible/scalatest-compatible-3.2.9.jar",
                        "file://\$BAZEL_CACHE/external/io_bazel_rules_scala_scalatest_core/scalatest-core_2.12-3.2.9.jar",
                        "file://\$BAZEL_CACHE/external/io_bazel_rules_scala_scalatest_featurespec/scalatest-featurespec_2.12-3.2.9.jar",
                        "file://\$BAZEL_CACHE/external/io_bazel_rules_scala_scalatest_flatspec/scalatest-flatspec_2.12-3.2.9.jar",
                        "file://\$BAZEL_CACHE/external/io_bazel_rules_scala_scalatest_freespec/scalatest-freespec_2.12-3.2.9.jar",
                        "file://\$BAZEL_CACHE/external/io_bazel_rules_scala_scalatest_funspec/scalatest-funspec_2.12-3.2.9.jar",
                        "file://\$BAZEL_CACHE/external/io_bazel_rules_scala_scalatest_funsuite/scalatest-funsuite_2.12-3.2.9.jar",
                        "file://\$BAZEL_CACHE/external/io_bazel_rules_scala_scalatest_matchers_core/scalatest-matchers-core_2.12-3.2.9.jar",
                        "file://\$BAZEL_CACHE/external/io_bazel_rules_scala_scalatest_mustmatchers/scalatest-mustmatchers_2.12-3.2.9.jar",
                        "file://\$BAZEL_CACHE/external/io_bazel_rules_scala_scalatest_shouldmatchers/scalatest-shouldmatchers_2.12-3.2.9.jar"
                    ),
                    emptyList(),
                    "\$WORKSPACE",
                    mapOf()
                ),
                JvmEnvironmentItem(
                    BuildTargetIdentifier("//target_with_resources:java_binary"),
                    listOf(
                        "file://\$BAZEL_CACHE/bazel-out/k8-fastbuild/bin/target_with_resources/java_binary.jar"
                    ),
                    emptyList(),
                    "\$WORKSPACE",
                    mapOf()
                ),
                JvmEnvironmentItem(
                    BuildTargetIdentifier("//target_without_args:binary"),
                    listOf(
                        "file://\$BAZEL_CACHE/bazel-out/k8-fastbuild/bin/target_without_args/binary.jar",
                        "file://\$BAZEL_CACHE/external/io_bazel_rules_scala_scala_library/scala-library-2.12.14.jar",
                        "file://\$BAZEL_CACHE/external/io_bazel_rules_scala_scala_reflect/scala-reflect-2.12.14.jar"
                    ),
                    listOf("-Xms2G -Xmx5G"),
                    "\$WORKSPACE",
                    mapOf()
                ),
                JvmEnvironmentItem(
                    BuildTargetIdentifier("//target_without_jvm_flags:binary"),
                    listOf(
                        "file://\$BAZEL_CACHE/bazel-out/k8-fastbuild/bin/target_without_jvm_flags/binary.jar",
                        "file://\$BAZEL_CACHE/external/io_bazel_rules_scala_scala_library/scala-library-2.12.14.jar",
                        "file://\$BAZEL_CACHE/external/io_bazel_rules_scala_scala_reflect/scala-reflect-2.12.14.jar"
                    ),
                    emptyList(),
                    "\$WORKSPACE",
                    mapOf()
                ),
                JvmEnvironmentItem(
                    BuildTargetIdentifier("//target_without_main_class:library"),
                    listOf(
                        "file://\$BAZEL_CACHE/bazel-out/k8-fastbuild/bin/target_without_main_class/library.jar",
                        "file://\$BAZEL_CACHE/external/io_bazel_rules_scala_scala_library/scala-library-2.12.14.jar",
                        "file://\$BAZEL_CACHE/external/io_bazel_rules_scala_scala_reflect/scala-reflect-2.12.14.jar"
                    ),
                    emptyList(),
                    "\$WORKSPACE",
                    mapOf()
                )
            )
        )
        return BazelBspTestScenarioStep(
            "jvm test environment results"
        ) { testClient.testJvmTestEnvironment(Duration.ofSeconds(30), params, expectedResult) }
    }

    private val expectedTargetIdentifiers: List<BuildTargetIdentifier>
        get() = expectedWorkspaceBuildTargetsResult.targets.stream()
            .map { obj: BuildTarget -> obj.id }
            .collect(Collectors.toList())
    private val expectedWorkspaceBuildTargetsResult: WorkspaceBuildTargetsResult
        get() {
            val jvmBuildTarget = JvmBuildTarget("file://\$BAZEL_CACHE/external/remotejdk11_\$OS/", "11")
            val javaTargetsJavaBinary = BuildTarget(
                BuildTargetIdentifier("//java_targets:java_binary"),
                listOf("application"),
                listOf("java"),
                emptyList(),
                BuildTargetCapabilities(true, false, true, false)
            )
            javaTargetsJavaBinary.displayName = "//java_targets:java_binary"
            javaTargetsJavaBinary.baseDirectory = "file://\$WORKSPACE/java_targets/"
            javaTargetsJavaBinary.dataKind = "jvm"
            javaTargetsJavaBinary.data = jvmBuildTarget
            val scalaBuildTarget = ScalaBuildTarget(
                "org.scala-lang",
                "2.12.14",
                "2.12",
                ScalaPlatform.JVM,
                listOf(
                    "file://\$BAZEL_CACHE/external/io_bazel_rules_scala_scala_compiler/scala-compiler-2.12.14.jar",
                    "file://\$BAZEL_CACHE/external/io_bazel_rules_scala_scala_library/scala-library-2.12.14.jar",
                    "file://\$BAZEL_CACHE/external/io_bazel_rules_scala_scala_reflect/scala-reflect-2.12.14.jar"
                )
            )
            scalaBuildTarget.jvmBuildTarget = jvmBuildTarget
            val scalaTargetsScalaBinary = BuildTarget(
                BuildTargetIdentifier("//scala_targets:scala_binary"),
                listOf("application"),
                listOf("scala"),
                emptyList(),
                BuildTargetCapabilities(true, false, true, false)
            )
            scalaTargetsScalaBinary.displayName = "//scala_targets:scala_binary"
            scalaTargetsScalaBinary.baseDirectory = "file://\$WORKSPACE/scala_targets/"
            scalaTargetsScalaBinary.dataKind = "scala"
            scalaTargetsScalaBinary.data = scalaBuildTarget
            val javaTargetsSubpackageSubpackage = BuildTarget(
                BuildTargetIdentifier("//java_targets/subpackage:java_library"),
                listOf("library"),
                listOf("java"),
                emptyList(),
                BuildTargetCapabilities(true, false, false, false)
            )
            javaTargetsSubpackageSubpackage.displayName = "//java_targets/subpackage:java_library"
            javaTargetsSubpackageSubpackage.baseDirectory = "file://\$WORKSPACE/java_targets/subpackage/"
            javaTargetsSubpackageSubpackage.dataKind = "jvm"
            javaTargetsSubpackageSubpackage.data = jvmBuildTarget
            val javaTargetsJavaLibrary = BuildTarget(
                BuildTargetIdentifier("//java_targets:java_library"),
                listOf("library"),
                listOf("java"),
                emptyList(),
                BuildTargetCapabilities(true, false, false, false)
            )
            javaTargetsJavaLibrary.displayName = "//java_targets:java_library"
            javaTargetsJavaLibrary.baseDirectory = "file://\$WORKSPACE/java_targets/"
            javaTargetsJavaLibrary.dataKind = "jvm"
            javaTargetsJavaLibrary.data = jvmBuildTarget
            val targetWithoutJvmFlagsBinary = BuildTarget(
                BuildTargetIdentifier("//target_without_jvm_flags:binary"),
                listOf("application"),
                listOf("scala"),
                emptyList(),
                BuildTargetCapabilities(true, false, true, false)
            )
            targetWithoutJvmFlagsBinary.displayName = "//target_without_jvm_flags:binary"
            targetWithoutJvmFlagsBinary.baseDirectory = "file://\$WORKSPACE/target_without_jvm_flags/"
            targetWithoutJvmFlagsBinary.dataKind = "scala"
            targetWithoutJvmFlagsBinary.data = scalaBuildTarget
            val targetWithoutMainClassLibrary = BuildTarget(
                BuildTargetIdentifier("//target_without_main_class:library"),
                listOf("library"),
                listOf("scala"),
                emptyList(),
                BuildTargetCapabilities(true, false, false, false)
            )
            targetWithoutMainClassLibrary.displayName = "//target_without_main_class:library"
            targetWithoutMainClassLibrary.baseDirectory = "file://\$WORKSPACE/target_without_main_class/"
            targetWithoutMainClassLibrary.dataKind = "scala"
            targetWithoutMainClassLibrary.data = scalaBuildTarget
            val targetWithoutArgsBinary = BuildTarget(
                BuildTargetIdentifier("//target_without_args:binary"),
                listOf("application"),
                listOf("scala"),
                emptyList(),
                BuildTargetCapabilities(true, false, true, false)
            )
            targetWithoutArgsBinary.displayName = "//target_without_args:binary"
            targetWithoutArgsBinary.baseDirectory = "file://\$WORKSPACE/target_without_args/"
            targetWithoutArgsBinary.dataKind = "scala"
            targetWithoutArgsBinary.data = scalaBuildTarget
            val targetWithDependencyJavaBinary = BuildTarget(
                BuildTargetIdentifier("//target_with_dependency:java_binary"),
                listOf("application"),
                listOf("java"),
                listOf(
                    BuildTargetIdentifier("//java_targets:java_library_exported"),
                    BuildTargetIdentifier("@guava//:guava"),
                    BuildTargetIdentifier("//java_targets/subpackage:java_library")
                ),
                BuildTargetCapabilities(true, false, true, false)
            )
            targetWithDependencyJavaBinary.displayName = "//target_with_dependency:java_binary"
            targetWithDependencyJavaBinary.baseDirectory = "file://\$WORKSPACE/target_with_dependency/"
            targetWithDependencyJavaBinary.dataKind = "jvm"
            targetWithDependencyJavaBinary.data = jvmBuildTarget
            val scalaTargetsScalaTest = BuildTarget(
                BuildTargetIdentifier("//scala_targets:scala_test"),
                listOf("test"),
                listOf("scala"),
                emptyList(),
                BuildTargetCapabilities(true, true, false, false)
            )
            scalaTargetsScalaTest.displayName = "//scala_targets:scala_test"
            scalaTargetsScalaTest.baseDirectory = "file://\$WORKSPACE/scala_targets/"
            scalaTargetsScalaTest.dataKind = "scala"
            scalaTargetsScalaTest.data = scalaBuildTarget
            val targetWithResourcesJavaBinary = BuildTarget(
                BuildTargetIdentifier("//target_with_resources:java_binary"),
                listOf("application"),
                listOf("java"),
                emptyList(),
                BuildTargetCapabilities(true, false, true, false)
            )
            targetWithResourcesJavaBinary.displayName = "//target_with_resources:java_binary"
            targetWithResourcesJavaBinary.baseDirectory = "file://\$WORKSPACE/target_with_resources/"
            targetWithResourcesJavaBinary.dataKind = "jvm"
            targetWithResourcesJavaBinary.data = jvmBuildTarget
            val javaTargetsJavaLibraryExported = BuildTarget(
                BuildTargetIdentifier("//java_targets:java_library_exported"),
                listOf("library"),
                emptyList(),
                listOf(BuildTargetIdentifier("//java_targets/subpackage:java_library")),
                BuildTargetCapabilities(true, false, false, false)
            )
            javaTargetsJavaLibraryExported.displayName = "//java_targets:java_library_exported"
            javaTargetsJavaLibraryExported.baseDirectory = "file://\$WORKSPACE/java_targets/"
            val bspWorkspaceRoot = BuildTarget(
                BuildTargetIdentifier("bsp-workspace-root"),
                emptyList(),
                emptyList(),
                emptyList(),
                BuildTargetCapabilities(false, false, false, false)
            )
            bspWorkspaceRoot.displayName = "bsp-workspace-root"
            bspWorkspaceRoot.baseDirectory = "file://\$WORKSPACE/"
            val manualScalaBuildTarget = ScalaBuildTarget(
                "org.scala-lang",
                "2.12.14",
                "2.12",
                ScalaPlatform.JVM,
                listOf(
                    "file://\$BAZEL_CACHE/external/io_bazel_rules_scala_scala_compiler/scala-compiler-2.12.14.jar",
                    "file://\$BAZEL_CACHE/external/io_bazel_rules_scala_scala_library/scala-library-2.12.14.jar",
                    "file://\$BAZEL_CACHE/external/io_bazel_rules_scala_scala_reflect/scala-reflect-2.12.14.jar"
                )
            )
            val manualTargetScalaLibrary = BuildTarget(
                BuildTargetIdentifier("//manual_target:scala_library"),
                listOf("library"),
                listOf("scala"),
                emptyList(),
                BuildTargetCapabilities(false, false, false, false)
            )
            manualTargetScalaLibrary.displayName = "//manual_target:scala_library"
            manualTargetScalaLibrary.baseDirectory = "file://\$WORKSPACE/manual_target/"
            manualTargetScalaLibrary.dataKind = "scala"
            manualTargetScalaLibrary.data = manualScalaBuildTarget
            val manualTargetJavaLibrary = BuildTarget(
                BuildTargetIdentifier("//manual_target:java_library"),
                listOf("library"),
                listOf("java"),
                emptyList(),
                BuildTargetCapabilities(false, false, false, false)
            )
            manualTargetJavaLibrary.displayName = "//manual_target:java_library"
            manualTargetJavaLibrary.baseDirectory = "file://\$WORKSPACE/manual_target/"
            val manualTargetScalaBinary = BuildTarget(
                BuildTargetIdentifier("//manual_target:scala_binary"),
                listOf("application"),
                listOf("scala"),
                emptyList(),
                BuildTargetCapabilities(false, false, false, false)
            )
            manualTargetScalaBinary.displayName = "//manual_target:scala_binary"
            manualTargetScalaBinary.baseDirectory = "file://\$WORKSPACE/manual_target/"
            manualTargetScalaBinary.dataKind = "scala"
            manualTargetScalaBinary.data = manualScalaBuildTarget
            val manualTargetJavaBinary = BuildTarget(
                BuildTargetIdentifier("//manual_target:java_binary"),
                listOf("application"),
                listOf("java"),
                emptyList(),
                BuildTargetCapabilities(false, false, false, false)
            )
            manualTargetJavaBinary.displayName = "//manual_target:java_binary"
            manualTargetJavaBinary.baseDirectory = "file://\$WORKSPACE/manual_target/"
            val manualTargetScalaTest = BuildTarget(
                BuildTargetIdentifier("//manual_target:scala_test"),
                listOf("test"),
                listOf("scala"),
                emptyList(),
                BuildTargetCapabilities(false, false, false, false)
            )
            manualTargetScalaTest.displayName = "//manual_target:scala_test"
            manualTargetScalaTest.baseDirectory = "file://\$WORKSPACE/manual_target/"
            manualTargetScalaTest.dataKind = "scala"
            manualTargetScalaTest.data = manualScalaBuildTarget
            val manualTargetJavaTest = BuildTarget(
                BuildTargetIdentifier("//manual_target:java_test"),
                listOf("test"),
                listOf("java"),
                emptyList(),
                BuildTargetCapabilities(false, false, false, false)
            )
            manualTargetJavaTest.displayName = "//manual_target:java_test"
            manualTargetJavaTest.baseDirectory = "file://\$WORKSPACE/manual_target/"
            return WorkspaceBuildTargetsResult(
                listOf(
                    javaTargetsJavaBinary,
                    scalaTargetsScalaBinary,
                    javaTargetsSubpackageSubpackage,
                    javaTargetsJavaLibrary,
                    targetWithoutJvmFlagsBinary,
                    targetWithoutMainClassLibrary,
                    targetWithoutArgsBinary,
                    targetWithDependencyJavaBinary,
                    scalaTargetsScalaTest,
                    targetWithResourcesJavaBinary,
                    javaTargetsJavaLibraryExported,
                    bspWorkspaceRoot,
                    manualTargetJavaLibrary,
                    manualTargetScalaLibrary,
                    manualTargetJavaBinary,
                    manualTargetScalaBinary,
                    manualTargetJavaTest,
                    manualTargetScalaTest
                )
            )
        }

    companion object {
        private const val REPO_NAME = "sample-repo"

        // we cannot use `bazel test ...` because test runner blocks bazel daemon,
        // but testing server needs it for queries and etc
        @JvmStatic
        fun main(args: Array<String>) {
            val test = BazelBspSampleRepoTest()
            test.executeScenario()
        }
    }
}