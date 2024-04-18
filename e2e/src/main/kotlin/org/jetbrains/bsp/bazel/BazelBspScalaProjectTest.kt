package org.jetbrains.bsp.bazel

import ch.epfl.scala.bsp4j.*
import org.jetbrains.bsp.bazel.base.BazelBspTestBaseScenario
import org.jetbrains.bsp.bazel.base.BazelBspTestScenarioStep
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

object BazelBspScalaProjectTest : BazelBspTestBaseScenario() {
  private val testClient = createTestkitClient()

  @JvmStatic
  fun main(args: Array<String>) = executeScenario()

  override fun scenarioSteps(): List<BazelBspTestScenarioStep> = listOf(
    scalaOptionsResults(),
    compileWithWarnings(),
  )

  override fun expectedWorkspaceBuildTargetsResult(): WorkspaceBuildTargetsResult {
    return WorkspaceBuildTargetsResult(
      listOf(
        BuildTarget(
          BuildTargetIdentifier("@//scala_targets:library"),
          listOf("library"),
          listOf("scala"),
          emptyList(),
          BuildTargetCapabilities()
        )
      )
    )
  }


  private fun scalaOptionsResults(): BazelBspTestScenarioStep {
    val expectedTargetIdentifiers = expectedTargetIdentifiers().filter { it.uri != "bsp-workspace-root" }
    val expectedScalaOptionsItems = expectedTargetIdentifiers.map {
      ScalacOptionsItem(
        it,
        emptyList(),
        listOf(
          "file://\$BAZEL_OUTPUT_BASE_PATH/execroot/__main__/bazel-out/k8-fastbuild/bin/external/io_bazel_rules_scala_scala_library/io_bazel_rules_scala_scala_library.stamp/scala-library-2.13.6-stamped.jar",
          "file://\$BAZEL_OUTPUT_BASE_PATH/execroot/__main__/bazel-out/k8-fastbuild/bin/external/io_bazel_rules_scala_scala_reflect/io_bazel_rules_scala_scala_reflect.stamp/scala-reflect-2.13.6-stamped.jar"
        ),
        "file://\$BAZEL_OUTPUT_BASE_PATH/execroot/__main__/bazel-out/k8-fastbuild/bin/scala_targets/library.jar"
      )
    }
    val expectedScalaOptionsResult = ScalacOptionsResult(expectedScalaOptionsItems)
    val scalaOptionsParams = ScalacOptionsParams(expectedTargetIdentifiers)
    return BazelBspTestScenarioStep("scalaOptions results") {
      testClient.testScalacOptions(60.seconds, scalaOptionsParams, expectedScalaOptionsResult)
    }
  }

  private fun compileWithWarnings(): BazelBspTestScenarioStep {
    val expectedTargetIdentifiers = expectedTargetIdentifiers().filter { it.uri != "bsp-workspace-root" }
    val compileParams = CompileParams(expectedTargetIdentifiers);
    compileParams.originId = UUID.randomUUID().toString()

    val expectedCompilerResult = CompileResult(StatusCode.OK)
    val expectedDiagnostic = Diagnostic(
      Range(Position(2, 4), Position(2, 4)),
      "\"match may not be exhaustive.\\nIt would fail on the following input: C(_)\\n  aa match {\\n  ^\""
    )
    expectedDiagnostic.severity = DiagnosticSeverity.WARNING
    val expectedDocumentId = TextDocumentIdentifier("file://\$WORKSPACE/scala_targets/Example.scala")
    val expectedDiagnosticsParam = PublishDiagnosticsParams(
      expectedDocumentId,
      expectedTargetIdentifiers[0],
      listOf(expectedDiagnostic),
      true,
    )
    expectedDiagnosticsParam.originId = compileParams.originId

    return BazelBspTestScenarioStep("compile results") {
      testClient.testCompile(60.seconds, compileParams, expectedCompilerResult, listOf(expectedDiagnosticsParam))
    }
  }

}
