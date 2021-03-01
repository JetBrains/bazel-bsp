package org.jetbrains.bsp.bazel;

import ch.epfl.scala.bsp.testkit.client.TestClient;
import ch.epfl.scala.bsp.testkit.client.TestClient$;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BazelBspServerTest {

  private static final Logger LOGGER = LogManager.getLogger(BazelBspServerTest.class);

  private static final Integer SUCCESS_EXIT_CODE = 0;
  private static final Integer FAIL_EXIT_CODE = 1;

  private final ExecutorService executorService = Executors.newCachedThreadPool();

  public BazelBspServerTest() {
    LOGGER.info("Creating TestClient...");

    LOGGER.info("Created TestClient");
  }

  @SafeVarargs
  static <T> Stream<T> concat(Stream<T>... streams) {
    return Stream.of(streams).reduce(Stream::concat).orElseGet(Stream::empty);
  }

  public void run() {
    LOGGER.info("Running BazelBspServerTest...");

    List<BazelBspServerSingleTest> testsToRun =
        concat(
                getSampleRepoTests().stream(),
                getActionGraphV1Tests().stream(),
                getActionGraphV2Tests().stream())
            .collect(Collectors.toList());
    runTests(testsToRun);
  }

  private List<BazelBspServerSingleTest> getActionGraphV1Tests() {
    TestClient client =
        TestClient$.MODULE$.testInitialStructure(
            BazelBspServerTestData.SAMPLE_REPO_FULL_PATH,
            ImmutableMap.of(),
            BazelBspServerTestData.TEST_CLIENT_TIMEOUT_IN_MINUTES);

    return ImmutableList.of(
        new BazelBspServerSingleTest(
            "actiong-graph-v1 javacopts test",
            () ->
                client.testJavacOptions(
                    BazelBspServerTestData.JAVAC_OPTIONS_PARAMS,
                    BazelBspServerTestData.EXPECTED_JAVAC_OPTIONS)),
        new BazelBspServerSingleTest(
            "actiong-graph-v1 scalacopts test",
            () ->
                client.testScalacOptions(
                    BazelBspServerTestData.SCALAC_OPTIONS_PARAMS,
                    BazelBspServerTestData.EXPECTED_SCALAC_OPTIONS)));
  }

  private List<BazelBspServerSingleTest> getActionGraphV2Tests() {
    TestClient client =
        TestClient$.MODULE$.testInitialStructure(
            BazelBspServerTestData.ACTION_GRAPH_V2_FULL_PATH,
            ImmutableMap.of(),
            BazelBspServerTestData.TEST_CLIENT_TIMEOUT_IN_MINUTES);

    return ImmutableList.of(
        new BazelBspServerSingleTest(
            "actiong-graph-v2 javacopts test",
            () ->
                client.testJavacOptions(
                    BazelBspServerTestData.JAVAC_OPTIONS_PARAMS_ACTION_GRAPH_V2,
                    BazelBspServerTestData.EXPECTED_JAVAC_OPTIONS_ACTION_GRAPH_V2)),
        new BazelBspServerSingleTest(
            "actiong-graph-v2 scalacopts test",
            () ->
                client.testScalacOptions(
                    BazelBspServerTestData.SCALAC_OPTIONS_PARAMS_ACTION_GRAPH_V2,
                    BazelBspServerTestData.EXPECTED_SCALAC_OPTIONS_ACTION_GRAPH_V2)));
  }

  private List<BazelBspServerSingleTest> getSampleRepoTests() {
    TestClient client =
        TestClient$.MODULE$.testInitialStructure(
            BazelBspServerTestData.SAMPLE_REPO_FULL_PATH,
            ImmutableMap.of(),
            BazelBspServerTestData.TEST_CLIENT_TIMEOUT_IN_MINUTES);

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
                    BazelBspServerTestData.EXPECTED_DEPENDENCIES)),
        new BazelBspServerSingleTest(
            "Scala main classes",
            () ->
                client.testScalaMainClasses(
                    BazelBspServerTestData.SCALA_MAIN_CLASSES_PARAMS,
                    BazelBspServerTestData.EXPECTED_SCALA_MAIN_CLASSES)),
        new BazelBspServerSingleTest(
            "Scala test classes",
            () ->
                client.testScalaTestClasses(
                    BazelBspServerTestData.SCALA_TEST_CLASSES_PARAMS,
                    BazelBspServerTestData.EXPECTED_SCALA_TEST_CLASSES))
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
    if (didAllTestsPass) {
      LOGGER.info("All test passed - exiting with success");
      System.exit(SUCCESS_EXIT_CODE);
    }

    LOGGER.fatal("Test failed - exiting with fail");
    System.exit(FAIL_EXIT_CODE);
  }
}
