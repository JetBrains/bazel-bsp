package org.jetbrains.bsp.bazel

import ch.epfl.scala.bsp4j.BuildTarget
import ch.epfl.scala.bsp4j.BuildTargetCapabilities
import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import ch.epfl.scala.bsp4j.WorkspaceBuildTargetsResult
import ch.epfl.scala.bsp4j.PythonBuildTarget
import ch.epfl.scala.bsp4j.BuildTargetDataKind
import ch.epfl.scala.bsp4j.ScalaBuildTarget
import ch.epfl.scala.bsp4j.ScalacOptionsItem
import ch.epfl.scala.bsp4j.ScalacOptionsParams
import ch.epfl.scala.bsp4j.ScalacOptionsResult
import org.jetbrains.bsp.bazel.base.BazelBspTestBaseScenario
import org.jetbrains.bsp.bazel.base.BazelBspTestScenarioStep

import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

object BazelBspScalaProjectTest : BazelBspTestBaseScenario() {

    @JvmStatic
    fun main(args: Array<String>) = executeScenario()

    override fun scenarioSteps(): List<BazelBspTestScenarioStep> = listOf(
        scalaOptionsResults(),
    )

    override fun expectedWorkspaceBuildTargetsResult(): WorkspaceBuildTargetsResult {
        return WorkspaceBuildTargetsResult(
            emptyList()
        )
    }


    private fun scalaOptionsResults(): BazelBspTestScenarioStep {
        val expectedTargetIdentifiers = expectedTargetIdentifiers().filter { it.uri != "bsp-workspace-root" }
        val expectedScalaOptionsItems = expectedTargetIdentifiers.map { ScalacOptionsItem(it, emptyList(), emptyList(), "") }
        val expectedScalaOptionsResult = ScalacOptionsResult(expectedScalaOptionsItems)
        val pythonOptionsParams = ScalacOptionsParams(expectedTargetIdentifiers)

        return BazelBspTestScenarioStep(
            "scalaOptions results"
        ) {
            testClient.testScalacOptions(30.seconds, pythonOptionsParams, expectedScalaOptionsResult)
        }
    }

}
