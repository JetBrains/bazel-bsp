package org.jetbrains.bsp.bazel.base

import io.vavr.CheckedRunnable
import io.vavr.control.Try
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class BazelBspTestScenarioStep(private val testName: String, private val testkitCall: CheckedRunnable) {
    fun executeAndReturnResult(): Boolean {
        LOGGER.info("Executing \"{}\"...", testName)
        return Try.run(testkitCall)
            .onSuccess { LOGGER.info("Step \"{}\" executed correctly!", testName) }
            .onFailure { LOGGER.error("Step \"{}\" execution failed!", testName, it) }
            .map { true }
            .getOrElse(false)
    }

    companion object {
        private val LOGGER: Logger = LogManager.getLogger(
            BazelBspTestScenarioStep::class.java
        )
    }
}