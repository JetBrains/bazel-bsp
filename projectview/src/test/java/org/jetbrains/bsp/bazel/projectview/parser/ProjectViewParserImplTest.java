package org.jetbrains.bsp.bazel.projectview.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import org.jetbrains.bsp.bazel.projectview.model.ProjectView;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBazelPathSection;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewDebuggerAddressSection;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewJavaPathSection;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewTargetsSection;
import org.junit.Before;
import org.junit.Test;

public class ProjectViewParserImplTest {

  private ProjectViewParser parser;

  @Before
  public void before() {
    // given
    this.parser = new ProjectViewParserMockTestImpl();
  }

  // ProjectView parse(projectViewFilePath)

  @Test
  public void shouldReturnFailureForNotExistingFile() {
    // given
    var projectViewFilePath = Paths.get("/does/not/exist.bazelproject");

    // when
    var projectViewTry = parser.parse(projectViewFilePath);

    // then
    assertTrue(projectViewTry.isFailure());
    assertEquals(
        "/does/not/exist.bazelproject file does not exist!",
        projectViewTry.getCause().getMessage());
  }

  @Test
  public void shouldReturnFailureForNotExistingImportedFile() {
    // given
    var projectViewFilePath = Paths.get("/projectview/file9ImportsNotExisting.bazelproject");

    // when
    var projectViewTry = parser.parse(projectViewFilePath);

    System.out.println(projectViewTry);
    // then
    assertTrue(projectViewTry.isFailure());
    assertEquals(
        "/projectview/does/not/exist.bazelproject file does not exist!",
        projectViewTry.getCause().getMessage());
  }

  @Test
  public void shouldReturnEmptyTargetsSectionForFileWithoutTargetsSection() {
    // given
    var projectViewFilePath = Paths.get("/projectview/without/targets.bazelproject");

    // when
    var projectViewTry = parser.parse(projectViewFilePath);

    // then
    assertTrue(projectViewTry.isSuccess());
    var projectView = projectViewTry.get();

    var expectedTargetsSection = new ProjectViewTargetsSection();
    assertEquals(expectedTargetsSection, projectView.getTargets());
  }

  @Test
  public void shouldReturnEmptyBazelPathForFileWithoutBazelPathSection() {
    // given
    var projectViewFilePath = Paths.get("/projectview/without/bazelpath.bazelproject");

    // when
    var projectViewTry = parser.parse(projectViewFilePath);

    // then
    assertTrue(projectViewTry.isSuccess());
    var projectView = projectViewTry.get();

    assertTrue(projectView.getBazelPath().isEmpty());
  }

  @Test
  public void shouldReturnEmptyDebuggerAddressForFileWithoutDebuggerAddressSection() {
    // given
    var projectViewFilePath = Paths.get("/projectview/without/debuggeraddress.bazelproject");

    // when
    var projectViewTry = parser.parse(projectViewFilePath);

    // then
    assertTrue(projectViewTry.isSuccess());
    var projectView = projectViewTry.get();

    assertTrue(projectView.getDebuggerAddress().isEmpty());
  }

  @Test
  public void shouldReturnEmptyJavaPathSectionForFileWithoutJavaPathSection() {
    // given
    var projectViewFilePath = Paths.get("/projectview/without/javapath.bazelproject");

    // when
    var projectViewTry = parser.parse(projectViewFilePath);

    // then
    assertTrue(projectViewTry.isSuccess());
    var projectView = projectViewTry.get();

    assertTrue(projectView.getJavaPath().isEmpty());
  }

  @Test
  public void shouldParseEmptyFile() {
    // given
    var projectViewFilePath = Paths.get("/projectview/empty.bazelproject");

    // when
    var projectViewTry = parser.parse(projectViewFilePath);

    // then
    assertTrue(projectViewTry.isSuccess());
    var projectView = projectViewTry.get();

    var expectedProjectView =
        ProjectView.builder()
            .targets(new ProjectViewTargetsSection())
            .bazelPath(Optional.empty())
            .debuggerAddress(Optional.empty())
            .javaPath(Optional.empty())
            .build()
            .get();
    assertEquals(expectedProjectView, projectView);
  }

  @Test
  public void shouldParseFileWithAllSections() {
    // given
    var projectViewFilePath = Paths.get("/projectview/file1.bazelproject");

    // when
    var projectViewTry = parser.parse(projectViewFilePath);

    // then
    assertTrue(projectViewTry.isSuccess());
    var projectView = projectViewTry.get();

    var expectedProjectView =
        ProjectView.builder()
            .targets(
                new ProjectViewTargetsSection(
                    List.of("//included_target1.1", "//included_target1.2"),
                    List.of("//excluded_target1.1")))
            .bazelPath(Optional.of(new ProjectViewBazelPathSection("path1/to/bazel")))
            .debuggerAddress(Optional.of(new ProjectViewDebuggerAddressSection("0.0.0.1:8000")))
            .javaPath(Optional.of(new ProjectViewJavaPathSection("path1/to/java")))
            .build()
            .get();
    assertEquals(expectedProjectView, projectView);
  }

  @Test
  public void shouldParseFileWithSingleImportedFileWithoutSingletonValues() {
    // given
    var projectViewFilePath = Paths.get("/projectview/file4ImportsFile1.bazelproject");

    // when
    var projectViewTry = parser.parse(projectViewFilePath);

    // then
    assertTrue(projectViewTry.isSuccess());
    var projectView = projectViewTry.get();

    var expectedProjectView =
        ProjectView.builder()
            .targets(
                new ProjectViewTargetsSection(
                    List.of("//included_target1.1", "//included_target1.2", "//included_target4.1"),
                    List.of(
                        "//excluded_target1.1", "//excluded_target4.1", "//excluded_target4.2")))
            .bazelPath(Optional.of(new ProjectViewBazelPathSection("path1/to/bazel")))
            .debuggerAddress(Optional.of(new ProjectViewDebuggerAddressSection("0.0.0.1:8000")))
            .javaPath(Optional.of(new ProjectViewJavaPathSection("path1/to/java")))
            .build()
            .get();
    assertEquals(expectedProjectView, projectView);
  }

  @Test
  public void shouldParseFileWithSingleImportedFileWithSingletonValues() {
    // given
    var projectViewFilePath = Paths.get("/projectview/file7ImportsFile1.bazelproject");

    // when
    var projectViewTry = parser.parse(projectViewFilePath);

    // then
    assertTrue(projectViewTry.isSuccess());
    var projectView = projectViewTry.get();

    var expectedProjectView =
        ProjectView.builder()
            .targets(
                new ProjectViewTargetsSection(
                    List.of("//included_target1.1", "//included_target1.2", "//included_target7.1"),
                    List.of(
                        "//excluded_target1.1", "//excluded_target7.1", "//excluded_target7.2")))
            .bazelPath(Optional.of(new ProjectViewBazelPathSection("path7/to/bazel")))
            .debuggerAddress(Optional.of(new ProjectViewDebuggerAddressSection("0.0.0.7:8000")))
            .javaPath(Optional.of(new ProjectViewJavaPathSection("path7/to/java")))
            .build()
            .get();
    assertEquals(expectedProjectView, projectView);
  }

  @Test
  public void shouldParseFileWithEmptyImportedFile() {
    // given
    var projectViewFilePath = Paths.get("/projectview/file8ImportsEmpty.bazelproject");

    // when
    var projectViewTry = parser.parse(projectViewFilePath);

    // then
    assertTrue(projectViewTry.isSuccess());
    var projectView = projectViewTry.get();

    var expectedProjectView =
        ProjectView.builder()
            .targets(
                new ProjectViewTargetsSection(
                    List.of("//included_target8.1"),
                    List.of("//excluded_target8.1", "//excluded_target8.2")))
            .bazelPath(Optional.of(new ProjectViewBazelPathSection("path8/to/bazel")))
            .debuggerAddress(Optional.of(new ProjectViewDebuggerAddressSection("0.0.0.8:8000")))
            .javaPath(Optional.of(new ProjectViewJavaPathSection("path8/to/java")))
            .build()
            .get();
    assertEquals(expectedProjectView, projectView);
  }

  @Test
  public void shouldParseFileWithThreeImportedFiles() {
    // given
    var projectViewFilePath = Paths.get("/projectview/file5ImportsFile1File2File3.bazelproject");

    // when
    var projectViewTry = parser.parse(projectViewFilePath);

    // then
    assertTrue(projectViewTry.isSuccess());
    var projectView = projectViewTry.get();

    var expectedProjectView =
        ProjectView.builder()
            .targets(
                new ProjectViewTargetsSection(
                    List.of(
                        "//included_target1.1",
                        "//included_target1.2",
                        "//included_target2.1",
                        "//included_target3.1"),
                    List.of(
                        "//excluded_target1.1",
                        "//excluded_target2.1",
                        "//excluded_target5.1",
                        "//excluded_target5.2")))
            .bazelPath(Optional.of(new ProjectViewBazelPathSection("path3/to/bazel")))
            .debuggerAddress(Optional.of(new ProjectViewDebuggerAddressSection("0.0.0.3:8000")))
            .javaPath(Optional.of(new ProjectViewJavaPathSection("path3/to/java")))
            .build()
            .get();
    assertEquals(expectedProjectView, projectView);
  }

  @Test
  public void shouldParseFileWithNestedImportedFiles() {
    // given
    var projectViewFilePath = Paths.get("/projectview/file6ImportsFile2File3File4.bazelproject");

    // when
    var projectViewTry = parser.parse(projectViewFilePath);

    // then
    assertTrue(projectViewTry.isSuccess());
    var projectView = projectViewTry.get();

    var expectedProjectView =
        ProjectView.builder()
            .targets(
                new ProjectViewTargetsSection(
                    List.of(
                        "//included_target1.1",
                        "//included_target1.2",
                        "//included_target2.1",
                        "//included_target3.1",
                        "//included_target4.1"),
                    List.of(
                        "//excluded_target1.1",
                        "//excluded_target2.1",
                        "//excluded_target4.1",
                        "//excluded_target4.2")))
            .bazelPath(Optional.of(new ProjectViewBazelPathSection("path1/to/bazel")))
            .debuggerAddress(Optional.of(new ProjectViewDebuggerAddressSection("0.0.0.1:8000")))
            .javaPath(Optional.of(new ProjectViewJavaPathSection("path1/to/java")))
            .build()
            .get();
    assertEquals(expectedProjectView, projectView);
  }

  // ProjectView parse(projectViewFilePath, defaultProjectViewFilePath)

  @Test
  public void shouldReturnFailureForNotExistingDefaultFile() {
    // given
    var projectViewFilePath = Paths.get("/projectview/file1.bazelproject");
    var defaultProjectViewFilePath = Paths.get("/does/not/exist.bazelproject");

    // when
    var projectViewTry = parser.parse(projectViewFilePath, defaultProjectViewFilePath);

    // then
    assertTrue(projectViewTry.isFailure());
    assertEquals(
        "/does/not/exist.bazelproject file does not exist!",
        projectViewTry.getCause().getMessage());
  }

  @Test
  public void shouldReturnFailureForNotExistingDefaultFileAndNotExistingFile() {
    // given
    var projectViewFilePath = Paths.get("/does/not/exist.bazelproject");
    var defaultProjectViewFilePath = Paths.get("/does/not/exist.bazelproject");

    // when
    var projectViewTry = parser.parse(projectViewFilePath, defaultProjectViewFilePath);

    // then
    assertTrue(projectViewTry.isFailure());
    assertEquals(
        "/does/not/exist.bazelproject file does not exist!",
        projectViewTry.getCause().getMessage());
  }

  @Test
  public void shouldReturnFile1ForEmptyDefaultFile() {
    // given
    var projectViewFilePath = Paths.get("/projectview/file1.bazelproject");
    var defaultProjectViewFilePath = Paths.get("/projectview/empty.bazelproject");

    // when
    var projectViewTry = parser.parse(projectViewFilePath, defaultProjectViewFilePath);

    // then
    assertTrue(projectViewTry.isSuccess());
    var projectView = projectViewTry.get();

    var expectedProjectView =
        ProjectView.builder()
            .targets(
                new ProjectViewTargetsSection(
                    List.of("//included_target1.1", "//included_target1.2"),
                    List.of("//excluded_target1.1")))
            .bazelPath(Optional.of(new ProjectViewBazelPathSection("path1/to/bazel")))
            .debuggerAddress(Optional.of(new ProjectViewDebuggerAddressSection("0.0.0.1:8000")))
            .javaPath(Optional.of(new ProjectViewJavaPathSection("path1/to/java")))
            .build()
            .get();
    assertEquals(expectedProjectView, projectView);
  }

  @Test
  public void shouldReturnEmptyTargetsForDefaultFileWithoutTargetsSection() {
    // given
    var projectViewFilePath = Paths.get("/projectview/empty.bazelproject");
    var defaultProjectViewFilePath = Paths.get("/projectview/without/targets.bazelproject");

    // when
    var projectViewTry = parser.parse(projectViewFilePath, defaultProjectViewFilePath);

    // then
    assertTrue(projectViewTry.isSuccess());
    var projectView = projectViewTry.get();

    var expectedTargetsSection = new ProjectViewTargetsSection();
    assertEquals(expectedTargetsSection, projectView.getTargets());
  }

  @Test
  public void shouldReturnEmptyForDefaultFileWithoutBazelPathSection() {
    // given
    var projectViewFilePath = Paths.get("/projectview/empty.bazelproject");
    var defaultProjectViewFilePath = Paths.get("/projectview/without/bazelpath.bazelproject");

    // when
    var projectViewTry = parser.parse(projectViewFilePath, defaultProjectViewFilePath);

    // then
    assertTrue(projectViewTry.isSuccess());
    var projectView = projectViewTry.get();

    assertTrue(projectView.getBazelPath().isEmpty());
  }

  @Test
  public void shouldReturnEmptyForDefaultFileWithoutDebuggerAddressSection() {
    // given
    var projectViewFilePath = Paths.get("/projectview/empty.bazelproject");
    var defaultProjectViewFilePath = Paths.get("/projectview/without/debuggeraddress.bazelproject");

    // when
    var projectViewTry = parser.parse(projectViewFilePath, defaultProjectViewFilePath);

    // then
    assertTrue(projectViewTry.isSuccess());
    var projectView = projectViewTry.get();

    assertTrue(projectView.getDebuggerAddress().isEmpty());
  }

  @Test
  public void shouldReturnEmptyForDefaultFileWithoutJavaPathSection() {
    // given
    var projectViewFilePath = Paths.get("/projectview/empty.bazelproject");
    var defaultProjectViewFilePath = Paths.get("/projectview/without/javapath.bazelproject");

    // when
    var projectViewTry = parser.parse(projectViewFilePath, defaultProjectViewFilePath);

    // then
    assertTrue(projectViewTry.isSuccess());
    var projectView = projectViewTry.get();

    assertTrue(projectView.getJavaPath().isEmpty());
  }

  @Test
  public void shouldParseFileAndSkipDefaults() {
    // given
    var projectViewFilePath = Paths.get("/projectview/file1.bazelproject");
    var defaultProjectViewFilePath = Paths.get("/projectview/file2.bazelproject");

    // when
    var projectViewTry = parser.parse(projectViewFilePath, defaultProjectViewFilePath);

    // then
    assertTrue(projectViewTry.isSuccess());
    var projectView = projectViewTry.get();

    var expectedProjectView =
        ProjectView.builder()
            .targets(
                new ProjectViewTargetsSection(
                    List.of("//included_target1.1", "//included_target1.2"),
                    List.of("//excluded_target1.1")))
            .bazelPath(Optional.of(new ProjectViewBazelPathSection("path1/to/bazel")))
            .debuggerAddress(Optional.of(new ProjectViewDebuggerAddressSection("0.0.0.1:8000")))
            .javaPath(Optional.of(new ProjectViewJavaPathSection("path1/to/java")))
            .build()
            .get();
    assertEquals(expectedProjectView, projectView);
  }

  @Test
  public void shouldParseDefaultsForNotExistingFile() {
    // given
    var projectViewFilePath = Paths.get("/doesnt/exist.bazelproject");
    var defaultProjectViewFilePath = Paths.get("/projectview/file1.bazelproject");

    // when
    var projectViewTry = parser.parse(projectViewFilePath, defaultProjectViewFilePath);

    // then
    assertTrue(projectViewTry.isSuccess());
    var projectView = projectViewTry.get();

    var expectedProjectView =
        ProjectView.builder()
            .targets(
                new ProjectViewTargetsSection(
                    List.of("//included_target1.1", "//included_target1.2"),
                    List.of("//excluded_target1.1")))
            .bazelPath(Optional.of(new ProjectViewBazelPathSection("path1/to/bazel")))
            .debuggerAddress(Optional.of(new ProjectViewDebuggerAddressSection("0.0.0.1:8000")))
            .javaPath(Optional.of(new ProjectViewJavaPathSection("path1/to/java")))
            .build()
            .get();
    assertEquals(expectedProjectView, projectView);
  }

  @Test
  public void shouldParseFileAndUseDefaults() {
    // given
    var projectViewFilePath = Paths.get("/projectview/empty.bazelproject");
    var defaultProjectViewFilePath = Paths.get("/projectview/file1.bazelproject");

    // when
    var projectViewTry = parser.parse(projectViewFilePath, defaultProjectViewFilePath);

    // then
    assertTrue(projectViewTry.isSuccess());
    var projectView = projectViewTry.get();

    var expectedProjectView =
        ProjectView.builder()
            .targets(
                new ProjectViewTargetsSection(
                    List.of("//included_target1.1", "//included_target1.2"),
                    List.of("//excluded_target1.1")))
            .bazelPath(Optional.of(new ProjectViewBazelPathSection("path1/to/bazel")))
            .debuggerAddress(Optional.of(new ProjectViewDebuggerAddressSection("0.0.0.1:8000")))
            .javaPath(Optional.of(new ProjectViewJavaPathSection("path1/to/java")))
            .build()
            .get();
    assertEquals(expectedProjectView, projectView);
  }

  @Test
  public void shouldParseFileAndUseDefaultsWithEmptyImportedFile() {
    // given
    var projectViewFilePath = Paths.get("/projectview/empty.bazelproject");
    var defaultProjectViewFilePath = Paths.get("/projectview/file8ImportsEmpty.bazelproject");

    // when
    var projectViewTry = parser.parse(projectViewFilePath, defaultProjectViewFilePath);

    // then
    assertTrue(projectViewTry.isSuccess());
    var projectView = projectViewTry.get();

    var expectedProjectView =
        ProjectView.builder()
            .targets(
                new ProjectViewTargetsSection(
                    List.of("//included_target8.1"),
                    List.of("//excluded_target8.1", "//excluded_target8.2")))
            .bazelPath(Optional.of(new ProjectViewBazelPathSection("path8/to/bazel")))
            .debuggerAddress(Optional.of(new ProjectViewDebuggerAddressSection("0.0.0.8:8000")))
            .javaPath(Optional.of(new ProjectViewJavaPathSection("path8/to/java")))
            .build()
            .get();
    assertEquals(expectedProjectView, projectView);
  }
}
