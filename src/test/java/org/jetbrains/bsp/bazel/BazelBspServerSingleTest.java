package org.jetbrains.bsp.bazel;

import io.vavr.control.Try;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BazelBspServerSingleTest {

  private static final Logger LOGGER = LogManager.getLogger(BazelBspServerSingleTest.class);

  private final String testName;
  private final Runnable testToRun;

  private Optional<Future<?>> submittedTest;

  public BazelBspServerSingleTest(String testName, Runnable testToRun) {
    this.testName = testName;
    this.testToRun = testToRun;
    this.submittedTest = Optional.empty();
  }

  public BazelBspServerSingleTest submit(ExecutorService executorService) {
    submittedTest = Optional.of(executorService.submit(testToRun));

    return this;
  }

  public boolean executeTestWithTimeoutAndReturnTrueIfPassed(int timeoutInMinutes) {
    return submittedTest
        .map(test -> getSubmittedTestWithTimeout(test, timeoutInMinutes))
        .orElse(false);
  }

  private boolean getSubmittedTestWithTimeout(Future<?> submittedTest, int timeoutInMinutes) {
    LOGGER.info("Running \"{}\" test...", testName);

    return Try.of(() -> submittedTest.get(timeoutInMinutes, TimeUnit.MINUTES))
        .onSuccess(e -> LOGGER.info("Test \"{}\" passed!", testName))
        .onFailure(e -> LOGGER.error("Test \"{}\" failed! Exception: {}", testName, e))
        .map(i -> true)
        .getOrElse(false);
  }
}
