package org.jetbrains.bsp.bazel;

import ch.epfl.scala.bsp.testkit.client.TestClient;
import ch.epfl.scala.bsp.testkit.client.TestClient$;
import ch.epfl.scala.bsp4j.BuildTarget;
import ch.epfl.scala.bsp4j.BuildTargetCapabilities;
import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import ch.epfl.scala.bsp4j.DependencySourcesItem;
import ch.epfl.scala.bsp4j.DependencySourcesResult;
import ch.epfl.scala.bsp4j.InverseSourcesResult;
import ch.epfl.scala.bsp4j.ResourcesItem;
import ch.epfl.scala.bsp4j.ResourcesResult;
import ch.epfl.scala.bsp4j.SourceItem;
import ch.epfl.scala.bsp4j.SourceItemKind;
import ch.epfl.scala.bsp4j.SourcesItem;
import ch.epfl.scala.bsp4j.SourcesResult;
import ch.epfl.scala.bsp4j.TextDocumentIdentifier;
import ch.epfl.scala.bsp4j.WorkspaceBuildTargetsResult;
import com.google.common.collect.Lists;
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
import scala.concurrent.ExecutionContext;

public class BazelBspServerTest {
  private final String outDirectory = "bazel-out";
  private final String workspace;
  private final TestClient client;
  private final ExecutorService executorService = Executors.newCachedThreadPool();
  private final ExecutionContext context = ExecutionContext.fromExecutor(executorService);

  private final BuildTargetIdentifier id1 = new BuildTargetIdentifier("//example:example");
  private final BuildTargetIdentifier id2 = new BuildTargetIdentifier("//dep:dep");
  private final BuildTargetIdentifier id3 = new BuildTargetIdentifier("//dep/deeper:deeper");
  private final WorkspaceBuildTargetsResult expectedBuildTargets =
      new WorkspaceBuildTargetsResult(
          Lists.newArrayList(
              new BuildTarget(
                  id1,
                  Lists.newArrayList(),
                  Lists.newArrayList("scala"),
                  Lists.newArrayList(id2),
                  new BuildTargetCapabilities(true, false, true)),
              new BuildTarget(
                  id2,
                  Lists.newArrayList(),
                  Lists.newArrayList("java", "scala"),
                  Lists.newArrayList(id3),
                  new BuildTargetCapabilities(true, false, false))));
  private final List<String> dependencies =
      Lists.newArrayList(
          "https/repo1.maven.org/maven2/com/google/j2objc/j2objc-annotations/1.3/j2objc-annotations-1.3-sources.jar",
          "https/repo1.maven.org/maven2/com/google/code/findbugs/jsr305/3.0.2/jsr305-3.0.2-sources.jar",
          "https/repo1.maven.org/maven2/com/google/guava/failureaccess/1.0.1/failureaccess-1.0.1-sources.jar",
          "https/repo1.maven.org/maven2/com/google/errorprone/error_prone_annotations/2.3.2/error_prone_annotations-2.3.2-sources.jar",
          "https/repo1.maven.org/maven2/org/codehaus/mojo/animal-sniffer-annotations/1.17/animal-sniffer-annotations-1.17-sources.jar",
          "https/repo1.maven.org/maven2/org/checkerframework/checker-qual/2.8.1/checker-qual-2.8.1-sources.jar",
          "https/repo1.maven.org/maven2/com/google/guava/guava/28.0-jre/guava-28.0-jre-sources.jar");
  private final DependencySourcesResult expectedDependencies =
      new DependencySourcesResult(
          Lists.newArrayList(
              new DependencySourcesItem(id1, dependencies),
              new DependencySourcesItem(id2, dependencies)));
  private final SourcesResult expectedSources =
      new SourcesResult(
          Lists.newArrayList(
              new SourcesItem(
                  id1,
                  Lists.newArrayList(
                      new SourceItem(
                          "sample-repo/example/Example.scala", SourceItemKind.FILE, false))),
              new SourcesItem(
                  id2,
                  Lists.newArrayList(
                      new SourceItem("sample-repo/dep/Test.scala", SourceItemKind.FILE, false),
                      new SourceItem("sample-repo/dep/JavaTest.java", SourceItemKind.FILE, false),
                      new SourceItem("sample-repo/dep/Dep.scala", SourceItemKind.FILE, false)))));

  private final ResourcesResult expectedResources =
      new ResourcesResult(
          Lists.newArrayList(
              new ResourcesItem(
                  id1,
                  Lists.newArrayList(
                      "sample-repo/example/file.txt", "sample-repo/example/file2.txt"))));

  private final InverseSourcesResult expectedInverseSources =
      new InverseSourcesResult(Lists.newArrayList(id2));

  public BazelBspServerTest(String workspace) {
    this.workspace = workspace;
    this.client =
        TestClient$.MODULE$.testInitialStructure(workspace, new HashMap<>(), Duration.ofMinutes(4));

    Runnable[] tests = {
      client::testResolveProject,
      () -> client.testCompareWorkspaceTargetsResults(expectedBuildTargets),
      () -> client.testSourcesResults(expectedBuildTargets, expectedSources),
      () -> client.testResourcesResults(expectedBuildTargets, expectedResources),
      () ->
          client.testInverseSourcesResults(
              new TextDocumentIdentifier("file://" + workspace + "/dep/Dep.scala"),
              expectedInverseSources),
      () -> client.testDependencySourcesResults(expectedBuildTargets, expectedDependencies),
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
