package org.jetbrains.bsp.bazel;

import ch.epfl.scala.bsp.testkit.client.TestClient;
import ch.epfl.scala.bsp.testkit.client.TestClient$;
import ch.epfl.scala.bsp4j.TextDocumentIdentifier;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class BazelBspServerTest {

  private final String workspace;
  private final TestClient client;
  private final ExecutorService executorService = Executors.newCachedThreadPool();


  public BazelBspServerTest(String workspace) {
    this.workspace = workspace;
    this.client =
        TestClient$.MODULE$.testInitialStructure(workspace, new HashMap<>(), Duration.ofMinutes(4));

    Runnable[] tests = {
      client::testResolveProject,
      () -> client.testCompareWorkspaceTargetsResults(BazelBspServerTestData.EXPECTED_BUILD_TARGETS),
      () -> client.testSourcesResults(BazelBspServerTestData.EXPECTED_BUILD_TARGETS, BazelBspServerTestData.EXPECTED_SOURCES),
      () -> client.testResourcesResults(BazelBspServerTestData.EXPECTED_BUILD_TARGETS, BazelBspServerTestData.EXPECTED_RESOURCES),
      () ->
          client.testInverseSourcesResults(
              new TextDocumentIdentifier("file://" + workspace + "/dep/Dep.scala"),
              BazelBspServerTestData.EXPECTED_INVERSE_SOURCES),
      () -> client.testDependencySourcesResults(BazelBspServerTestData.EXPECTED_BUILD_TARGETS, BazelBspServerTestData.EXPECTED_DEPENDENCIES),
//       client::testTargetsRunUnsuccessfully,
//       client::testTargetsTestUnsuccessfully,
//       client::testTargetCapabilities,
    };
    runTests(tests);
  }

  public static void main(String[] args) {
    new BazelBspServerTest(System.getenv("BUILD_WORKSPACE_DIRECTORY") + "/sample-repo");
  }

  private void runTests(Runnable[] tests) {
    List<Future<?>> futures =
        Arrays.stream(tests).map(executorService::submit).collect(Collectors.toList());
    boolean failed = false;
    for (Future<?> future : futures) {
      try {
        future.get(15, TimeUnit.MINUTES);
      } catch (InterruptedException | TimeoutException e) {
        System.err.println("Something wrong happened while running the test");
        failed = true;
      } catch (ExecutionException e) {
        System.err.println(e.getMessage());
        failed = true;
      }
    }

    System.exit(failed ? 1 : 0);
  }
}
