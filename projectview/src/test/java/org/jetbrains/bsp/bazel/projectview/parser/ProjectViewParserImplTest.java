package org.jetbrains.bsp.bazel.projectview.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import org.jetbrains.bsp.bazel.projectview.model.ProjectView;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBazelPathSection;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewDebuggerAddressSection;
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

  @Test(expected = IllegalStateException.class)
  public void shouldThrowExceptionForFileWithoutTargetsSection() {
    // given
    var projectViewFilePath = Paths.get("/projectview/without/targets.bazelproject");

    // when
    parser.parse(projectViewFilePath);

    // then
    // throw an IllegalStateException
  }

  @Test
  public void shouldReturnEmptyBazelPathForFileWithoutBazelPathSection() {
    // given
    var projectViewFilePath = Paths.get("/projectview/without/bazelpath.bazelproject");

    // when
    var projectView = parser.parse(projectViewFilePath);

    // then
    assertFalse(projectView.getBazelPath().isPresent());
  }

  @Test
  public void shouldReturnEmptyDebuggerAddressForFileWithoutDebuggerAddressSection() {
    // given
    var projectViewFilePath = Paths.get("/projectview/without/debuggeraddress.bazelproject");

    // when
    var projectView = parser.parse(projectViewFilePath);

    // then
    assertFalse(projectView.getDebuggerAddress().isPresent());
  }

  @Test
  public void shouldParseFileWithAllSections() {
    // given
    var projectViewFilePath = Paths.get("/projectview/file1.bazelproject");

    // when
    var projectView = parser.parse(projectViewFilePath);

    // then
    var expectedProjectView =
        ProjectView.builder()
            .targets(
                new ProjectViewTargetsSection(
                    List.of("//included_target1.1", "//included_target1.2"),
                    List.of("//excluded_target1.1")))
            .bazelPath(Optional.of(new ProjectViewBazelPathSection("path1/to/bazel")))
            .debuggerAddress(Optional.of(new ProjectViewDebuggerAddressSection("0.0.0.1:8000")))
            .build();
    assertEquals(expectedProjectView, projectView);
  }

  @Test
  public void shouldParseFileWithSingleImportedFileWitoutSingletonValues() {
    // given
    var projectViewFilePath = Paths.get("/projectview/file4ImportsFile1.bazelproject");

    // when
    var projectView = parser.parse(projectViewFilePath);

    // then
    var expectedProjectView =
        ProjectView.builder()
            .targets(
                new ProjectViewTargetsSection(
                    List.of("//included_target1.1", "//included_target1.2", "//included_target4.1"),
                    List.of(
                        "//excluded_target1.1", "//excluded_target4.1", "//excluded_target4.2")))
            .bazelPath(Optional.of(new ProjectViewBazelPathSection("path1/to/bazel")))
            .debuggerAddress(Optional.of(new ProjectViewDebuggerAddressSection("0.0.0.1:8000")))
            .build();

    assertEquals(expectedProjectView, projectView);
  }

  @Test
  public void shouldParseFileWithSingleImportedFileWithSingletonValues() {
    // given
    var projectViewFilePath = Paths.get("/projectview/file7ImportsFile1.bazelproject");

    // when
    var projectView = parser.parse(projectViewFilePath);

    // then
    var expectedProjectView =
        ProjectView.builder()
            .targets(
                new ProjectViewTargetsSection(
                    List.of("//included_target1.1", "//included_target1.2", "//included_target7.1"),
                    List.of(
                        "//excluded_target1.1", "//excluded_target7.1", "//excluded_target7.2")))
            .bazelPath(Optional.of(new ProjectViewBazelPathSection("path7/to/bazel")))
            .debuggerAddress(Optional.of(new ProjectViewDebuggerAddressSection("0.0.0.7:8000")))
            .build();
    assertEquals(expectedProjectView, projectView);
  }

  @Test
  public void shouldParseFileWithThreeImportedFiles() {
    // given
    var projectViewFilePath = Paths.get("/projectview/file5ImportsFile1File2File3.bazelproject");

    // when
    var projectView = parser.parse(projectViewFilePath);

    // then
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
            .build();
    assertEquals(expectedProjectView, projectView);
  }

  @Test
  public void shouldParseFileWithNestedImportedFiles() {
    // given
    var projectViewFilePath = Paths.get("/projectview/file6ImportsFile2File3File4.bazelproject");

    // when
    var projectView = parser.parse(projectViewFilePath);

    // then
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
            .build();
    assertEquals(expectedProjectView, projectView);
  }

  // ProjectView parse(projectViewFileContent, defaultProjectViewFileContent)

  @Test(expected = IllegalStateException.class)
  public void shouldThrowExceptionForDefaultFileWithoutTargetsSection() {
    // given
    var projectViewFilePath = Paths.get("/projectview/file1.bazelproject");
    var defaultProjectViewFilePath = Paths.get("/projectview/without/targets.bazelproject");

    // when
    parser.parse(projectViewFilePath, defaultProjectViewFilePath);

    // then
    // throw an IllegalStateException
  }

  @Test
  public void shouldReturnEmptyForDefaultFileWithoutBazelPathSection() {
    // given
    var projectViewFilePath = Paths.get("/projectview/empty.bazelproject");
    var defaultProjectViewFilePath = Paths.get("/projectview/without/bazelpath.bazelproject");

    // when
    var projectView = parser.parse(projectViewFilePath, defaultProjectViewFilePath);

    // then
    assertFalse(projectView.getBazelPath().isPresent());
  }

  @Test
  public void shouldReturnEmptyForDefaultFileWithoutDebuggerAddressSection() {
    // given
    var projectViewFilePath = Paths.get("/projectview/empty.bazelproject");
    var defaultProjectViewFilePath = Paths.get("/projectview/without/debuggeraddress.bazelproject");

    // when
    var projectView = parser.parse(projectViewFilePath, defaultProjectViewFilePath);

    // then
    assertFalse(projectView.getDebuggerAddress().isPresent());
  }

  @Test
  public void shouldParseFileAndSkipDefaults() {
    // given
    var projectViewFilePath = Paths.get("/projectview/file1.bazelproject");
    var defaultProjectViewFilePath = Paths.get("/projectview/file2.bazelproject");

    // when
    var projectView = parser.parse(projectViewFilePath, defaultProjectViewFilePath);

    // then
    var expectedProjectView =
        ProjectView.builder()
            .targets(
                new ProjectViewTargetsSection(
                    List.of("//included_target1.1", "//included_target1.2"),
                    List.of("//excluded_target1.1")))
            .bazelPath(Optional.of(new ProjectViewBazelPathSection("path1/to/bazel")))
            .debuggerAddress(Optional.of(new ProjectViewDebuggerAddressSection("0.0.0.1:8000")))
            .build();
    assertEquals(expectedProjectView, projectView);
  }

  @Test
  public void shouldParseDefaultsForNotExistingFile() {
    // given
    var projectViewFilePath = Paths.get("/doesnt/exist.bazelproject");
    var defaultProjectViewFilePath = Paths.get("/projectview/file1.bazelproject");

    // when
    var projectView = parser.parse(projectViewFilePath, defaultProjectViewFilePath);

    // then
    var expectedProjectView =
        ProjectView.builder()
            .targets(
                new ProjectViewTargetsSection(
                    List.of("//included_target1.1", "//included_target1.2"),
                    List.of("//excluded_target1.1")))
            .bazelPath(Optional.of(new ProjectViewBazelPathSection("path1/to/bazel")))
            .debuggerAddress(Optional.of(new ProjectViewDebuggerAddressSection("0.0.0.1:8000")))
            .build();
    assertEquals(expectedProjectView, projectView);
  }

  @Test
  public void shouldParseFileAndUseDefaults() {
    // given
    var projectViewFilePath = Paths.get("/projectview/empty.bazelproject");
    var defaultProjectViewFilePath = Paths.get("/projectview/file1.bazelproject");

    // when
    var projectView = parser.parse(projectViewFilePath, defaultProjectViewFilePath);

    // then
    var expectedProjectView =
        ProjectView.builder()
            .targets(
                new ProjectViewTargetsSection(
                    List.of("//included_target1.1", "//included_target1.2"),
                    List.of("//excluded_target1.1")))
            .bazelPath(Optional.of(new ProjectViewBazelPathSection("path1/to/bazel")))
            .debuggerAddress(Optional.of(new ProjectViewDebuggerAddressSection("0.0.0.1:8000")))
            .build();
    assertEquals(expectedProjectView, projectView);
  }
}
