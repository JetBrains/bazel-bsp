package org.jetbrains.bsp.bazel;

import ch.epfl.scala.bsp.testkit.client.TestClient;
import ch.epfl.scala.bsp.testkit.client.TestClient$;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.vavr.control.Try;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class BazelBspServerTest {

  private static Logger logger = LogManager.getLogger(BazelBspServerTest.class);

  private final TestClient client;
  private final ExecutorService executorService = Executors.newCachedThreadPool();


  public BazelBspServerTest() {
    logger.info("Creating TestClient");

    this.client = TestClient$
        .MODULE$
        .testInitialStructure(
            BazelBspServerTestData.WORKSPACE_FULL_PATH,
            ImmutableMap.of(),
            BazelBspServerTestData.TEST_CLIENT_TIMEOUT_IN_MINUTES);

    logger.info("Created TestClient");
  }


  public void run() {
    logger.info("Running BazelBspServerTest...");

    List<Runnable> testsTopRun = getTestsTopRun();
    runTests(testsTopRun);
  }

  private List<Runnable> getTestsTopRun() {
    return ImmutableList.of(
        client::testResolveProject,
        () -> client.testCompareWorkspaceTargetsResults(BazelBspServerTestData.EXPECTED_BUILD_TARGETS),
        () -> client.testSourcesResults(BazelBspServerTestData.EXPECTED_BUILD_TARGETS, BazelBspServerTestData.EXPECTED_SOURCES),
        () -> client.testResourcesResults(BazelBspServerTestData.EXPECTED_BUILD_TARGETS, BazelBspServerTestData.EXPECTED_RESOURCES),
        () -> client.testInverseSourcesResults(BazelBspServerTestData.INVERSE_SOURCES_DOCUMENT, BazelBspServerTestData.EXPECTED_INVERSE_SOURCES),
        () -> client.testDependencySourcesResults(BazelBspServerTestData.EXPECTED_BUILD_TARGETS, BazelBspServerTestData.EXPECTED_DEPENDENCIES)
//         TODO one day we will uncomment them...
//       client::testTargetsRunUnsuccessfully,
//       client::testTargetsTestUnsuccessfully,
//       client::testTargetCapabilities,
    );
  }

  private void runTests(List<Runnable> tests) {
    List<Future<?>> submittedTests = submitTestsForExecution(tests);
    boolean didAllTestsPass = executeAllTestsAndReturnTrueIfAllPassed(submittedTests);

    exitProgramWithSuccessIfAllTestPassed(didAllTestsPass);
  }

  private List<Future<?>> submitTestsForExecution(List<Runnable> tests) {
    return tests.stream()
        .map(executorService::submit)
        .collect(Collectors.toList());
  }

  private boolean executeAllTestsAndReturnTrueIfAllPassed(List<Future<?>> submittedTests) {
    return submittedTests.stream()
        .allMatch(this::executeTestAndReturnTrueIfPassed);
  }

  private boolean executeTestAndReturnTrueIfPassed(Future<?> submittedTest) {
    return Try.of(() -> submittedTest.get(BazelBspServerTestData.TEST_EXECUTION_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES))
        .onFailure(e -> logger.error("Test execution failed! Exception: { }", e))
        .map(i -> true)
        .getOrElse(false);
  }

  private void exitProgramWithSuccessIfAllTestPassed(boolean didAllTestsPass) {
    int successExitCode = 0;
    int failExitCode = 1;

    if (didAllTestsPass) {
      logger.info("All test passed - exiting with success");
      System.exit(successExitCode);
    }

    logger.fatal("Test failed - exiting with fail");
    System.exit(failExitCode);
  }
}
