package org.jetbrains.bsp.bazel.projectview.parser;

import com.google.common.collect.ImmutableList;
import org.jetbrains.bsp.bazel.projectview.model.ProjectView;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBazelPathSection;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewTargetsSection;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

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
    Path projectViewFilePath = Paths.get("/projectview/without/targets.bazelproject");

    // when
    parser.parse(projectViewFilePath);

    // then
    // throw an IllegalStateException
  }

  @Test
  public void shouldReturnEmptyBazelPathForFileWithoutBazelPathSection() {
    // given
    Path projectViewFilePath = Paths.get("/projectview/without/bazelpath.bazelproject");

    // when
    ProjectView projectView = parser.parse(projectViewFilePath);

    // then
    assertFalse(projectView.getBazelPath().isPresent());
  }

  @Test
  public void shouldParseFileWithAllSections() {
    // given
    Path projectViewFilePath = Paths.get("/projectview/file1.bazelproject");

    // when
    ProjectView projectView = parser.parse(projectViewFilePath);

    // then
    ProjectView expectedProjectView =
        ProjectView.builder()
            .targets(
                new ProjectViewTargetsSection(
                    ImmutableList.of("//included_target1.1", "//included_target1.2"),
                    ImmutableList.of("//excluded_target1.1")))
            .bazelPath(Optional.of(new ProjectViewBazelPathSection("path1/to/bazel")))
            .build();
    assertEquals(expectedProjectView, projectView);
  }

  @Test
  public void shouldParseFileWithSingleImportedFileWitoutSingletonValues() {
    // given
    Path projectViewFilePath = Paths.get("/projectview/file4ImportsFile1.bazelproject");

    // when
    ProjectView projectView = parser.parse(projectViewFilePath);

    // then
    ProjectView expectedProjectView =
        ProjectView.builder()
            .targets(
                new ProjectViewTargetsSection(
                    ImmutableList.of(
                        "//included_target1.1", "//included_target1.2", "//included_target4.1"),
                    ImmutableList.of(
                        "//excluded_target1.1", "//excluded_target4.1", "//excluded_target4.2")))
            .bazelPath(Optional.of(new ProjectViewBazelPathSection("path1/to/bazel")))
            .build();

    assertEquals(expectedProjectView, projectView);
  }

  @Test
  public void shouldParseFileWithSingleImportedFileWithSingletonValues() {
    // given
    Path projectViewFilePath = Paths.get("/projectview/file7ImportsFile1.bazelproject");

    // when
    ProjectView projectView = parser.parse(projectViewFilePath);

    // then
    ProjectView expectedProjectView =
        ProjectView.builder()
            .targets(
                new ProjectViewTargetsSection(
                    ImmutableList.of(
                        "//included_target1.1", "//included_target1.2", "//included_target7.1"),
                    ImmutableList.of(
                        "//excluded_target1.1", "//excluded_target7.1", "//excluded_target7.2")))
            .bazelPath(Optional.of(new ProjectViewBazelPathSection("path7/to/bazel")))
            .build();
    assertEquals(expectedProjectView, projectView);
  }

  @Test
  public void shouldParseFileWithThreeImportedFiles() {
    // given
    Path projectViewFilePath = Paths.get("/projectview/file5ImportsFile1File2File3.bazelproject");

    // when
    ProjectView projectView = parser.parse(projectViewFilePath);

    // then
    ProjectView expectedProjectView =
        ProjectView.builder()
            .targets(
                new ProjectViewTargetsSection(
                    ImmutableList.of(
                        "//included_target1.1",
                        "//included_target1.2",
                        "//included_target2.1",
                        "//included_target3.1"),
                    ImmutableList.of(
                        "//excluded_target1.1",
                        "//excluded_target2.1",
                        "//excluded_target5.1",
                        "//excluded_target5.2")))
            .bazelPath(Optional.of(new ProjectViewBazelPathSection("path3/to/bazel")))
            .build();
    assertEquals(expectedProjectView, projectView);
  }

  @Test
  public void shouldParseFileWithNestedImportedFiles() {
    // given
    Path projectViewFilePath = Paths.get("/projectview/file6ImportsFile2File3File4.bazelproject");

    // when
    ProjectView projectView = parser.parse(projectViewFilePath);

    // then
    ProjectView expectedProjectView =
        ProjectView.builder()
            .targets(
                new ProjectViewTargetsSection(
                    ImmutableList.of(
                        "//included_target1.1",
                        "//included_target1.2",
                        "//included_target2.1",
                        "//included_target3.1",
                        "//included_target4.1"),
                    ImmutableList.of(
                        "//excluded_target1.1",
                        "//excluded_target2.1",
                        "//excluded_target4.1",
                        "//excluded_target4.2")))
            .bazelPath(Optional.of(new ProjectViewBazelPathSection("path1/to/bazel")))
            .build();

    assertEquals(expectedProjectView, projectView);
  }

  // ProjectView parse(projectViewFileContent, defaultProjectViewFileContent)

  @Test(expected = IllegalStateException.class)
  public void shouldThrowExceptionForDefaultFileWithoutTargetsSection() {
    // given
    Path projectViewFilePath = Paths.get("/projectview/file1.bazelproject");
    Path defaultProjectViewFilePath = Paths.get("/projectview/without/targets.bazelproject");

    // when
    parser.parse(projectViewFilePath, defaultProjectViewFilePath);

    // then
    // throw an IllegalStateException
  }

  @Test
  public void shouldReturnEmptyForDefaultFileWithoutBazelPathSection() {
    // given
    Path projectViewFilePath = Paths.get("/projectview/empty.bazelproject");
    Path defaultProjectViewFilePath = Paths.get("/projectview/without/bazelpath.bazelproject");

    // when
    ProjectView projectView = parser.parse(projectViewFilePath, defaultProjectViewFilePath);

    // then
    assertFalse(projectView.getBazelPath().isPresent());
  }

  @Test
  public void shouldParseFileAndSkipDefaults() {
    // given
    Path projectViewFilePath = Paths.get("/projectview/file1.bazelproject");
    Path defaultProjectViewFilePath = Paths.get("/projectview/file2.bazelproject");

    // when
    ProjectView projectView = parser.parse(projectViewFilePath, defaultProjectViewFilePath);

    // then
    ProjectView expectedProjectView =
        ProjectView.builder()
            .targets(
                new ProjectViewTargetsSection(
                    ImmutableList.of("//included_target1.1", "//included_target1.2"),
                    ImmutableList.of("//excluded_target1.1")))
            .bazelPath(Optional.of(new ProjectViewBazelPathSection("path1/to/bazel")))
            .build();

    assertEquals(expectedProjectView, projectView);
  }

  @Test
  public void shouldParseDefaultsForNotExistingFile() {
    // given
    Path projectViewFilePath = Paths.get("/doesnt/exist.bazelproject");
    Path defaultProjectViewFilePath = Paths.get("/projectview/file1.bazelproject");

    // when
    ProjectView projectView = parser.parse(projectViewFilePath, defaultProjectViewFilePath);

    // then
    ProjectView expectedProjectView =
        ProjectView.builder()
            .targets(
                new ProjectViewTargetsSection(
                    ImmutableList.of("//included_target1.1", "//included_target1.2"),
                    ImmutableList.of("//excluded_target1.1")))
            .bazelPath(Optional.of(new ProjectViewBazelPathSection("path1/to/bazel")))
            .build();

    assertEquals(expectedProjectView, projectView);
  }

  @Test
  public void shouldParseFileAndUseDefaults() {
    // given
    Path projectViewFilePath = Paths.get("/projectview/empty.bazelproject");
    Path defaultProjectViewFilePath = Paths.get("/projectview/file1.bazelproject");

    // when
    ProjectView projectView = parser.parse(projectViewFilePath, defaultProjectViewFilePath);

    // then
    ProjectView expectedProjectView =
        ProjectView.builder()
            .targets(
                new ProjectViewTargetsSection(
                    ImmutableList.of("//included_target1.1", "//included_target1.2"),
                    ImmutableList.of("//excluded_target1.1")))
            .bazelPath(Optional.of(new ProjectViewBazelPathSection("path1/to/bazel")))
            .build();

    assertEquals(expectedProjectView, projectView);
  }
}
