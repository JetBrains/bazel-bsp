package org.jetbrains.bsp.bazel.base

import org.apache.logging.log4j.LogManager

class BazelBspTestScenarioStep(private val testName: String, private val testkitCall: () -> Unit) {
  fun executeAndReturnResult(): Boolean {
    log.info("Executing \"$testName\"...")

    return try {
      testkitCall()
      log.info("Step \"$testName\" successful!")
      true
    } catch (e: Exception) {
      log.error("Step \"$testName\" failed!", e)
      false
    }
  }

  companion object {
    private val log = LogManager.getLogger(BazelBspTestScenarioStep::class.java)
  }
}
