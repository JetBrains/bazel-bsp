package org.jetbrains.bsp.bazel.projectview.parser;

import static org.assertj.core.api.Assertions.assertThat;

import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import com.google.common.net.HostAndPort;
import io.vavr.collection.List;
import io.vavr.control.Option;
import java.io.IOException;
import java.nio.file.Paths;
import org.jetbrains.bsp.bazel.projectview.model.ProjectView;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBazelPathSection;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewDebuggerAddressSection;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewJavaPathSection;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewTargetsSection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class ProjectViewParserImplTest {

  private ProjectViewParser parser;

  @BeforeEach
  public void before() {
    // given
    this.parser = new ProjectViewParserMockTestImpl();
  }

  @Nested
  @DisplayName("ProjectView parse(projectViewFilePath) tests")
  class ParseProjectViewFilePathTest {

    @Test
    public void shouldReturnFailureForNotExistingFile() {
      // given
      var projectViewFilePath = Paths.get("/does/not/exist.bazelproject");

      // when
      var projectViewTry = parser.parse(projectViewFilePath);

      // then
      assertThat(projectViewTry.isFailure()).isTrue();
      assertThat(projectViewTry.getCause().getClass()).isEqualTo(IOException.class);
      assertThat(projectViewTry.getCause().getMessage())
          .isEqualTo("/does/not/exist.bazelproject file does not exist!");
    }

    @Test
    public void shouldReturnFailureForNotExistingImportedFile() {
      // given
      var projectViewFilePath = Paths.get("/projectview/file9ImportsNotExisting.bazelproject");

      // when
      var projectViewTry = parser.parse(projectViewFilePath);

      // then
      assertThat(projectViewTry.isFailure()).isTrue();
      assertThat(projectViewTry.getCause().getClass()).isEqualTo(IOException.class);
      assertThat(projectViewTry.getCause().getMessage())
          .isEqualTo("/projectview/does/not/exist.bazelproject file does not exist!");
    }

    @Test
    public void shouldReturnEmptyTargetsSectionForFileWithoutTargetsSection() {
      // given
      var projectViewFilePath = Paths.get("/projectview/without/targets.bazelproject");

      // when
      var projectViewTry = parser.parse(projectViewFilePath);

      // then
      assertThat(projectViewTry.isSuccess()).isTrue();
      var projectView = projectViewTry.get();

      assertThat(projectView.getTargets()).isEmpty();
    }

    @Test
    public void shouldReturnEmptyBazelPathForFileWithoutBazelPathSection() {
      // given
      var projectViewFilePath = Paths.get("/projectview/without/bazelpath.bazelproject");

      // when
      var projectViewTry = parser.parse(projectViewFilePath);

      // then
      assertThat(projectViewTry.isSuccess()).isTrue();
      var projectView = projectViewTry.get();

      assertThat(projectView.getBazelPath()).isEmpty();
    }

    @Test
    public void shouldReturnEmptyDebuggerAddressForFileWithoutDebuggerAddressSection() {
      // given
      var projectViewFilePath = Paths.get("/projectview/without/debuggeraddress.bazelproject");

      // when
      var projectViewTry = parser.parse(projectViewFilePath);

      // then
      assertThat(projectViewTry.isSuccess()).isTrue();
      var projectView = projectViewTry.get();

      assertThat(projectView.getDebuggerAddress()).isEmpty();
    }

    @Test
    public void shouldReturnEmptyJavaPathSectionForFileWithoutJavaPathSection() {
      // given
      var projectViewFilePath = Paths.get("/projectview/without/javapath.bazelproject");

      // when
      var projectViewTry = parser.parse(projectViewFilePath);

      // then
      assertThat(projectViewTry.isSuccess()).isTrue();
      var projectView = projectViewTry.get();

      assertThat(projectView.getJavaPath()).isEmpty();
    }

    @Test
    public void shouldParseEmptyFile() {
      // given
      var projectViewFilePath = Paths.get("/projectview/empty.bazelproject");

      // when
      var projectViewTry = parser.parse(projectViewFilePath);

      // then
      var expectedProjectViewTry =
          ProjectView.builder()
              .targets(Option.none())
              .bazelPath(Option.none())
              .debuggerAddress(Option.none())
              .javaPath(Option.none())
              .build();
      assertThat(projectViewTry).isEqualTo(expectedProjectViewTry);
    }

    @Test
    public void shouldParseFileWithAllSections() {
      // given
      var projectViewFilePath = Paths.get("/projectview/file1.bazelproject");

      // when
      var projectViewTry = parser.parse(projectViewFilePath);

      // then
      var expectedProjectViewTry =
          ProjectView.builder()
              .targets(
                  Option.of(
                      new ProjectViewTargetsSection(
                          List.of(
                              new BuildTargetIdentifier("//included_target1.1"),
                              new BuildTargetIdentifier("//included_target1.2")),
                          List.of(new BuildTargetIdentifier("//excluded_target1.1")))))
              .bazelPath(Option.of(new ProjectViewBazelPathSection(Paths.get("path1/to/bazel"))))
              .debuggerAddress(
                  Option.of(
                      new ProjectViewDebuggerAddressSection(
                          HostAndPort.fromString("0.0.0.1:8000"))))
              .javaPath(Option.of(new ProjectViewJavaPathSection(Paths.get("path1/to/java"))))
              .build();
      assertThat(projectViewTry).isEqualTo(expectedProjectViewTry);
    }

    @Test
    public void shouldParseFileWithSingleImportedFileWithoutSingletonValues() {
      // given
      var projectViewFilePath = Paths.get("/projectview/file4ImportsFile1.bazelproject");

      // when
      var projectViewTry = parser.parse(projectViewFilePath);

      // then
      var expectedProjectViewTry =
          ProjectView.builder()
              .targets(
                  Option.of(
                      new ProjectViewTargetsSection(
                          List.of(
                              new BuildTargetIdentifier("//included_target1.1"),
                              new BuildTargetIdentifier("//included_target1.2"),
                              new BuildTargetIdentifier("//included_target4.1")),
                          List.of(
                              new BuildTargetIdentifier("//excluded_target1.1"),
                              new BuildTargetIdentifier("//excluded_target4.1"),
                              new BuildTargetIdentifier("//excluded_target4.2")))))
              .bazelPath(Option.of(new ProjectViewBazelPathSection(Paths.get("path1/to/bazel"))))
              .debuggerAddress(
                  Option.of(
                      new ProjectViewDebuggerAddressSection(
                          HostAndPort.fromString("0.0.0.1:8000"))))
              .javaPath(Option.of(new ProjectViewJavaPathSection(Paths.get("path1/to/java"))))
              .build();
      assertThat(projectViewTry).isEqualTo(expectedProjectViewTry);
    }

    @Test
    public void shouldParseFileWithSingleImportedFileWithSingletonValues() {
      // given
      var projectViewFilePath = Paths.get("/projectview/file7ImportsFile1.bazelproject");

      // when
      var projectViewTry = parser.parse(projectViewFilePath);

      // then
      var expectedProjectViewTry =
          ProjectView.builder()
              .targets(
                  Option.of(
                      new ProjectViewTargetsSection(
                          List.of(
                              new BuildTargetIdentifier("//included_target1.1"),
                              new BuildTargetIdentifier("//included_target1.2"),
                              new BuildTargetIdentifier("//included_target7.1")),
                          List.of(
                              new BuildTargetIdentifier("//excluded_target1.1"),
                              new BuildTargetIdentifier("//excluded_target7.1"),
                              new BuildTargetIdentifier("//excluded_target7.2")))))
              .bazelPath(Option.of(new ProjectViewBazelPathSection(Paths.get("path7/to/bazel"))))
              .debuggerAddress(
                  Option.of(
                      new ProjectViewDebuggerAddressSection(
                          HostAndPort.fromString("0.0.0.7:8000"))))
              .javaPath(Option.of(new ProjectViewJavaPathSection(Paths.get("path7/to/java"))))
              .build();
      assertThat(projectViewTry).isEqualTo(expectedProjectViewTry);
    }

    @Test
    public void shouldParseFileWithEmptyImportedFile() {
      // given
      var projectViewFilePath = Paths.get("/projectview/file8ImportsEmpty.bazelproject");

      // when
      var projectViewTry = parser.parse(projectViewFilePath);

      // then
      var expectedProjectViewTry =
          ProjectView.builder()
              .targets(
                  Option.of(
                      new ProjectViewTargetsSection(
                          List.of(new BuildTargetIdentifier("//included_target8.1")),
                          List.of(
                              new BuildTargetIdentifier("//excluded_target8.1"),
                              new BuildTargetIdentifier("//excluded_target8.2")))))
              .bazelPath(Option.of(new ProjectViewBazelPathSection(Paths.get("path8/to/bazel"))))
              .debuggerAddress(
                  Option.of(
                      new ProjectViewDebuggerAddressSection(
                          HostAndPort.fromString("0.0.0.8:8000"))))
              .javaPath(Option.of(new ProjectViewJavaPathSection(Paths.get("path8/to/java"))))
              .build();
      assertThat(projectViewTry).isEqualTo(expectedProjectViewTry);
    }

    @Test
    public void shouldParseFileWithThreeImportedFiles() {
      // given
      var projectViewFilePath = Paths.get("/projectview/file5ImportsFile1File2File3.bazelproject");

      // when
      var projectViewTry = parser.parse(projectViewFilePath);

      // then
      var expectedProjectViewTry =
          ProjectView.builder()
              .targets(
                  Option.of(
                      new ProjectViewTargetsSection(
                          List.of(
                              new BuildTargetIdentifier("//included_target1.1"),
                              new BuildTargetIdentifier("//included_target1.2"),
                              new BuildTargetIdentifier("//included_target2.1"),
                              new BuildTargetIdentifier("//included_target3.1")),
                          List.of(
                              new BuildTargetIdentifier("//excluded_target1.1"),
                              new BuildTargetIdentifier("//excluded_target2.1"),
                              new BuildTargetIdentifier("//excluded_target5.1"),
                              new BuildTargetIdentifier("//excluded_target5.2")))))
              .bazelPath(Option.of(new ProjectViewBazelPathSection(Paths.get("path3/to/bazel"))))
              .debuggerAddress(
                  Option.of(
                      new ProjectViewDebuggerAddressSection(
                          HostAndPort.fromString("0.0.0.3:8000"))))
              .javaPath(Option.of(new ProjectViewJavaPathSection(Paths.get("path3/to/java"))))
              .build();
      assertThat(projectViewTry).isEqualTo(expectedProjectViewTry);
    }

    @Test
    public void shouldParseFileWithNestedImportedFiles() {
      // given
      var projectViewFilePath = Paths.get("/projectview/file6ImportsFile2File3File4.bazelproject");

      // when
      var projectViewTry = parser.parse(projectViewFilePath);

      // then
      var expectedProjectViewTry =
          ProjectView.builder()
              .targets(
                  Option.of(
                      new ProjectViewTargetsSection(
                          List.of(
                              new BuildTargetIdentifier("//included_target1.1"),
                              new BuildTargetIdentifier("//included_target1.2"),
                              new BuildTargetIdentifier("//included_target2.1"),
                              new BuildTargetIdentifier("//included_target3.1"),
                              new BuildTargetIdentifier("//included_target4.1")),
                          List.of(
                              new BuildTargetIdentifier("//excluded_target1.1"),
                              new BuildTargetIdentifier("//excluded_target2.1"),
                              new BuildTargetIdentifier("//excluded_target4.1"),
                              new BuildTargetIdentifier("//excluded_target4.2")))))
              .bazelPath(Option.of(new ProjectViewBazelPathSection(Paths.get("path1/to/bazel"))))
              .debuggerAddress(
                  Option.of(
                      new ProjectViewDebuggerAddressSection(
                          HostAndPort.fromString("0.0.0.1:8000"))))
              .javaPath(Option.of(new ProjectViewJavaPathSection(Paths.get("path1/to/java"))))
              .build();
      assertThat(projectViewTry).isEqualTo(expectedProjectViewTry);
    }
  }

  @Nested
  @DisplayName("ProjectView parse(projectViewFilePath, defaultProjectViewFilePath) tests")
  class ParseProjectViewFilePathDefaultProjectViewFilePathTest {

    @Test
    public void shouldReturnFailureForNotExistingDefaultFile() {
      // given
      var projectViewFilePath = Paths.get("/projectview/file1.bazelproject");
      var defaultProjectViewFilePath = Paths.get("/does/not/exist.bazelproject");

      // when
      var projectViewTry = parser.parse(projectViewFilePath, defaultProjectViewFilePath);

      // then
      assertThat(projectViewTry.isFailure()).isTrue();
      assertThat(projectViewTry.getCause().getClass()).isEqualTo(IOException.class);
      assertThat(projectViewTry.getCause().getMessage())
          .isEqualTo("/does/not/exist.bazelproject file does not exist!");
    }

    @Test
    public void shouldReturnFailureForNotExistingDefaultFileAndNotExistingFile() {
      // given
      var projectViewFilePath = Paths.get("/does/not/exist.bazelproject");
      var defaultProjectViewFilePath = Paths.get("/does/not/exist.bazelproject");

      // when
      var projectViewTry = parser.parse(projectViewFilePath, defaultProjectViewFilePath);

      // then
      assertThat(projectViewTry.isFailure()).isTrue();
      assertThat(projectViewTry.getCause().getClass()).isEqualTo(IOException.class);
      assertThat(projectViewTry.getCause().getMessage())
          .isEqualTo("/does/not/exist.bazelproject file does not exist!");
    }

    @Test
    public void shouldReturnFile1ForEmptyDefaultFile() {
      // given
      var projectViewFilePath = Paths.get("/projectview/file1.bazelproject");
      var defaultProjectViewFilePath = Paths.get("/projectview/empty.bazelproject");

      // when
      var projectViewTry = parser.parse(projectViewFilePath, defaultProjectViewFilePath);

      // then
      var expectedProjectViewTry =
          ProjectView.builder()
              .targets(
                  Option.of(
                      new ProjectViewTargetsSection(
                          List.of(
                              new BuildTargetIdentifier("//included_target1.1"),
                              new BuildTargetIdentifier("//included_target1.2")),
                          List.of(new BuildTargetIdentifier("//excluded_target1.1")))))
              .bazelPath(Option.of(new ProjectViewBazelPathSection(Paths.get("path1/to/bazel"))))
              .debuggerAddress(
                  Option.of(
                      new ProjectViewDebuggerAddressSection(
                          HostAndPort.fromString("0.0.0.1:8000"))))
              .javaPath(Option.of(new ProjectViewJavaPathSection(Paths.get("path1/to/java"))))
              .build();
      assertThat(projectViewTry).isEqualTo(expectedProjectViewTry);
    }

    @Test
    public void shouldReturnEmptyTargetsForDefaultFileWithoutTargetsSection() {
      // given
      var projectViewFilePath = Paths.get("/projectview/empty.bazelproject");
      var defaultProjectViewFilePath = Paths.get("/projectview/without/targets.bazelproject");

      // when
      var projectViewTry = parser.parse(projectViewFilePath, defaultProjectViewFilePath);

      // then
      assertThat(projectViewTry.isSuccess()).isTrue();
      var projectView = projectViewTry.get();

      assertThat(projectView.getTargets()).isEmpty();
    }

    @Test
    public void shouldReturnEmptyForDefaultFileWithoutBazelPathSection() {
      // given
      var projectViewFilePath = Paths.get("/projectview/empty.bazelproject");
      var defaultProjectViewFilePath = Paths.get("/projectview/without/bazelpath.bazelproject");

      // when
      var projectViewTry = parser.parse(projectViewFilePath, defaultProjectViewFilePath);

      // then
      assertThat(projectViewTry.isSuccess()).isTrue();
      var projectView = projectViewTry.get();

      assertThat(projectView.getBazelPath()).isEmpty();
    }

    @Test
    public void shouldReturnEmptyForDefaultFileWithoutDebuggerAddressSection() {
      // given
      var projectViewFilePath = Paths.get("/projectview/empty.bazelproject");
      var defaultProjectViewFilePath =
          Paths.get("/projectview/without/debuggeraddress.bazelproject");

      // when
      var projectViewTry = parser.parse(projectViewFilePath, defaultProjectViewFilePath);

      // then
      assertThat(projectViewTry.isSuccess()).isTrue();
      var projectView = projectViewTry.get();

      assertThat(projectView.getDebuggerAddress()).isEmpty();
    }

    @Test
    public void shouldReturnEmptyForDefaultFileWithoutJavaPathSection() {
      // given
      var projectViewFilePath = Paths.get("/projectview/empty.bazelproject");
      var defaultProjectViewFilePath = Paths.get("/projectview/without/javapath.bazelproject");

      // when
      var projectViewTry = parser.parse(projectViewFilePath, defaultProjectViewFilePath);

      // then
      assertThat(projectViewTry.isSuccess()).isTrue();
      var projectView = projectViewTry.get();

      assertThat(projectView.getJavaPath()).isEmpty();
    }

    @Test
    public void shouldParseFileAndSkipDefaults() {
      // given
      var projectViewFilePath = Paths.get("/projectview/file1.bazelproject");
      var defaultProjectViewFilePath = Paths.get("/projectview/file2.bazelproject");

      // when
      var projectViewTry = parser.parse(projectViewFilePath, defaultProjectViewFilePath);

      // then
      var expectedProjectViewTry =
          ProjectView.builder()
              .targets(
                  Option.of(
                      new ProjectViewTargetsSection(
                          List.of(
                              new BuildTargetIdentifier("//included_target1.1"),
                              new BuildTargetIdentifier("//included_target1.2")),
                          List.of(new BuildTargetIdentifier("//excluded_target1.1")))))
              .bazelPath(Option.of(new ProjectViewBazelPathSection(Paths.get("path1/to/bazel"))))
              .debuggerAddress(
                  Option.of(
                      new ProjectViewDebuggerAddressSection(
                          HostAndPort.fromString("0.0.0.1:8000"))))
              .javaPath(Option.of(new ProjectViewJavaPathSection(Paths.get("path1/to/java"))))
              .build();
      assertThat(projectViewTry).isEqualTo(expectedProjectViewTry);
    }

    @Test
    public void shouldParseDefaultsForNotExistingFile() {
      // given
      var projectViewFilePath = Paths.get("/doesnt/exist.bazelproject");
      var defaultProjectViewFilePath = Paths.get("/projectview/file1.bazelproject");

      // when
      var projectViewTry = parser.parse(projectViewFilePath, defaultProjectViewFilePath);

      // then
      var expectedProjectViewTry =
          ProjectView.builder()
              .targets(
                  Option.of(
                      new ProjectViewTargetsSection(
                          List.of(
                              new BuildTargetIdentifier("//included_target1.1"),
                              new BuildTargetIdentifier("//included_target1.2")),
                          List.of(new BuildTargetIdentifier("//excluded_target1.1")))))
              .bazelPath(Option.of(new ProjectViewBazelPathSection(Paths.get("path1/to/bazel"))))
              .debuggerAddress(
                  Option.of(
                      new ProjectViewDebuggerAddressSection(
                          HostAndPort.fromString("0.0.0.1:8000"))))
              .javaPath(Option.of(new ProjectViewJavaPathSection(Paths.get("path1/to/java"))))
              .build();
      assertThat(projectViewTry).isEqualTo(expectedProjectViewTry);
    }

    @Test
    public void shouldParseFileAndUseDefaults() {
      // given
      var projectViewFilePath = Paths.get("/projectview/empty.bazelproject");
      var defaultProjectViewFilePath = Paths.get("/projectview/file1.bazelproject");

      // when
      var projectViewTry = parser.parse(projectViewFilePath, defaultProjectViewFilePath);

      // then
      var expectedProjectViewTry =
          ProjectView.builder()
              .targets(
                  Option.of(
                      new ProjectViewTargetsSection(
                          List.of(
                              new BuildTargetIdentifier("//included_target1.1"),
                              new BuildTargetIdentifier("//included_target1.2")),
                          List.of(new BuildTargetIdentifier("//excluded_target1.1")))))
              .bazelPath(Option.of(new ProjectViewBazelPathSection(Paths.get("path1/to/bazel"))))
              .debuggerAddress(
                  Option.of(
                      new ProjectViewDebuggerAddressSection(
                          HostAndPort.fromString("0.0.0.1:8000"))))
              .javaPath(Option.of(new ProjectViewJavaPathSection(Paths.get("path1/to/java"))))
              .build();
      assertThat(projectViewTry).isEqualTo(expectedProjectViewTry);
    }

    @Test
    public void shouldParseFileAndUseDefaultsWithEmptyImportedFile() {
      // given
      var projectViewFilePath = Paths.get("/projectview/empty.bazelproject");
      var defaultProjectViewFilePath = Paths.get("/projectview/file8ImportsEmpty.bazelproject");

      // when
      var projectViewTry = parser.parse(projectViewFilePath, defaultProjectViewFilePath);

      // then
      var expectedProjectViewTry =
          ProjectView.builder()
              .targets(
                  Option.of(
                      new ProjectViewTargetsSection(
                          List.of(new BuildTargetIdentifier("//included_target8.1")),
                          List.of(
                              new BuildTargetIdentifier("//excluded_target8.1"),
                              new BuildTargetIdentifier("//excluded_target8.2")))))
              .bazelPath(Option.of(new ProjectViewBazelPathSection(Paths.get("path8/to/bazel"))))
              .debuggerAddress(
                  Option.of(
                      new ProjectViewDebuggerAddressSection(
                          HostAndPort.fromString("0.0.0.8:8000"))))
              .javaPath(Option.of(new ProjectViewJavaPathSection(Paths.get("path8/to/java"))))
              .build();
      assertThat(projectViewTry).isEqualTo(expectedProjectViewTry);
    }
  }
}
