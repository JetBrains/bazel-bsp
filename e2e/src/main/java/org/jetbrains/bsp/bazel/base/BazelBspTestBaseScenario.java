package org.jetbrains.bsp.bazel.base;

import ch.epfl.scala.bsp.testkit.client.TestClient;
import ch.epfl.scala.bsp.testkit.client.TestClient$;
import com.google.common.collect.ImmutableMap;
import java.time.Duration;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class BazelBspTestBaseScenario {

  private static final Logger LOGGER = LogManager.getLogger(BazelBspTestBaseScenario.class);

  private static final Integer SUCCESS_EXIT_CODE = 0;
  private static final Integer FAIL_EXIT_CODE = 1;

  private static final String TEST_RESOURCES_DIR = "e2e/test-resources";
  private static final String WORKSPACE_DIR = System.getenv("BUILD_WORKSPACE_DIRECTORY");

  protected final TestClient testClient;

  public BazelBspTestBaseScenario(Duration clientTimeout) {
    this.testClient = createClient(WORKSPACE_DIR, clientTimeout);
  }

  public BazelBspTestBaseScenario(String repoName, Duration clientTimeout) {
    String workspacePath = getTestingRepoPath(repoName);

    this.testClient = createClient(workspacePath, clientTimeout);
  }

  protected String getTestingRepoPath(String repoName) {
    return String.format("%s/%s/%s", WORKSPACE_DIR, TEST_RESOURCES_DIR, repoName);
  }

  private TestClient createClient(String workspacePath, Duration clientTimeout) {
    LOGGER.info("Workspace path: {}", workspacePath);

    LOGGER.info("Creating TestClient...");
    TestClient client =
        TestClient$.MODULE$.testInitialStructure(workspacePath, ImmutableMap.of(), clientTimeout);
    LOGGER.info("Created TestClient.");

    return client;
  }

  public void executeScenario() {
    LOGGER.info("Running scenario...");
    boolean scenarioStepsExecutionResult = executeScenarioSteps();
    LOGGER.info("Running scenario done.");

    if (scenarioStepsExecutionResult) {
      LOGGER.info("Test passed!");
      System.exit(SUCCESS_EXIT_CODE);
    }

    LOGGER.fatal("Test failed :(");
    System.exit(FAIL_EXIT_CODE);
  }

  private boolean executeScenarioSteps() {
    return getScenarioSteps().stream().allMatch(BazelBspTestScenarioStep::executeAndReturnResult);
  }

  protected abstract List<BazelBspTestScenarioStep> getScenarioSteps();
}
