package org.jetbrains.bsp.bazel.base;

import ch.epfl.scala.bsp4j.BuildClientCapabilities;
import ch.epfl.scala.bsp4j.InitializeBuildParams;
import java.nio.file.Path;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.bsp.testkit.client.TestClient;

public abstract class BazelBspTestBaseScenario {

  private static final String BSP_VERSION = "2.0.0";

  private static final Logger LOGGER = LogManager.getLogger(BazelBspTestBaseScenario.class);

  private static final Integer SUCCESS_EXIT_CODE = 0;
  private static final Integer FAIL_EXIT_CODE = 1;

  private static final String TEST_RESOURCES_DIR = "e2e/test-resources";
  private static final String WORKSPACE_DIR = System.getenv("BUILD_WORKSPACE_DIRECTORY");

  protected final TestClient testClient;

  /** Used for importing *this* workspace */
  public BazelBspTestBaseScenario() {
    var workspacePath = Path.of(WORKSPACE_DIR);

    this.testClient = createClient(workspacePath);
  }

  public BazelBspTestBaseScenario(String repoName) {
    var workspacePath = getTestingRepoPath(repoName);

    this.testClient = createClient(workspacePath);
  }

  protected Path getTestingRepoPath(String repoName) {
    return Path.of(WORKSPACE_DIR, TEST_RESOURCES_DIR, repoName);
  }

  private TestClient createClient(Path workspacePath) {
    LOGGER.info("Workspace path: {}", workspacePath);

    LOGGER.info("Creating TestClient...");
    // TODO: capabilities should be configurable
    var capabilities = new BuildClientCapabilities(List.of("java", "scala", "kotlin", "cpp"));
    var initializeBuildParams =
        new InitializeBuildParams(
            "BspTestClient", "1.0.0", BSP_VERSION, workspacePath.toString(), capabilities);
    var client = new TestClient(workspacePath, initializeBuildParams);
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
    return getScenarioSteps().stream()
        .map(BazelBspTestScenarioStep::executeAndReturnResult)
        .collect(java.util.stream.Collectors.toList())
        .stream()
        .allMatch(x -> x);
  }

  protected abstract List<BazelBspTestScenarioStep> getScenarioSteps();
}
