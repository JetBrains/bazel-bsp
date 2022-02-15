package org.jetbrains.bsp.bazel.base;

import io.vavr.CheckedRunnable;
import io.vavr.control.Try;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BazelBspTestScenarioStep {

  private static final Logger LOGGER = LogManager.getLogger(BazelBspTestScenarioStep.class);

  private final String testName;
  private final CheckedRunnable testkitCall;

  public BazelBspTestScenarioStep(String testName, CheckedRunnable testkitCall) {
    this.testName = testName;
    this.testkitCall = testkitCall;
  }

  public boolean executeAndReturnResult() {
    LOGGER.info("Executing \"{}\"...", testName);

    return Try.run(testkitCall)
        .onSuccess(e -> LOGGER.info("Step \"{}\" executed correctly!", testName))
        .onFailure(e -> LOGGER.error("Step \"{}\" execution failed!", testName, e))
        .map(i -> true)
        .getOrElse(false);
  }
}
