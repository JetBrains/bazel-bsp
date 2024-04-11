package org.jetbrains.bsp.bazel

import ch.epfl.scala.bsp4j.BuildTarget
import ch.epfl.scala.bsp4j.BuildTargetCapabilities
import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import ch.epfl.scala.bsp4j.DependencySourcesItem
import ch.epfl.scala.bsp4j.DependencySourcesParams
import ch.epfl.scala.bsp4j.DependencySourcesResult
import ch.epfl.scala.bsp4j.InverseSourcesParams
import ch.epfl.scala.bsp4j.InverseSourcesResult
import ch.epfl.scala.bsp4j.JvmBuildTarget
import ch.epfl.scala.bsp4j.JvmEnvironmentItem
import ch.epfl.scala.bsp4j.JvmMainClass
import ch.epfl.scala.bsp4j.JvmRunEnvironmentParams
import ch.epfl.scala.bsp4j.JvmRunEnvironmentResult
import ch.epfl.scala.bsp4j.JvmTestEnvironmentParams
import ch.epfl.scala.bsp4j.JvmTestEnvironmentResult
import ch.epfl.scala.bsp4j.ResourcesItem
import ch.epfl.scala.bsp4j.ResourcesParams
import ch.epfl.scala.bsp4j.ResourcesResult
import ch.epfl.scala.bsp4j.ScalaBuildTarget
import ch.epfl.scala.bsp4j.ScalaMainClass
import ch.epfl.scala.bsp4j.ScalaMainClassesItem
import ch.epfl.scala.bsp4j.ScalaMainClassesParams
import ch.epfl.scala.bsp4j.ScalaMainClassesResult
import ch.epfl.scala.bsp4j.ScalaPlatform
import ch.epfl.scala.bsp4j.ScalaTestClassesItem
import ch.epfl.scala.bsp4j.ScalaTestClassesParams
import ch.epfl.scala.bsp4j.ScalaTestClassesResult
import ch.epfl.scala.bsp4j.SourceItem
import ch.epfl.scala.bsp4j.SourceItemKind
import ch.epfl.scala.bsp4j.SourcesItem
import ch.epfl.scala.bsp4j.SourcesParams
import ch.epfl.scala.bsp4j.SourcesResult
import ch.epfl.scala.bsp4j.TextDocumentIdentifier
import ch.epfl.scala.bsp4j.WorkspaceBuildTargetsResult
import org.jetbrains.bsp.bazel.base.BazelBspTestBaseScenario
import org.jetbrains.bsp.bazel.base.BazelBspTestScenarioStep
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

object BazelBspSampleRepoTest : BazelBspTestBaseScenario() {
  private val testClient = createTestkitClient()

  // TODO: https://youtrack.jetbrains.com/issue/BAZEL-95
  @JvmStatic
  fun main(args: Array<String>) = executeScenario()

  override fun scenarioSteps(): List<BazelBspTestScenarioStep> =
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
      jvmTestEnvironment()
    )

  private fun resolveProject(): BazelBspTestScenarioStep = BazelBspTestScenarioStep(
    "resolve project"
  ) { testClient.testResolveProject(2.minutes) }

  private fun compareWorkspaceTargetsResults(): BazelBspTestScenarioStep = BazelBspTestScenarioStep(
    "compare workspace targets results"
  ) { testClient.testWorkspaceTargets(30.seconds, expectedWorkspaceBuildTargetsResult()) }

  private fun sourcesResults(): BazelBspTestScenarioStep {
    val targetWithoutJvmFlagsExampleScala = SourceItem(
      "file://\$WORKSPACE/target_without_jvm_flags/Example.scala", SourceItemKind.FILE, false
    )
    val targetWithoutJvmFlagsSources = SourcesItem(
      BuildTargetIdentifier("$targetPrefix//target_without_jvm_flags:binary"),
      listOf(targetWithoutJvmFlagsExampleScala)
    )
    targetWithoutJvmFlagsSources.roots = listOf("file://\$WORKSPACE/")

    val targetWithoutArgsExampleScala = SourceItem(
      "file://\$WORKSPACE/target_without_args/Example.scala", SourceItemKind.FILE, false
    )
    val targetWithoutArgsSources = SourcesItem(
      BuildTargetIdentifier("$targetPrefix//target_without_args:binary"),
      listOf(targetWithoutArgsExampleScala)
    )
    targetWithoutArgsSources.roots = listOf("file://\$WORKSPACE/")

    val targetWithoutMainClassExampleScala = SourceItem(
      "file://\$WORKSPACE/target_without_main_class/Example.scala",
      SourceItemKind.FILE,
      false
    )
    val targetWithoutMainClassSources = SourcesItem(
      BuildTargetIdentifier("$targetPrefix//target_without_main_class:library"),
      listOf(targetWithoutMainClassExampleScala)
    )
    targetWithoutMainClassSources.roots = listOf("file://\$WORKSPACE/")

    val targetWithResourcesJavaBinaryJava = SourceItem(
      "file://\$WORKSPACE/target_with_resources/JavaBinary.java", SourceItemKind.FILE, false
    )
    val targetWithResourcesSources = SourcesItem(
      BuildTargetIdentifier("$targetPrefix//target_with_resources:java_binary"),
      listOf(targetWithResourcesJavaBinaryJava)
    )
    targetWithResourcesSources.roots = listOf("file://\$WORKSPACE/")

    val targetWithDependencyJavaBinaryJava = SourceItem(
      "file://\$WORKSPACE/target_with_dependency/JavaBinary.java", SourceItemKind.FILE, false
    )
    val targetWithDependencySources = SourcesItem(
      BuildTargetIdentifier("$targetPrefix//target_with_dependency:java_binary"),
      listOf(targetWithDependencyJavaBinaryJava)
    )
    targetWithDependencySources.roots = listOf("file://\$WORKSPACE/")

    val scalaTargetsScalaBinaryScala = SourceItem(
      "file://\$WORKSPACE/scala_targets/ScalaBinary.scala", SourceItemKind.FILE, false
    )
    val scalaTargetsScalaBinarySources = SourcesItem(
      BuildTargetIdentifier("$targetPrefix//scala_targets:scala_binary"),
      listOf(scalaTargetsScalaBinaryScala)
    )
    scalaTargetsScalaBinarySources.roots = listOf("file://\$WORKSPACE/")

    val scalaTargetsScalaTestScala = SourceItem(
      "file://\$WORKSPACE/scala_targets/ScalaTest.scala", SourceItemKind.FILE, false
    )
    val scalaTargetsScalaTestSources = SourcesItem(
      BuildTargetIdentifier("$targetPrefix//scala_targets:scala_test"),
      listOf(scalaTargetsScalaTestScala)
    )
    scalaTargetsScalaTestSources.roots = listOf("file://\$WORKSPACE/")

    val javaTargetsJavaBinaryJava = SourceItem(
      "file://\$WORKSPACE/java_targets/JavaBinary.java", SourceItemKind.FILE, false
    )
    val javaTargetsJavaBinarySources = SourcesItem(
      BuildTargetIdentifier("$targetPrefix//java_targets:java_binary"),
      listOf(javaTargetsJavaBinaryJava)
    )
    javaTargetsJavaBinarySources.roots = listOf("file://\$WORKSPACE/")

    val javaTargetsJavaBinaryWithFlagJava = SourceItem(
      "file://\$WORKSPACE/java_targets/JavaBinaryWithFlag.java", SourceItemKind.FILE, false
    )
    val javaTargetsJavaBinaryWithFlagSources = SourcesItem(
      BuildTargetIdentifier("$targetPrefix//java_targets:java_binary_with_flag"),
      listOf(javaTargetsJavaBinaryWithFlagJava)
    )
    javaTargetsJavaBinaryWithFlagSources.roots = listOf("file://\$WORKSPACE/")

    val javaTargetsJavaLibraryJava = SourceItem(
      "file://\$WORKSPACE/java_targets/JavaLibrary.java", SourceItemKind.FILE, false
    )
    val javaTargetsJavaLibrarySources = SourcesItem(
      BuildTargetIdentifier("$targetPrefix//java_targets:java_library"),
      listOf(javaTargetsJavaLibraryJava)
    )
    javaTargetsJavaLibrarySources.roots = listOf("file://\$WORKSPACE/")

    val javaTargetsSubpackageJavaLibraryJava = SourceItem(
      "file://\$WORKSPACE/java_targets/subpackage/JavaLibrary2.java",
      SourceItemKind.FILE,
      false
    )
    val javaTargetsSubpackageJavaLibrarySources = SourcesItem(
      BuildTargetIdentifier("$targetPrefix//java_targets/subpackage:java_library"),
      listOf(javaTargetsSubpackageJavaLibraryJava)
    )
    javaTargetsSubpackageJavaLibrarySources.roots = listOf("file://\$WORKSPACE/")

    val javaTargetsJavaLibraryExportedSources = SourcesItem(
      BuildTargetIdentifier("$targetPrefix//java_targets:java_library_exported"), emptyList()
    )
    javaTargetsJavaLibraryExportedSources.roots = emptyList()

    val manualTargetTestJavaFile = SourceItem(
      "file://\$WORKSPACE/manual_target/TestJavaFile.java", SourceItemKind.FILE, false
    )
    val manualTargetTestJavaFileSources = SourcesItem(
      BuildTargetIdentifier("$targetPrefix//manual_target:java_library"),
      listOf(manualTargetTestJavaFile)
    )
    manualTargetTestJavaFileSources.roots = listOf("file://\$WORKSPACE/")

    val manualTargetTestScalaFile = SourceItem(
      "file://\$WORKSPACE/manual_target/TestScalaFile.scala", SourceItemKind.FILE, false
    )
    val manualTargetTestScalaFileSources = SourcesItem(
      BuildTargetIdentifier("$targetPrefix//manual_target:scala_library"),
      listOf(manualTargetTestScalaFile)
    )
    manualTargetTestScalaFileSources.roots = listOf("file://\$WORKSPACE/")

    val manualTargetTestJavaTest =
      SourceItem("file://\$WORKSPACE/manual_target/JavaTest.java", SourceItemKind.FILE, false)
    val manualTargetTestJavaTestSources = SourcesItem(
      BuildTargetIdentifier("$targetPrefix//manual_target:java_test"),
      listOf(manualTargetTestJavaTest)
    )
    manualTargetTestJavaTestSources.roots = listOf("file://\$WORKSPACE/")

    val manualTargetTestScalaTest = SourceItem(
      "file://\$WORKSPACE/manual_target/ScalaTest.scala", SourceItemKind.FILE, false
    )
    val manualTargetTestScalaTestSources = SourcesItem(
      BuildTargetIdentifier("$targetPrefix//manual_target:scala_test"),
      listOf(manualTargetTestScalaTest)
    )
    manualTargetTestScalaTestSources.roots = listOf("file://\$WORKSPACE/")

    val manualTargetTestJavaBinary = SourceItem(
      "file://\$WORKSPACE/manual_target/TestJavaBinary.java", SourceItemKind.FILE, false
    )
    val manualTargetTestJavaBinarySources = SourcesItem(
      BuildTargetIdentifier("$targetPrefix//manual_target:java_binary"),
      listOf(manualTargetTestJavaBinary)
    )
    manualTargetTestJavaBinarySources.roots = listOf("file://\$WORKSPACE/")

    val manualTargetTestScalaBinary = SourceItem(
      "file://\$WORKSPACE/manual_target/TestScalaBinary.scala", SourceItemKind.FILE, false
    )
    val manualTargetTestScalaBinarySources = SourcesItem(
      BuildTargetIdentifier("$targetPrefix//manual_target:scala_binary"),
      listOf(manualTargetTestScalaBinary)
    )
    manualTargetTestScalaBinarySources.roots = listOf("file://\$WORKSPACE/")

    val environmentVariablesJavaBinary =
      SourceItem("file://\$WORKSPACE/environment_variables/JavaEnv.java", SourceItemKind.FILE, false)
    val environmentVariablesJavaBinarySources = SourcesItem(
      BuildTargetIdentifier("$targetPrefix//environment_variables:java_binary"),
      listOf(environmentVariablesJavaBinary)
    )
    environmentVariablesJavaBinarySources.roots = listOf("file://\$WORKSPACE/")

    val environmentVariablesJavaTest =
      SourceItem("file://\$WORKSPACE/environment_variables/JavaTest.java", SourceItemKind.FILE, false)
    val environmentVariablesJavaTestSources = SourcesItem(
      BuildTargetIdentifier("$targetPrefix//environment_variables:java_test"),
      listOf(environmentVariablesJavaTest)
    )
    environmentVariablesJavaTestSources.roots = listOf("file://\$WORKSPACE/")

    val sourcesParams = SourcesParams(expectedTargetIdentifiers())
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
        javaTargetsJavaBinaryWithFlagSources,
        javaTargetsJavaLibrarySources,
        javaTargetsSubpackageJavaLibrarySources,
        javaTargetsJavaLibraryExportedSources,
        manualTargetTestJavaFileSources,
        manualTargetTestScalaFileSources,
        manualTargetTestJavaBinarySources,
        manualTargetTestScalaBinarySources,
        manualTargetTestJavaTestSources,
        manualTargetTestScalaTestSources,
        environmentVariablesJavaBinarySources,
        environmentVariablesJavaTestSources
      )
    )
    return BazelBspTestScenarioStep("sources results") {
      testClient.testSources(30.seconds, sourcesParams, expectedSourcesResult)
    }
  }

  private fun resourcesResults(): BazelBspTestScenarioStep {
    val targetWithResources = ResourcesItem(
      BuildTargetIdentifier("$targetPrefix//target_with_resources:java_binary"),
      listOf(
        "file://\$WORKSPACE/target_with_resources/file1.txt",
        "file://\$WORKSPACE/target_with_resources/file2.txt"
      )
    )
    val javaTargetsSubpackageJavaLibrary = ResourcesItem(
      BuildTargetIdentifier("$targetPrefix//java_targets/subpackage:java_library"), emptyList()
    )
    val javaTargetsJavaBinary =
      ResourcesItem(BuildTargetIdentifier("$targetPrefix//java_targets:java_binary"), emptyList())
    val javaTargetsJavaBinaryWithFlag =
      ResourcesItem(BuildTargetIdentifier("$targetPrefix//java_targets:java_binary_with_flag"), emptyList())
    val javaTargetsJavaLibrary =
      ResourcesItem(BuildTargetIdentifier("$targetPrefix//java_targets:java_library"), emptyList())
    val javaTargetsJavaLibraryExported = ResourcesItem(
      BuildTargetIdentifier("$targetPrefix//java_targets:java_library_exported"), emptyList()
    )
    val scalaTargetsScalaBinary =
      ResourcesItem(BuildTargetIdentifier("$targetPrefix//scala_targets:scala_binary"), emptyList())
    val scalaTargetsScalaTest =
      ResourcesItem(BuildTargetIdentifier("$targetPrefix//scala_targets:scala_test"), emptyList())
    val targetWithDependencyJavaBinary = ResourcesItem(
      BuildTargetIdentifier("$targetPrefix//target_with_dependency:java_binary"), emptyList()
    )
    val targetWithoutArgsBinary =
      ResourcesItem(BuildTargetIdentifier("$targetPrefix//target_without_args:binary"), emptyList())
    val targetWithoutJvmFlagsBinary = ResourcesItem(
      BuildTargetIdentifier("$targetPrefix//target_without_jvm_flags:binary"), emptyList()
    )
    val targetWithoutMainClassLibrary = ResourcesItem(
      BuildTargetIdentifier("$targetPrefix//target_without_main_class:library"), emptyList()
    )
    val manualTargetJavaLibrary =
      ResourcesItem(BuildTargetIdentifier("$targetPrefix//manual_target:java_library"), emptyList())
    val manualTargetScalaLibrary =
      ResourcesItem(BuildTargetIdentifier("$targetPrefix//manual_target:scala_library"), emptyList())
    val manualTargetJavaBinary =
      ResourcesItem(BuildTargetIdentifier("$targetPrefix//manual_target:java_binary"), emptyList())
    val manualTargetScalaBinary =
      ResourcesItem(BuildTargetIdentifier("$targetPrefix//manual_target:scala_binary"), emptyList())
    val manualTargetJavaTest =
      ResourcesItem(BuildTargetIdentifier("$targetPrefix//manual_target:java_test"), emptyList())
    val manualTargetScalaTest =
      ResourcesItem(BuildTargetIdentifier("$targetPrefix//manual_target:scala_test"), emptyList())
    val environmentVariablesJavaBinary =
      ResourcesItem(BuildTargetIdentifier("$targetPrefix//environment_variables:java_binary"), emptyList())
    val environmentVariablesJavaTest =
      ResourcesItem(BuildTargetIdentifier("$targetPrefix//environment_variables:java_test"), emptyList())

    val expectedResourcesResult = ResourcesResult(
      listOf(
        targetWithResources,
        javaTargetsSubpackageJavaLibrary,
        javaTargetsJavaBinary,
        javaTargetsJavaBinaryWithFlag,
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
        manualTargetScalaTest,
        environmentVariablesJavaBinary,
        environmentVariablesJavaTest
      )
    )
    val resourcesParams = ResourcesParams(expectedTargetIdentifiers())
    return BazelBspTestScenarioStep(
      "resources results"
    ) {
      testClient.testResources(30.seconds, resourcesParams, expectedResourcesResult)
    }
  }

  private fun inverseSourcesResults(): BazelBspTestScenarioStep {
    val inverseSourcesDocument = TextDocumentIdentifier("file://\$WORKSPACE/java_targets/JavaBinary.java")
    val expectedInverseSourcesResult =
      InverseSourcesResult(listOf(BuildTargetIdentifier("$targetPrefix//java_targets:java_binary")))
    val inverseSourcesParams = InverseSourcesParams(inverseSourcesDocument)
    return BazelBspTestScenarioStep(
      "inverse sources results"
    ) {
      testClient.testInverseSources(
        30.seconds, inverseSourcesParams, expectedInverseSourcesResult
      )
    }
  }

  // FIXME: Is it even correct? Here when queried for *all* targets we return only the ones that
  //   are actually Scala ones. It kinda makes sense, but it seems to be inconsistent.
  private fun scalaMainClasses(): BazelBspTestScenarioStep {
    val scalaMainClassesParams = ScalaMainClassesParams(expectedTargetIdentifiers())
    val scalaTargetsScalaBinary = ScalaMainClassesItem(
      BuildTargetIdentifier("$targetPrefix//scala_targets:scala_binary"),
      listOf(
        ScalaMainClass(
          "example.Example", listOf("arg1", "arg2"), listOf("-Xms2G -Xmx5G")
        )
      )
    )
    val targetWithoutJvmFlagsBinary = ScalaMainClassesItem(
      BuildTargetIdentifier("$targetPrefix//target_without_jvm_flags:binary"),
      listOf(ScalaMainClass("example.Example", listOf("arg1", "arg2"), emptyList()))
    )
    val targetWithoutArgsBinary = ScalaMainClassesItem(
      BuildTargetIdentifier("$targetPrefix//target_without_args:binary"),
      listOf(
        ScalaMainClass(
          "example.Example",
          emptyList(),
          listOf("-Xms2G -Xmx5G")
        )
      )
    )

    val manualTargetScalaBinary = ScalaMainClassesItem(
      BuildTargetIdentifier("$targetPrefix//manual_target:scala_binary"),
      listOf(ScalaMainClass("manual_target.TestScalaBinary", emptyList(), emptyList()))
    )

    // FIXME: I'd like to add a test case where target's environment variables field is non-null.
    //  But how can I even populate it?
    //  Apparently it is populated in JVM run environment?
    val expectedScalaMainClassesResult = ScalaMainClassesResult(
      listOf(scalaTargetsScalaBinary, targetWithoutJvmFlagsBinary, targetWithoutArgsBinary, manualTargetScalaBinary)
    )
    return BazelBspTestScenarioStep(
      "Scala main classes"
    ) {
      testClient.testScalaMainClasses(
        30.seconds, scalaMainClassesParams, expectedScalaMainClassesResult
      )
    }
  }

  // FIXME: Re-add a spec2 test target. But that requires messing with the bazel toolchain
  //  See:
  // https://github.com/bazelbuild/rules_scala/tree/9b85affa2e08a350a4315882b602eda55b262356/examples/testing/multi_frameworks_toolchain
  //  See: https://github.com/JetBrains/bazel-bsp/issues/96
  private fun scalaTestClasses(): BazelBspTestScenarioStep {
    val scalaTestClassesParams = ScalaTestClassesParams(expectedTargetIdentifiers())
    val scalaTargetsScalaTest = ScalaTestClassesItem(
      BuildTargetIdentifier("$targetPrefix//scala_targets:scala_test"),
      listOf("io.bazel.rulesscala.scala_test.Runner")
    )

    val manualTargetScalaTest = ScalaTestClassesItem(
      BuildTargetIdentifier("$targetPrefix//manual_target:scala_test"),
      listOf("io.bazel.rulesscala.scala_test.Runner")
    )

    val expectedScalaTestClassesResult = ScalaTestClassesResult(listOf(scalaTargetsScalaTest, manualTargetScalaTest))
    return BazelBspTestScenarioStep(
      "Scala test classes"
    ) {
      testClient.testScalaTestClasses(
        30.seconds, scalaTestClassesParams, expectedScalaTestClassesResult
      )
    }
  }

  private fun dependencySourcesResults(): BazelBspTestScenarioStep {
    val javaTargetsJavaBinary = DependencySourcesItem(
      BuildTargetIdentifier("$targetPrefix//java_targets:java_binary"), emptyList()
    )
    val javaTargetsJavaBinaryWithFlag = DependencySourcesItem(
      BuildTargetIdentifier("$targetPrefix//java_targets:java_binary_with_flag"), emptyList()
    )
    val javaTargetsJavaLibrary = DependencySourcesItem(
      BuildTargetIdentifier("$targetPrefix//java_targets:java_library"), emptyList()
    )
    val targetWithDependencyJavaBinary = DependencySourcesItem(
      BuildTargetIdentifier("$targetPrefix//target_with_dependency:java_binary"),
      listOf(
        "file://\$BAZEL_OUTPUT_BASE_PATH/external/guava/guava-28.0-jre-src.jar"
      )
    )
    val javaTargetsSubpackageJavaLibrary = DependencySourcesItem(
      BuildTargetIdentifier("$targetPrefix//java_targets/subpackage:java_library"), emptyList()
    )
    val scalaTargetsScalaBinary = DependencySourcesItem(
      BuildTargetIdentifier("$targetPrefix//scala_targets:scala_binary"), emptyList()
    )
    val scalaTargetsScalaTest = DependencySourcesItem(
      BuildTargetIdentifier("$targetPrefix//scala_targets:scala_test"), emptyList()
    )
    val targetWithResourcesJavaBinary = DependencySourcesItem(
      BuildTargetIdentifier("$targetPrefix//target_with_resources:java_binary"), emptyList()
    )
    val targetWithoutArgsBinary = DependencySourcesItem(
      BuildTargetIdentifier("$targetPrefix//target_without_args:binary"), emptyList()
    )
    val targetWithoutJvmFlagsBinary = DependencySourcesItem(
      BuildTargetIdentifier("$targetPrefix//target_without_jvm_flags:binary"), emptyList()
    )
    val targetWithoutMainClassLibrary = DependencySourcesItem(
      BuildTargetIdentifier("$targetPrefix//target_without_main_class:library"), emptyList()
    )
    val javaTargetsJavaLibraryExported = DependencySourcesItem(
      BuildTargetIdentifier("$targetPrefix//java_targets:java_library_exported"), emptyList()
    )
    val manualTargetJavaLibrary = DependencySourcesItem(
      BuildTargetIdentifier("$targetPrefix//manual_target:java_library"), emptyList()
    )
    val manualTargetScalaLibrary = DependencySourcesItem(
      BuildTargetIdentifier("$targetPrefix//manual_target:scala_library"), emptyList()
    )
    val manualTargetJavaBinary = DependencySourcesItem(
      BuildTargetIdentifier("$targetPrefix//manual_target:java_binary"), emptyList()
    )
    val manualTargetScalaBinary = DependencySourcesItem(
      BuildTargetIdentifier("$targetPrefix//manual_target:scala_binary"), emptyList()
    )
    val manualTargetJavaTest = DependencySourcesItem(
      BuildTargetIdentifier("$targetPrefix//manual_target:java_test"), emptyList()
    )
    val manualTargetScalaTest = DependencySourcesItem(
      BuildTargetIdentifier("$targetPrefix//manual_target:scala_test"), emptyList()
    )
    val environmentVariablesJavaBinary = DependencySourcesItem(
      BuildTargetIdentifier("$targetPrefix//environment_variables:java_binary"), emptyList()
    )
    val environmentVariablesJavaTest = DependencySourcesItem(
      BuildTargetIdentifier("$targetPrefix//environment_variables:java_test"), emptyList()
    )
    val expectedDependencies = DependencySourcesResult(
      listOf(
        javaTargetsJavaBinary,
        javaTargetsJavaBinaryWithFlag,
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
        manualTargetJavaLibrary,
        manualTargetScalaLibrary,
        manualTargetJavaBinary,
        manualTargetScalaBinary,
        manualTargetJavaTest,
        manualTargetScalaTest,
        environmentVariablesJavaBinary,
        environmentVariablesJavaTest
      )
    )
    val dependencySourcesParams = DependencySourcesParams(expectedTargetIdentifiers())
    return BazelBspTestScenarioStep(
      "dependency sources results"
    ) {
      testClient.testDependencySources(
        30.seconds, dependencySourcesParams, expectedDependencies
      )
    }
  }

  // FIXME: Environment is always empty for now until we figure out how to handle it.
  // FIXME: Working directory is not an URI???
  // FIXME: Should this return only targets which are runnable?
  private fun jvmRunEnvironment(): BazelBspTestScenarioStep {
    val params = JvmRunEnvironmentParams(expectedTargetIdentifiers())
    val expectedResult = JvmRunEnvironmentResult(
      listOf(
        JvmEnvironmentItem(
          BuildTargetIdentifier("$targetPrefix//java_targets:java_binary"),
          listOf(),
          emptyList(),
          "\$WORKSPACE",
          mapOf()
        ).apply {
          mainClasses = listOf(JvmMainClass("java_targets.JavaBinary", emptyList()))
        },
        JvmEnvironmentItem(
          BuildTargetIdentifier("$targetPrefix//java_targets:java_binary_with_flag"),
          listOf(),
          emptyList(),
          "\$WORKSPACE",
          mapOf()
        ).apply {
          mainClasses = listOf(JvmMainClass("java_targets.JavaBinaryWithFlag", emptyList()))
        },
        JvmEnvironmentItem(
          BuildTargetIdentifier("$targetPrefix//java_targets:java_library"),
          listOf(),
          emptyList(),
          "\$WORKSPACE",
          mapOf()
        ).apply {
          mainClasses = emptyList()
        },
        JvmEnvironmentItem(
          BuildTargetIdentifier("$targetPrefix//target_with_dependency:java_binary"),
          listOf(
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/guava/guava-28.0-jre.jar"
          ),
          emptyList(),
          "\$WORKSPACE",
          mapOf()
        ).apply {
          mainClasses = listOf(JvmMainClass("target_with_dependency.JavaBinary", emptyList()))
        },
        JvmEnvironmentItem(
          BuildTargetIdentifier("$targetPrefix//java_targets/subpackage:java_library"),
          listOf(),
          emptyList(),
          "\$WORKSPACE",
          mapOf()
        ).apply {
          mainClasses = emptyList()
        },
        JvmEnvironmentItem(
          BuildTargetIdentifier("$targetPrefix//scala_targets:scala_binary"),
          listOf(
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scala_library/scala-library-2.12.14.jar",
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scala_reflect/scala-reflect-2.12.14.jar"
          ),
          listOf("-Xms2G -Xmx5G"),
          "\$WORKSPACE",
          mapOf()
        ).apply {
          mainClasses = listOf(JvmMainClass("example.Example", listOf("arg1", "arg2")))
        },
        JvmEnvironmentItem(
          BuildTargetIdentifier("$targetPrefix//scala_targets:scala_test"),
          listOf(
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scala_library/scala-library-2.12.14.jar",
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scala_reflect/scala-reflect-2.12.14.jar",
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scalactic/scalactic_2.12-3.2.9.jar",
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scalatest/scalatest_2.12-3.2.9.jar",
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scalatest_compatible/scalatest-compatible-3.2.9.jar",
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scalatest_core/scalatest-core_2.12-3.2.9.jar",
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scalatest_featurespec/scalatest-featurespec_2.12-3.2.9.jar",
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scalatest_flatspec/scalatest-flatspec_2.12-3.2.9.jar",
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scalatest_freespec/scalatest-freespec_2.12-3.2.9.jar",
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scalatest_funspec/scalatest-funspec_2.12-3.2.9.jar",
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scalatest_funsuite/scalatest-funsuite_2.12-3.2.9.jar",
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scalatest_matchers_core/scalatest-matchers-core_2.12-3.2.9.jar",
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scalatest_mustmatchers/scalatest-mustmatchers_2.12-3.2.9.jar",
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scalatest_shouldmatchers/scalatest-shouldmatchers_2.12-3.2.9.jar"
          ),
          emptyList(),
          "\$WORKSPACE",
          mapOf()
        ).apply {
          mainClasses = listOf(JvmMainClass("io.bazel.rulesscala.scala_test.Runner", emptyList()))
        },
        JvmEnvironmentItem(
          BuildTargetIdentifier("$targetPrefix//target_with_resources:java_binary"),
          listOf(),
          emptyList(),
          "\$WORKSPACE",
          mapOf()
        ).apply {
          mainClasses = listOf(JvmMainClass("target_with_resources.JavaBinary", emptyList()))
        },
        JvmEnvironmentItem(
          BuildTargetIdentifier("$targetPrefix//target_without_args:binary"),
          listOf(
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scala_library/scala-library-2.12.14.jar",
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scala_reflect/scala-reflect-2.12.14.jar"
          ),
          listOf("-Xms2G -Xmx5G"),
          "\$WORKSPACE",
          mapOf()
        ).apply {
          mainClasses = listOf(JvmMainClass("example.Example", emptyList()))
        },
        JvmEnvironmentItem(
          BuildTargetIdentifier("$targetPrefix//target_without_jvm_flags:binary"),
          listOf(
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scala_library/scala-library-2.12.14.jar",
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scala_reflect/scala-reflect-2.12.14.jar"
          ),
          emptyList(),
          "\$WORKSPACE",
          mapOf()
        ).apply {
          mainClasses = listOf(JvmMainClass("example.Example", listOf("arg1", "arg2")))
        },
        JvmEnvironmentItem(
          BuildTargetIdentifier("$targetPrefix//target_without_main_class:library"),
          listOf(
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scala_library/scala-library-2.12.14.jar",
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scala_reflect/scala-reflect-2.12.14.jar"
          ),
          emptyList(),
          "\$WORKSPACE",
          mapOf()
        ).apply {
          mainClasses = emptyList()
        },
        JvmEnvironmentItem(
          BuildTargetIdentifier("$targetPrefix//environment_variables:java_binary"),
          listOf(),
          emptyList(),
          "\$WORKSPACE",
          mapOf("foo1" to "val1", "foo2" to "val2")
        ).apply {
          mainClasses = listOf(JvmMainClass("environment_variables.JavaEnv", emptyList()))
        },
        JvmEnvironmentItem(
          BuildTargetIdentifier("$targetPrefix//environment_variables:java_test"),
          listOf(
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/remote_java_tools/java_tools/Runner_deploy.jar"
          ),
          emptyList(),
          "\$WORKSPACE",
          mapOf("foo1" to "val1", "foo2" to "val2", "foo3" to "val3", "foo4" to "val4")
        ).apply {
          mainClasses = emptyList()
        },
        JvmEnvironmentItem(
          BuildTargetIdentifier("$targetPrefix//manual_target:scala_test"),
          listOf(
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scala_library/scala-library-2.12.14.jar",
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scala_reflect/scala-reflect-2.12.14.jar",
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scalactic/scalactic_2.12-3.2.9.jar",
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scalatest/scalatest_2.12-3.2.9.jar",
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scalatest_compatible/scalatest-compatible-3.2.9.jar",
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scalatest_core/scalatest-core_2.12-3.2.9.jar",
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scalatest_featurespec/scalatest-featurespec_2.12-3.2.9.jar",
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scalatest_flatspec/scalatest-flatspec_2.12-3.2.9.jar",
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scalatest_freespec/scalatest-freespec_2.12-3.2.9.jar",
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scalatest_funspec/scalatest-funspec_2.12-3.2.9.jar",
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scalatest_funsuite/scalatest-funsuite_2.12-3.2.9.jar",
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scalatest_matchers_core/scalatest-matchers-core_2.12-3.2.9.jar",
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scalatest_mustmatchers/scalatest-mustmatchers_2.12-3.2.9.jar",
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scalatest_shouldmatchers/scalatest-shouldmatchers_2.12-3.2.9.jar"
          ),
          emptyList(),
          "\$WORKSPACE",
          mapOf()
        ).apply {
          mainClasses = listOf(JvmMainClass("io.bazel.rulesscala.scala_test.Runner", emptyList()))
        },
        JvmEnvironmentItem(
          BuildTargetIdentifier("$targetPrefix//manual_target:scala_library"),
          listOf(
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scala_library/scala-library-2.12.14.jar",
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scala_reflect/scala-reflect-2.12.14.jar"
          ),
          emptyList(),
          "\$WORKSPACE",
          mapOf()
        ).apply {
          mainClasses = emptyList()
        },
        JvmEnvironmentItem(
          BuildTargetIdentifier("$targetPrefix//manual_target:scala_binary"),
          listOf(
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scala_library/scala-library-2.12.14.jar",
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scala_reflect/scala-reflect-2.12.14.jar"
          ),
          emptyList(),
          "\$WORKSPACE",
          mapOf()
        ).apply {
          mainClasses = listOf(JvmMainClass("manual_target.TestScalaBinary", emptyList()))
        },
        JvmEnvironmentItem(
          BuildTargetIdentifier("$targetPrefix//manual_target:java_test"),
          listOf(
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/remote_java_tools/java_tools/Runner_deploy.jar"
          ),
          emptyList(),
          "\$WORKSPACE",
          mapOf()
        ).apply {
          mainClasses = emptyList()
        },
        JvmEnvironmentItem(
          BuildTargetIdentifier("$targetPrefix//manual_target:java_library"),
          emptyList(),
          emptyList(),
          "\$WORKSPACE",
          mapOf()
        ).apply {
          mainClasses = emptyList()
        },
        JvmEnvironmentItem(
          BuildTargetIdentifier("$targetPrefix//manual_target:java_binary"),
          emptyList(),
          emptyList(),
          "\$WORKSPACE",
          mapOf()
        ).apply {
          mainClasses = listOf(JvmMainClass("manual_target.TestJavaBinary", emptyList()))
        }
      )
    )
    return BazelBspTestScenarioStep(
      "jvm run environment results"
    ) { testClient.testJvmRunEnvironment(30.seconds, params, expectedResult) }
  }

  // FIXME: Should this return only targets that are actually testable?
  private fun jvmTestEnvironment(): BazelBspTestScenarioStep {
    val params = JvmTestEnvironmentParams(expectedTargetIdentifiers())
    val expectedResult = JvmTestEnvironmentResult(
      listOf(
        JvmEnvironmentItem(
          BuildTargetIdentifier("$targetPrefix//java_targets:java_binary"),
          listOf(),
          emptyList(),
          "\$WORKSPACE",
          mapOf()
        ).apply {
          mainClasses = listOf(JvmMainClass("java_targets.JavaBinary", emptyList()))
        },
        JvmEnvironmentItem(
          BuildTargetIdentifier("$targetPrefix//java_targets:java_binary_with_flag"),
          listOf(),
          emptyList(),
          "\$WORKSPACE",
          mapOf()
        ).apply {
          mainClasses = listOf(JvmMainClass("java_targets.JavaBinaryWithFlag", emptyList()))
        },
        JvmEnvironmentItem(
          BuildTargetIdentifier("$targetPrefix//java_targets:java_library"),
          listOf(),
          emptyList(),
          "\$WORKSPACE",
          mapOf()
        ).apply {
          mainClasses = emptyList()
        },
        JvmEnvironmentItem(
          BuildTargetIdentifier("$targetPrefix//target_with_dependency:java_binary"),
          listOf(
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/guava/guava-28.0-jre.jar"
          ),
          emptyList(),
          "\$WORKSPACE",
          mapOf()
        ).apply {
          mainClasses = listOf(JvmMainClass("target_with_dependency.JavaBinary", emptyList()))
        },
        JvmEnvironmentItem(
          BuildTargetIdentifier("$targetPrefix//java_targets/subpackage:java_library"),
          listOf(),
          emptyList(),
          "\$WORKSPACE",
          mapOf()
        ).apply {
          mainClasses = emptyList()
        },
        JvmEnvironmentItem(
          BuildTargetIdentifier("$targetPrefix//scala_targets:scala_binary"),
          listOf(
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scala_library/scala-library-2.12.14.jar",
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scala_reflect/scala-reflect-2.12.14.jar"
          ),
          listOf("-Xms2G -Xmx5G"),
          "\$WORKSPACE",
          mapOf()
        ).apply {
          mainClasses = listOf(JvmMainClass("example.Example", listOf("arg1", "arg2")))
        },
        JvmEnvironmentItem(
          BuildTargetIdentifier("$targetPrefix//scala_targets:scala_test"),
          listOf(
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scala_library/scala-library-2.12.14.jar",
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scala_reflect/scala-reflect-2.12.14.jar",
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scalactic/scalactic_2.12-3.2.9.jar",
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scalatest/scalatest_2.12-3.2.9.jar",
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scalatest_compatible/scalatest-compatible-3.2.9.jar",
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scalatest_core/scalatest-core_2.12-3.2.9.jar",
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scalatest_featurespec/scalatest-featurespec_2.12-3.2.9.jar",
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scalatest_flatspec/scalatest-flatspec_2.12-3.2.9.jar",
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scalatest_freespec/scalatest-freespec_2.12-3.2.9.jar",
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scalatest_funspec/scalatest-funspec_2.12-3.2.9.jar",
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scalatest_funsuite/scalatest-funsuite_2.12-3.2.9.jar",
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scalatest_matchers_core/scalatest-matchers-core_2.12-3.2.9.jar",
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scalatest_mustmatchers/scalatest-mustmatchers_2.12-3.2.9.jar",
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scalatest_shouldmatchers/scalatest-shouldmatchers_2.12-3.2.9.jar"
          ),
          emptyList(),
          "\$WORKSPACE",
          mapOf()
        ).apply {
          mainClasses = listOf(JvmMainClass("io.bazel.rulesscala.scala_test.Runner", emptyList()))
        },
        JvmEnvironmentItem(
          BuildTargetIdentifier("$targetPrefix//target_with_resources:java_binary"),
          listOf(),
          emptyList(),
          "\$WORKSPACE",
          mapOf()
        ).apply {
          mainClasses = listOf(JvmMainClass("target_with_resources.JavaBinary", emptyList()))
        },
        JvmEnvironmentItem(
          BuildTargetIdentifier("$targetPrefix//target_without_args:binary"),
          listOf(
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scala_library/scala-library-2.12.14.jar",
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scala_reflect/scala-reflect-2.12.14.jar"
          ),
          listOf("-Xms2G -Xmx5G"),
          "\$WORKSPACE",
          mapOf()
        ).apply {
          mainClasses = listOf(JvmMainClass("example.Example", emptyList()))
        },
        JvmEnvironmentItem(
          BuildTargetIdentifier("$targetPrefix//target_without_jvm_flags:binary"),
          listOf(
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scala_library/scala-library-2.12.14.jar",
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scala_reflect/scala-reflect-2.12.14.jar"
          ),
          emptyList(),
          "\$WORKSPACE",
          mapOf()
        ).apply {
          mainClasses = listOf(JvmMainClass("example.Example", listOf("arg1", "arg2")))
        },
        JvmEnvironmentItem(
          BuildTargetIdentifier("$targetPrefix//target_without_main_class:library"),
          listOf(
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scala_library/scala-library-2.12.14.jar",
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scala_reflect/scala-reflect-2.12.14.jar"
          ),
          emptyList(),
          "\$WORKSPACE",
          mapOf()
        ).apply {
          mainClasses = emptyList()
        },
        JvmEnvironmentItem(
          BuildTargetIdentifier("$targetPrefix//environment_variables:java_binary"),
          listOf(),
          emptyList(),
          "\$WORKSPACE",
          mapOf("foo1" to "val1", "foo2" to "val2")
        ).apply {
          mainClasses = listOf(JvmMainClass("environment_variables.JavaEnv", emptyList()))
        },
        JvmEnvironmentItem(
          BuildTargetIdentifier("$targetPrefix//environment_variables:java_test"),
          listOf(
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/remote_java_tools/java_tools/Runner_deploy.jar"
          ),
          emptyList(),
          "\$WORKSPACE",
          mapOf("foo1" to "val1", "foo2" to "val2", "foo3" to "val3", "foo4" to "val4")
        ).apply {
          mainClasses = emptyList()
        },
        JvmEnvironmentItem(
          BuildTargetIdentifier("$targetPrefix//manual_target:scala_test"),
          listOf(
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scala_library/scala-library-2.12.14.jar",
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scala_reflect/scala-reflect-2.12.14.jar",
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scalactic/scalactic_2.12-3.2.9.jar",
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scalatest/scalatest_2.12-3.2.9.jar",
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scalatest_compatible/scalatest-compatible-3.2.9.jar",
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scalatest_core/scalatest-core_2.12-3.2.9.jar",
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scalatest_featurespec/scalatest-featurespec_2.12-3.2.9.jar",
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scalatest_flatspec/scalatest-flatspec_2.12-3.2.9.jar",
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scalatest_freespec/scalatest-freespec_2.12-3.2.9.jar",
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scalatest_funspec/scalatest-funspec_2.12-3.2.9.jar",
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scalatest_funsuite/scalatest-funsuite_2.12-3.2.9.jar",
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scalatest_matchers_core/scalatest-matchers-core_2.12-3.2.9.jar",
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scalatest_mustmatchers/scalatest-mustmatchers_2.12-3.2.9.jar",
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scalatest_shouldmatchers/scalatest-shouldmatchers_2.12-3.2.9.jar"
          ),
          emptyList(),
          "\$WORKSPACE",
          mapOf()
        ).apply {
          mainClasses = listOf(JvmMainClass("io.bazel.rulesscala.scala_test.Runner", emptyList()))
        },
        JvmEnvironmentItem(
          BuildTargetIdentifier("$targetPrefix//manual_target:scala_library"),
          listOf(
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scala_library/scala-library-2.12.14.jar",
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scala_reflect/scala-reflect-2.12.14.jar"
          ),
          emptyList(),
          "\$WORKSPACE",
          mapOf()
        ).apply {
          mainClasses = emptyList()
        },
        JvmEnvironmentItem(
          BuildTargetIdentifier("$targetPrefix//manual_target:scala_binary"),
          listOf(
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scala_library/scala-library-2.12.14.jar",
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scala_reflect/scala-reflect-2.12.14.jar"
          ),
          emptyList(),
          "\$WORKSPACE",
          mapOf()
        ).apply {
          mainClasses = listOf(JvmMainClass("manual_target.TestScalaBinary", emptyList()))
        },
        JvmEnvironmentItem(
          BuildTargetIdentifier("$targetPrefix//manual_target:java_test"),
          listOf(
            "file://\$BAZEL_OUTPUT_BASE_PATH/external/remote_java_tools/java_tools/Runner_deploy.jar"
          ),
          emptyList(),
          "\$WORKSPACE",
          mapOf()
        ).apply {
          mainClasses = emptyList()
        },
        JvmEnvironmentItem(
          BuildTargetIdentifier("$targetPrefix//manual_target:java_library"),
          emptyList(),
          emptyList(),
          "\$WORKSPACE",
          mapOf()
        ).apply {
          mainClasses = emptyList()
        },
        JvmEnvironmentItem(
          BuildTargetIdentifier("$targetPrefix//manual_target:java_binary"),
          emptyList(),
          emptyList(),
          "\$WORKSPACE",
          mapOf()
        ).apply {
          mainClasses = listOf(JvmMainClass("manual_target.TestJavaBinary", emptyList()))
        }
      )
    )
    return BazelBspTestScenarioStep(
      "jvm test environment results"
    ) { testClient.testJvmTestEnvironment(30.seconds, params, expectedResult) }
  }

  override fun expectedWorkspaceBuildTargetsResult(): WorkspaceBuildTargetsResult {
    val architecturePart = if (System.getProperty("os.arch") == "aarch64") "_aarch64" else ""
    val javaHome = "file://\$BAZEL_OUTPUT_BASE_PATH/external/remotejdk11_\$OS${architecturePart}/"
    val jvmBuildTarget = JvmBuildTarget().also {
      it.javaHome = javaHome
      it.javaVersion = "11"
    }

    val jvmBuildTargetWithFlag = JvmBuildTarget().also {
      it.javaHome = javaHome
      it.javaVersion = "8"
    }

    val javaTargetsJavaBinary = BuildTarget(
      BuildTargetIdentifier("$targetPrefix//java_targets:java_binary"),
      listOf("application"),
      listOf("java"),
      emptyList(),
      BuildTargetCapabilities().also { it.canCompile = true; it.canTest = false; it.canRun = true; it.canDebug = true }
    )
    javaTargetsJavaBinary.displayName = "$targetPrefix//java_targets:java_binary"
    javaTargetsJavaBinary.baseDirectory = "file://\$WORKSPACE/java_targets/"
    javaTargetsJavaBinary.dataKind = "jvm"
    javaTargetsJavaBinary.data = jvmBuildTarget

    val javaTargetsJavaBinaryWithFlag = BuildTarget(
      BuildTargetIdentifier("$targetPrefix//java_targets:java_binary_with_flag"),
      listOf("application"),
      listOf("java"),
      emptyList(),
      BuildTargetCapabilities().also { it.canCompile = true; it.canTest = false; it.canRun = true; it.canDebug = true }
    )
    javaTargetsJavaBinaryWithFlag.displayName = "$targetPrefix//java_targets:java_binary_with_flag"
    javaTargetsJavaBinaryWithFlag.baseDirectory = "file://\$WORKSPACE/java_targets/"
    javaTargetsJavaBinaryWithFlag.dataKind = "jvm"
    javaTargetsJavaBinaryWithFlag.data = jvmBuildTargetWithFlag

    val scalaBuildTarget = ScalaBuildTarget(
      "org.scala-lang",
      "2.12.14",
      "2.12",
      ScalaPlatform.JVM,
      listOf(
        "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scala_compiler/scala-compiler-2.12.14.jar",
        "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scala_library/scala-library-2.12.14.jar",
        "file://\$BAZEL_OUTPUT_BASE_PATH/external/io_bazel_rules_scala_scala_reflect/scala-reflect-2.12.14.jar"
      )
    )
    scalaBuildTarget.jvmBuildTarget = jvmBuildTarget

    val scalaTargetsScalaBinary = BuildTarget(
      BuildTargetIdentifier("$targetPrefix//scala_targets:scala_binary"),
      listOf("application"),
      listOf("scala"),
      listOf(
        BuildTargetIdentifier("scala-compiler-2.12.14.jar"),
        BuildTargetIdentifier("scala-library-2.12.14.jar"),
        BuildTargetIdentifier("scala-reflect-2.12.14.jar"),
      ),
      BuildTargetCapabilities().also { it.canCompile = true; it.canTest = false; it.canRun = true; it.canDebug = true }
    )
    scalaTargetsScalaBinary.displayName = "$targetPrefix//scala_targets:scala_binary"
    scalaTargetsScalaBinary.baseDirectory = "file://\$WORKSPACE/scala_targets/"
    scalaTargetsScalaBinary.dataKind = "scala"
    scalaTargetsScalaBinary.data = scalaBuildTarget

    val javaTargetsSubpackageSubpackage = BuildTarget(
      BuildTargetIdentifier("$targetPrefix//java_targets/subpackage:java_library"),
      listOf("library"),
      listOf("java"),
      emptyList(),
      BuildTargetCapabilities().also {
        it.canCompile = true; it.canTest = false; it.canRun = false; it.canDebug = false
      }
    )
    javaTargetsSubpackageSubpackage.displayName = "$targetPrefix//java_targets/subpackage:java_library"
    javaTargetsSubpackageSubpackage.baseDirectory = "file://\$WORKSPACE/java_targets/subpackage/"
    javaTargetsSubpackageSubpackage.dataKind = "jvm"
    javaTargetsSubpackageSubpackage.data = jvmBuildTarget

    val javaTargetsJavaLibrary = BuildTarget(
      BuildTargetIdentifier("$targetPrefix//java_targets:java_library"),
      listOf("library"),
      listOf("java"),
      listOf(),
      BuildTargetCapabilities().also {
        it.canCompile = true; it.canTest = false; it.canRun = false; it.canDebug = false
      }
    )
    javaTargetsJavaLibrary.displayName = "$targetPrefix//java_targets:java_library"
    javaTargetsJavaLibrary.baseDirectory = "file://\$WORKSPACE/java_targets/"
    javaTargetsJavaLibrary.dataKind = "jvm"
    javaTargetsJavaLibrary.data = jvmBuildTarget

    val targetWithoutJvmFlagsBinary = BuildTarget(
      BuildTargetIdentifier("$targetPrefix//target_without_jvm_flags:binary"),
      listOf("application"),
      listOf("scala"),
      listOf(
        BuildTargetIdentifier("scala-compiler-2.12.14.jar"),
        BuildTargetIdentifier("scala-library-2.12.14.jar"),
        BuildTargetIdentifier("scala-reflect-2.12.14.jar"),
      ),
      BuildTargetCapabilities().also { it.canCompile = true; it.canTest = false; it.canRun = true; it.canDebug = true }
    )
    targetWithoutJvmFlagsBinary.displayName = "$targetPrefix//target_without_jvm_flags:binary"
    targetWithoutJvmFlagsBinary.baseDirectory = "file://\$WORKSPACE/target_without_jvm_flags/"
    targetWithoutJvmFlagsBinary.dataKind = "scala"
    targetWithoutJvmFlagsBinary.data = scalaBuildTarget

    val targetWithoutMainClassLibrary = BuildTarget(
      BuildTargetIdentifier("$targetPrefix//target_without_main_class:library"),
      listOf("library"),
      listOf("scala"),
      listOf(
        BuildTargetIdentifier("scala-compiler-2.12.14.jar"),
        BuildTargetIdentifier("scala-library-2.12.14.jar"),
        BuildTargetIdentifier("scala-reflect-2.12.14.jar"),
      ),
      BuildTargetCapabilities().also {
        it.canCompile = true; it.canTest = false; it.canRun = false; it.canDebug = false
      }
    )
    targetWithoutMainClassLibrary.displayName = "$targetPrefix//target_without_main_class:library"
    targetWithoutMainClassLibrary.baseDirectory = "file://\$WORKSPACE/target_without_main_class/"
    targetWithoutMainClassLibrary.dataKind = "scala"
    targetWithoutMainClassLibrary.data = scalaBuildTarget

    val targetWithoutArgsBinary = BuildTarget(
      BuildTargetIdentifier("$targetPrefix//target_without_args:binary"),
      listOf("application"),
      listOf("scala"),
      listOf(
        BuildTargetIdentifier("scala-compiler-2.12.14.jar"),
        BuildTargetIdentifier("scala-library-2.12.14.jar"),
        BuildTargetIdentifier("scala-reflect-2.12.14.jar"),
      ),
      BuildTargetCapabilities().also { it.canCompile = true; it.canTest = false; it.canRun = true; it.canDebug = true }
    )
    targetWithoutArgsBinary.displayName = "$targetPrefix//target_without_args:binary"
    targetWithoutArgsBinary.baseDirectory = "file://\$WORKSPACE/target_without_args/"
    targetWithoutArgsBinary.dataKind = "scala"
    targetWithoutArgsBinary.data = scalaBuildTarget

    val targetWithDependencyJavaBinary = BuildTarget(
      BuildTargetIdentifier("$targetPrefix//target_with_dependency:java_binary"),
      listOf("application"),
      listOf("java"),
      listOf(
        BuildTargetIdentifier("$targetPrefix//java_targets:java_library_exported"),
        BuildTargetIdentifier("@guava//:guava"),
        BuildTargetIdentifier("$targetPrefix//java_targets/subpackage:java_library")
      ),
      BuildTargetCapabilities().also { it.canCompile = true; it.canTest = false; it.canRun = true; it.canDebug = true }
    )
    targetWithDependencyJavaBinary.displayName = "$targetPrefix//target_with_dependency:java_binary"
    targetWithDependencyJavaBinary.baseDirectory = "file://\$WORKSPACE/target_with_dependency/"
    targetWithDependencyJavaBinary.dataKind = "jvm"
    targetWithDependencyJavaBinary.data = jvmBuildTarget

    val scalaTargetsScalaTest = BuildTarget(
      BuildTargetIdentifier("$targetPrefix//scala_targets:scala_test"),
      listOf("test"),
      listOf("scala"),
      listOf(
        BuildTargetIdentifier("scala-compiler-2.12.14.jar"),
        BuildTargetIdentifier("scala-library-2.12.14.jar"),
        BuildTargetIdentifier("scala-reflect-2.12.14.jar"),
      ),
      BuildTargetCapabilities().also { it.canCompile = true; it.canTest = true; it.canRun = false; it.canDebug = true }
    )
    scalaTargetsScalaTest.displayName = "$targetPrefix//scala_targets:scala_test"
    scalaTargetsScalaTest.baseDirectory = "file://\$WORKSPACE/scala_targets/"
    scalaTargetsScalaTest.dataKind = "scala"
    scalaTargetsScalaTest.data = scalaBuildTarget

    val targetWithResourcesJavaBinary = BuildTarget(
      BuildTargetIdentifier("$targetPrefix//target_with_resources:java_binary"),
      listOf("application"),
      listOf("java"),
      emptyList(),
      BuildTargetCapabilities().also { it.canCompile = true; it.canTest = false; it.canRun = true; it.canDebug = true }
    )
    targetWithResourcesJavaBinary.displayName = "$targetPrefix//target_with_resources:java_binary"
    targetWithResourcesJavaBinary.baseDirectory = "file://\$WORKSPACE/target_with_resources/"
    targetWithResourcesJavaBinary.dataKind = "jvm"
    targetWithResourcesJavaBinary.data = jvmBuildTarget

    val javaTargetsJavaLibraryExported = BuildTarget(
      BuildTargetIdentifier("$targetPrefix//java_targets:java_library_exported"),
      listOf("library"),
      emptyList(),
      listOf(BuildTargetIdentifier("$targetPrefix//java_targets/subpackage:java_library")),
      BuildTargetCapabilities().also {
        it.canCompile = true; it.canTest = false; it.canRun = false; it.canDebug = false
      }
    )
    javaTargetsJavaLibraryExported.displayName = "$targetPrefix//java_targets:java_library_exported"
    javaTargetsJavaLibraryExported.baseDirectory = "file://\$WORKSPACE/java_targets/"

    val manualTargetScalaLibrary = BuildTarget(
      BuildTargetIdentifier("$targetPrefix//manual_target:scala_library"),
      listOf("library"),
      listOf("scala"),
      listOf(
        BuildTargetIdentifier("scala-compiler-2.12.14.jar"),
        BuildTargetIdentifier("scala-library-2.12.14.jar"),
        BuildTargetIdentifier("scala-reflect-2.12.14.jar"),
      ),
      BuildTargetCapabilities().also {
        it.canCompile = false; it.canTest = false; it.canRun = false; it.canDebug = false
      }
    )
    manualTargetScalaLibrary.displayName = "$targetPrefix//manual_target:scala_library"
    manualTargetScalaLibrary.baseDirectory = "file://\$WORKSPACE/manual_target/"
    manualTargetScalaLibrary.dataKind = "scala"
    manualTargetScalaLibrary.data = scalaBuildTarget

    val manualTargetJavaLibrary = BuildTarget(
      BuildTargetIdentifier("$targetPrefix//manual_target:java_library"),
      listOf("library"),
      listOf("java"),
      emptyList(),
      BuildTargetCapabilities().also {
        it.canCompile = false; it.canTest = false; it.canRun = false; it.canDebug = false
      }
    )
    manualTargetJavaLibrary.displayName = "$targetPrefix//manual_target:java_library"
    manualTargetJavaLibrary.baseDirectory = "file://\$WORKSPACE/manual_target/"
    manualTargetJavaLibrary.dataKind = "jvm"
    manualTargetJavaLibrary.data = jvmBuildTarget

    val manualTargetScalaBinary = BuildTarget(
      BuildTargetIdentifier("$targetPrefix//manual_target:scala_binary"),
      listOf("application"),
      listOf("scala"),
      listOf(
        BuildTargetIdentifier("scala-compiler-2.12.14.jar"),
        BuildTargetIdentifier("scala-library-2.12.14.jar"),
        BuildTargetIdentifier("scala-reflect-2.12.14.jar"),
      ),
      BuildTargetCapabilities().also {
        it.canCompile = false; it.canTest = false; it.canRun = false; it.canDebug = false
      }
    )
    manualTargetScalaBinary.displayName = "$targetPrefix//manual_target:scala_binary"
    manualTargetScalaBinary.baseDirectory = "file://\$WORKSPACE/manual_target/"
    manualTargetScalaBinary.dataKind = "scala"
    manualTargetScalaBinary.data = scalaBuildTarget

    val manualTargetJavaBinary = BuildTarget(
      BuildTargetIdentifier("$targetPrefix//manual_target:java_binary"),
      listOf("application"),
      listOf("java"),
      emptyList(),
      BuildTargetCapabilities().also {
        it.canCompile = false; it.canTest = false; it.canRun = false; it.canDebug = false
      }
    )
    manualTargetJavaBinary.displayName = "$targetPrefix//manual_target:java_binary"
    manualTargetJavaBinary.baseDirectory = "file://\$WORKSPACE/manual_target/"
    manualTargetJavaBinary.dataKind = "jvm"
    manualTargetJavaBinary.data = jvmBuildTarget

    val manualTargetScalaTest = BuildTarget(
      BuildTargetIdentifier("$targetPrefix//manual_target:scala_test"),
      listOf("test"),
      listOf("scala"),
      listOf(
        BuildTargetIdentifier("scala-compiler-2.12.14.jar"),
        BuildTargetIdentifier("scala-library-2.12.14.jar"),
        BuildTargetIdentifier("scala-reflect-2.12.14.jar"),
      ),
      BuildTargetCapabilities().also {
        it.canCompile = false; it.canTest = false; it.canRun = false; it.canDebug = false
      }
    )
    manualTargetScalaTest.displayName = "$targetPrefix//manual_target:scala_test"
    manualTargetScalaTest.baseDirectory = "file://\$WORKSPACE/manual_target/"
    manualTargetScalaTest.dataKind = "scala"
    manualTargetScalaTest.data = scalaBuildTarget

    val manualTargetJavaTest = BuildTarget(
      BuildTargetIdentifier("$targetPrefix//manual_target:java_test"),
      listOf("test"),
      listOf("java"),
      emptyList(),
      BuildTargetCapabilities().also {
        it.canCompile = false; it.canTest = false; it.canRun = false; it.canDebug = false
      }
    )
    manualTargetJavaTest.displayName = "$targetPrefix//manual_target:java_test"
    manualTargetJavaTest.baseDirectory = "file://\$WORKSPACE/manual_target/"
    manualTargetJavaTest.dataKind = "jvm"
    manualTargetJavaTest.data = jvmBuildTarget

    val environmentVariablesJavaLibrary = BuildTarget(
      BuildTargetIdentifier("$targetPrefix//environment_variables:java_binary"),
      listOf("application"),
      listOf("java"),
      emptyList(),
      BuildTargetCapabilities().also { it.canCompile = true; it.canTest = false; it.canRun = true; it.canDebug = true }
    )
    environmentVariablesJavaLibrary.displayName = "$targetPrefix//environment_variables:java_binary"
    environmentVariablesJavaLibrary.baseDirectory = "file://\$WORKSPACE/environment_variables/"
    environmentVariablesJavaLibrary.dataKind = "jvm"
    environmentVariablesJavaLibrary.data = jvmBuildTarget

    val environmentVariablesJavaTest = BuildTarget(
      BuildTargetIdentifier("$targetPrefix//environment_variables:java_test"),
      listOf("test"),
      listOf("java"),
      emptyList(),
      BuildTargetCapabilities().also { it.canCompile = true; it.canTest = true; it.canRun = false; it.canDebug = true }
    )
    environmentVariablesJavaTest.displayName = "$targetPrefix//environment_variables:java_test"
    environmentVariablesJavaTest.baseDirectory = "file://\$WORKSPACE/environment_variables/"
    environmentVariablesJavaTest.dataKind = "jvm"
    environmentVariablesJavaTest.data = jvmBuildTarget

    return WorkspaceBuildTargetsResult(
      listOf(
        javaTargetsJavaBinary,
        javaTargetsJavaBinaryWithFlag,
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
        manualTargetJavaLibrary,
        manualTargetScalaLibrary,
        manualTargetJavaBinary,
        manualTargetScalaBinary,
        manualTargetJavaTest,
        manualTargetScalaTest,
        environmentVariablesJavaLibrary,
        environmentVariablesJavaTest
      )
    )
  }
}
