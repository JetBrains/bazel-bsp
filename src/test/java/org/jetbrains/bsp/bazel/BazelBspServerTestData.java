package org.jetbrains.bsp.bazel;

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
import com.google.common.collect.ImmutableList;
import java.time.Duration;
import java.util.List;
import org.jetbrains.bsp.bazel.common.Constants;

class BazelBspServerTestData {

  private static final String SAMPLE_REPO_PATH = "sample-repo";
  private static final String SAMPLE_REPO_EXAMPLE_PATH = SAMPLE_REPO_PATH + "/example";
  private static final String SAMPLE_REPO_DEP_PATH = SAMPLE_REPO_PATH + "/dep";

  private static final String BUILD_WORKSPACE_DIRECTORY = "BUILD_WORKSPACE_DIRECTORY";
  private static final String WORKSPACE_DIR_PATH = System.getenv(BUILD_WORKSPACE_DIRECTORY);

  private static final BuildTargetIdentifier ID_1 = new BuildTargetIdentifier("//example:example");
  private static final BuildTargetIdentifier ID_2 = new BuildTargetIdentifier("//dep:dep");
  private static final BuildTargetIdentifier ID_3 =
      new BuildTargetIdentifier("//dep/deeper:deeper");

  static final Duration TEST_CLIENT_TIMEOUT_IN_MINUTES = Duration.ofMinutes(4);
  static final Integer TEST_EXECUTION_TIMEOUT_IN_MINUTES = 15;

  static final String WORKSPACE_FULL_PATH = WORKSPACE_DIR_PATH + "/" + SAMPLE_REPO_PATH;

  static final WorkspaceBuildTargetsResult EXPECTED_BUILD_TARGETS =
      new WorkspaceBuildTargetsResult(
          ImmutableList.of(
              new BuildTarget(
                  ID_1,
                  ImmutableList.of(),
                  ImmutableList.of(Constants.SCALA),
                  ImmutableList.of(ID_2),
                  new BuildTargetCapabilities(true, false, true)),
              new BuildTarget(
                  ID_2,
                  ImmutableList.of(),
                  ImmutableList.of(Constants.JAVA, Constants.SCALA),
                  ImmutableList.of(ID_3),
                  new BuildTargetCapabilities(true, false, false))));

  static final List<String> DEPENDENCIES =
      ImmutableList.of(
          "https/repo1.maven.org/maven2/com/google/j2objc/j2objc-annotations/1.3/j2objc-annotations-1.3-sources.jar",
          "https/repo1.maven.org/maven2/com/google/code/findbugs/jsr305/3.0.2/jsr305-3.0.2-sources.jar",
          "https/repo1.maven.org/maven2/com/google/guava/failureaccess/1.0.1/failureaccess-1.0.1-sources.jar",
          "https/repo1.maven.org/maven2/com/google/errorprone/error_prone_annotations/2.3.2/error_prone_annotations-2.3.2-sources.jar",
          "https/repo1.maven.org/maven2/org/codehaus/mojo/animal-sniffer-annotations/1.17/animal-sniffer-annotations-1.17-sources.jar",
          "https/repo1.maven.org/maven2/org/checkerframework/checker-qual/2.8.1/checker-qual-2.8.1-sources.jar",
          "https/repo1.maven.org/maven2/com/google/guava/guava/28.0-jre/guava-28.0-jre-sources.jar");

  static final DependencySourcesResult EXPECTED_DEPENDENCIES =
      new DependencySourcesResult(
          ImmutableList.of(
              new DependencySourcesItem(ID_1, DEPENDENCIES),
              new DependencySourcesItem(ID_2, DEPENDENCIES)));

  static final SourcesResult EXPECTED_SOURCES =
      new SourcesResult(
          ImmutableList.of(
              new SourcesItem(
                  ID_1,
                  ImmutableList.of(
                      new SourceItem(
                          SAMPLE_REPO_EXAMPLE_PATH + "/Example.scala",
                          SourceItemKind.FILE,
                          false))),
              new SourcesItem(
                  ID_2,
                  ImmutableList.of(
                      new SourceItem(
                          SAMPLE_REPO_DEP_PATH + "/Test.scala", SourceItemKind.FILE, false),
                      new SourceItem(
                          SAMPLE_REPO_DEP_PATH + "/JavaTest.java", SourceItemKind.FILE, false),
                      new SourceItem(
                          SAMPLE_REPO_DEP_PATH + "/Dep.scala", SourceItemKind.FILE, false)))));

  static final ResourcesResult EXPECTED_RESOURCES =
      new ResourcesResult(
          ImmutableList.of(
              new ResourcesItem(
                  ID_1,
                  ImmutableList.of(
                      SAMPLE_REPO_EXAMPLE_PATH + "/file.txt",
                      SAMPLE_REPO_EXAMPLE_PATH + "/file2.txt"))));

  static final TextDocumentIdentifier INVERSE_SOURCES_DOCUMENT =
      new TextDocumentIdentifier("file://" + WORKSPACE_FULL_PATH + "/dep/Dep.scala");

  static final InverseSourcesResult EXPECTED_INVERSE_SOURCES =
      new InverseSourcesResult(ImmutableList.of(ID_2));
}
