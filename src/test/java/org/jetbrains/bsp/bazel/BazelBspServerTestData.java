package org.jetbrains.bsp.bazel;

import ch.epfl.scala.bsp4j.BuildTarget;
import ch.epfl.scala.bsp4j.BuildTargetCapabilities;
import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import ch.epfl.scala.bsp4j.DependencySourcesItem;
import ch.epfl.scala.bsp4j.DependencySourcesResult;
import ch.epfl.scala.bsp4j.InverseSourcesResult;
import ch.epfl.scala.bsp4j.JavacOptionsItem;
import ch.epfl.scala.bsp4j.JavacOptionsParams;
import ch.epfl.scala.bsp4j.JavacOptionsResult;
import ch.epfl.scala.bsp4j.ResourcesItem;
import ch.epfl.scala.bsp4j.ResourcesResult;
import ch.epfl.scala.bsp4j.ScalaMainClass;
import ch.epfl.scala.bsp4j.ScalaMainClassesItem;
import ch.epfl.scala.bsp4j.ScalaMainClassesParams;
import ch.epfl.scala.bsp4j.ScalaMainClassesResult;
import ch.epfl.scala.bsp4j.ScalaTestClassesItem;
import ch.epfl.scala.bsp4j.ScalaTestClassesParams;
import ch.epfl.scala.bsp4j.ScalaTestClassesResult;
import ch.epfl.scala.bsp4j.ScalacOptionsItem;
import ch.epfl.scala.bsp4j.ScalacOptionsParams;
import ch.epfl.scala.bsp4j.ScalacOptionsResult;
import ch.epfl.scala.bsp4j.SourceItem;
import ch.epfl.scala.bsp4j.SourceItemKind;
import ch.epfl.scala.bsp4j.SourcesItem;
import ch.epfl.scala.bsp4j.SourcesResult;
import ch.epfl.scala.bsp4j.TextDocumentIdentifier;
import ch.epfl.scala.bsp4j.WorkspaceBuildTargetsResult;
import com.google.common.collect.ImmutableList;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import org.jetbrains.bsp.bazel.commons.Constants;

class BazelBspServerTestData {

  private static final String SAMPLE_REPO_PATH = "sample-repo";
  private static final String SAMPLE_REPO_EXAMPLE_PATH = SAMPLE_REPO_PATH + "/example";
  private static final String SAMPLE_REPO_DEP_PATH = SAMPLE_REPO_PATH + "/dep";

  private static final String BUILD_WORKSPACE_DIRECTORY = "BUILD_WORKSPACE_DIRECTORY";
  private static final String WORKSPACE_DIR_PATH = System.getenv(BUILD_WORKSPACE_DIRECTORY);

  private static final BuildTargetIdentifier EXAMPLE_EXAMPLE_TARGET =
      new BuildTargetIdentifier("//example:example");
  private static final BuildTargetIdentifier DEP_DEP_TARGET =
      new BuildTargetIdentifier("//dep:dep");
  private static final BuildTargetIdentifier DEP_DEEPER_DEEPER_TARGET =
      new BuildTargetIdentifier("//dep/deeper:deeper");
  private static final BuildTargetIdentifier EXAMPLE_EXAMPLE_TEST_TARGET =
      new BuildTargetIdentifier("//example:example-test");
  private static final BuildTargetIdentifier WITHOUT_MAIN_CLASS_LIBRARY_TARGET =
      new BuildTargetIdentifier("//target_without_main_class:library");
  private static final BuildTargetIdentifier WITHOUT_ARGS_BINARY_TARGET =
      new BuildTargetIdentifier("//target_without_args:binary");
  private static final BuildTargetIdentifier WITHOUT_JVM_FLAGS_BINARY_TARGET =
      new BuildTargetIdentifier("//target_without_jvm_flags:binary");
  private static final BuildTargetIdentifier DEP_JAVA_DEP_TARGET =
      new BuildTargetIdentifier("//dep:java-dep");

  static final Duration TEST_CLIENT_TIMEOUT_IN_MINUTES = Duration.ofMinutes(6);
  static final Integer TEST_EXECUTION_TIMEOUT_IN_MINUTES = 25;

  static final String WORKSPACE_FULL_PATH = WORKSPACE_DIR_PATH + "/" + SAMPLE_REPO_PATH;

  static final WorkspaceBuildTargetsResult EXPECTED_BUILD_TARGETS =
      new WorkspaceBuildTargetsResult(
          ImmutableList.of(
              new BuildTarget(
                  EXAMPLE_EXAMPLE_TARGET,
                  ImmutableList.of(),
                  ImmutableList.of(Constants.SCALA),
                  ImmutableList.of(DEP_DEP_TARGET),
                  new BuildTargetCapabilities(true, false, true)),
              new BuildTarget(
                  DEP_DEP_TARGET,
                  ImmutableList.of(),
                  ImmutableList.of(Constants.JAVA, Constants.SCALA),
                  ImmutableList.of(DEP_DEEPER_DEEPER_TARGET),
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
              new DependencySourcesItem(EXAMPLE_EXAMPLE_TARGET, DEPENDENCIES),
              new DependencySourcesItem(DEP_DEP_TARGET, DEPENDENCIES)));

  static final SourcesResult EXPECTED_SOURCES =
      new SourcesResult(
          ImmutableList.of(
              new SourcesItem(
                  EXAMPLE_EXAMPLE_TARGET,
                  ImmutableList.of(
                      new SourceItem(
                          SAMPLE_REPO_EXAMPLE_PATH + "/Example.scala",
                          SourceItemKind.FILE,
                          false))),
              new SourcesItem(
                  DEP_DEP_TARGET,
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
                  EXAMPLE_EXAMPLE_TARGET,
                  ImmutableList.of(
                      SAMPLE_REPO_EXAMPLE_PATH + "/file.txt",
                      SAMPLE_REPO_EXAMPLE_PATH + "/file2.txt"))));

  static final TextDocumentIdentifier INVERSE_SOURCES_DOCUMENT =
      new TextDocumentIdentifier("file://" + WORKSPACE_FULL_PATH + "/dep/Dep.scala");

  static final InverseSourcesResult EXPECTED_INVERSE_SOURCES =
      new InverseSourcesResult(ImmutableList.of(DEP_DEP_TARGET));

  static final ScalaMainClassesParams SCALA_MAIN_CLASSES_PARAMS =
      new ScalaMainClassesParams(
          ImmutableList.of(
              EXAMPLE_EXAMPLE_TARGET,
              WITHOUT_MAIN_CLASS_LIBRARY_TARGET,
              WITHOUT_ARGS_BINARY_TARGET,
              WITHOUT_JVM_FLAGS_BINARY_TARGET));

  static final ScalaMainClassesResult EXPECTED_SCALA_MAIN_CLASSES =
      new ScalaMainClassesResult(
          ImmutableList.of(
              new ScalaMainClassesItem(
                  EXAMPLE_EXAMPLE_TARGET,
                  Collections.singletonList(
                      new ScalaMainClass(
                          "example.Example",
                          ImmutableList.of("arg1", "arg2"),
                          ImmutableList.of("-Xms2G -Xmx5G")))),
              new ScalaMainClassesItem(
                  WITHOUT_ARGS_BINARY_TARGET,
                  Collections.singletonList(
                      new ScalaMainClass(
                          "example.Example",
                          ImmutableList.of(),
                          ImmutableList.of("-Xms2G -Xmx5G")))),
              new ScalaMainClassesItem(
                  WITHOUT_JVM_FLAGS_BINARY_TARGET,
                  Collections.singletonList(
                      new ScalaMainClass(
                          "example.Example",
                          ImmutableList.of("arg1", "arg2"),
                          ImmutableList.of())))));

  static final ScalaTestClassesParams SCALA_TEST_CLASSES_PARAMS =
      new ScalaTestClassesParams(
          ImmutableList.of(EXAMPLE_EXAMPLE_TARGET, EXAMPLE_EXAMPLE_TEST_TARGET));

  static final ScalaTestClassesResult EXPECTED_SCALA_TEST_CLASSES =
      new ScalaTestClassesResult(
          ImmutableList.of(
              new ScalaTestClassesItem(
                  EXAMPLE_EXAMPLE_TEST_TARGET, ImmutableList.of("example.ExampleTest"))));

  static final JavacOptionsParams JAVAC_OPTIONS_PARAMS =
      new JavacOptionsParams(ImmutableList.of(EXAMPLE_EXAMPLE_TARGET, DEP_JAVA_DEP_TARGET));

  static final JavacOptionsResult EXPECTED_JAVAC_OPTIONS =
      new JavacOptionsResult(
          ImmutableList.of(
              new JavacOptionsItem(
                  EXAMPLE_EXAMPLE_TARGET, ImmutableList.of(), ImmutableList.of(), ""),
              new JavacOptionsItem(
                  DEP_JAVA_DEP_TARGET,
                  ImmutableList.of("-Werror", "-Xlint:all"),
                  ImmutableList.of(),
                  "")));

  static final ScalacOptionsParams SCALAC_OPTIONS_PARAMS =
      new ScalacOptionsParams(ImmutableList.of(EXAMPLE_EXAMPLE_TARGET, DEP_DEP_TARGET));

  static final ScalacOptionsResult EXPECTED_SCALAC_OPTIONS =
      new ScalacOptionsResult(
          ImmutableList.of(
              new ScalacOptionsItem(
                  EXAMPLE_EXAMPLE_TARGET,
                  ImmutableList.of("-target:jvm-1.8"),
                  ImmutableList.of(
                      "__main__/external/io_bazel_rules_scala_scala_library/scala-library-2.12.8.jar",
                      "__main__/external/io_bazel_rules_scala_scala_reflect/scala-reflect-2.12.8.jar",
                      "/bin/external/io_bazel_rules_scala/src/java/io/bazel/rulesscala/scalac/scalac.jar",
                      "/bin/dep/deeper/deeper.jar",
                      "/bin/dep/dep.jar"),
                  "bin/example/"),
              new ScalacOptionsItem(
                  DEP_DEP_TARGET,
                  ImmutableList.of(),
                  ImmutableList.of(
                      "__main__/external/io_bazel_rules_scala_scala_library/scala-library-2.12.8.jar",
                      "__main__/external/io_bazel_rules_scala_scala_reflect/scala-reflect-2.12.8.jar",
                      "bin/external/io_bazel_rules_scala/src/java/io/bazel/rulesscala/scalac/scalac.jar",
                      "bin/dep/deeper/deeper.jar"),
                  "bin/dep/")));
}
