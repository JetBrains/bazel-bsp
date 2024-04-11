package org.jetbrains.bsp.bazel

import ch.epfl.scala.bsp4j.WorkspaceBuildTargetsResult
import org.jetbrains.bsp.bazel.base.BazelBspTestBaseScenario
import org.jetbrains.bsp.bazel.base.BazelBspTestScenarioStep
import org.jetbrains.bsp.bazel.install.Install
import kotlin.time.Duration.Companion.minutes

object ServerDownloadsBazeliskTest : BazelBspTestBaseScenario() {
  private val mockTestClient = createTestkitClient()

  @JvmStatic
  fun main(args: Array<String>) = executeScenario()

  override fun installServer() {
    // DO NOT supply the -b flag to test whether bazelisk is downloaded
    Install.main(
      arrayOf(
        "-d", workspaceDir, "-t", "//...", "--produce-trace-log"
      )
    )
  }

  override fun scenarioSteps(): List<BazelBspTestScenarioStep> = listOf(resolveProject())

  override fun expectedWorkspaceBuildTargetsResult(): WorkspaceBuildTargetsResult =
    WorkspaceBuildTargetsResult(listOf())

  private fun resolveProject(): BazelBspTestScenarioStep = BazelBspTestScenarioStep(
    "resolve project"
  ) { mockTestClient.testResolveProject(2.minutes) }
}
