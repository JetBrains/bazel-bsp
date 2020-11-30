package org.jetbrains.bsp.bazel;

import ch.epfl.scala.bsp.testkit.client.TestClient;
import ch.epfl.scala.bsp.testkit.client.TestClient$;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BazelBspServerTest {

  private static final Logger LOGGER = LogManager.getLogger(BazelBspServerTest.class);

  private final TestClient client;
  private final ExecutorService executorService = Executors.newCachedThreadPool();

  public BazelBspServerTest() {
    LOGGER.info("Creating TestClient...");

    this.client =
        TestClient$.MODULE$.testInitialStructure(
            BazelBspServerTestData.WORKSPACE_FULL_PATH,
            ImmutableMap.of(),
            BazelBspServerTestData.TEST_CLIENT_TIMEOUT_IN_MINUTES);

    LOGGER.info("Created TestClient");
  }

  public void run() {
    LOGGER.info("Running BazelBspServerTest...");

    List<BazelBspServerSingleTest> testsTopRun = getTestsTopRun();
    runTests(testsTopRun);
  }

  private List<BazelBspServerSingleTest> getTestsTopRun() {
    return ImmutableList.of(
        new BazelBspServerSingleTest("resolve project", client::testResolveProject),
        new BazelBspServerSingleTest(
            "compare workspace targets results",
            () ->
                client.testCompareWorkspaceTargetsResults(
                    BazelBspServerTestData.EXPECTED_BUILD_TARGETS)),
        new BazelBspServerSingleTest(
            "sources results",
            () ->
                client.testSourcesResults(
                    BazelBspServerTestData.EXPECTED_BUILD_TARGETS,
                    BazelBspServerTestData.EXPECTED_SOURCES)),
        new BazelBspServerSingleTest(
            "resources results",
            () ->
                client.testResourcesResults(
                    BazelBspServerTestData.EXPECTED_BUILD_TARGETS,
                    BazelBspServerTestData.EXPECTED_RESOURCES)),
        new BazelBspServerSingleTest(
            "inverse sources results",
            () ->
                client.testInverseSourcesResults(
                    BazelBspServerTestData.INVERSE_SOURCES_DOCUMENT,
                    BazelBspServerTestData.EXPECTED_INVERSE_SOURCES)),
        new BazelBspServerSingleTest(
            "dependency sources results",
            () ->
                client.testDependencySourcesResults(
                    BazelBspServerTestData.EXPECTED_BUILD_TARGETS,
                    BazelBspServerTestData.EXPECTED_DEPENDENCIES))
        //         TODO one day we will uncomment them...
        //        new BazelBspServerSingleTest(
        //            "targets run unsuccessfully",
        //            client::testTargetsRunUnsuccessfully),
        //        new BazelBspServerSingleTest(
        //            "targets test unsuccessfully",
        //            client::testTargetsTestUnsuccessfully),
        //            new BazelBspServerSingleTest(
        //                "target capabilities",
        //                client::testTargetCapabilities)
        );
  }

  private void runTests(List<BazelBspServerSingleTest> testsToRun) {
    List<BazelBspServerSingleTest> submittedTests = submitTestsForExecution(testsToRun);
    boolean didAllTestsPass = executeAllTestsAndReturnTrueIfAllPassed(submittedTests);

    exitProgramWithSuccessIfAllTestPassed(didAllTestsPass);
  }

  private List<BazelBspServerSingleTest> submitTestsForExecution(
      List<BazelBspServerSingleTest> testsToSubmit) {
    LOGGER.info("Submitting tests for execution...");

    return testsToSubmit.stream()
        .map(test -> test.submit(executorService))
        .collect(Collectors.toList());
  }

  private boolean executeAllTestsAndReturnTrueIfAllPassed(
      List<BazelBspServerSingleTest> submittedTests) {
    LOGGER.info("Executing tests...");

    return submittedTests.stream()
        .allMatch(
            test ->
                test.executeTestWithTimeoutAndReturnTrueIfPassed(
                    BazelBspServerTestData.TEST_EXECUTION_TIMEOUT_IN_MINUTES));
  }

  private void exitProgramWithSuccessIfAllTestPassed(boolean didAllTestsPass) {
    int successExitCode = 0;
    int failExitCode = 1;

    if (didAllTestsPass) {
      LOGGER.info("All test passed - exiting with success");
      System.exit(successExitCode);
    }

    LOGGER.fatal("Test failed - exiting with fail");
    System.exit(failExitCode);
  }
}
