package org.jetbrains.bsp.bazel.projectview.parser;

import com.google.common.collect.ImmutableList;
import com.google.common.io.CharSource;
import com.google.common.io.Files;
import org.jetbrains.bsp.bazel.projectview.model.ProjectView;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewTargetsSection;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

public class ProjectViewParserImplTest {

  private ProjectViewParser parser;

  @Before
  public void before() {
    // given
    this.parser = new ProjectViewParserMockTestImpl();
  }

  // ProjectView parse(Path projectViewFilePath)

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
            .build();

    assertEquals(expectedProjectView, projectView);
  }

  @Test
  public void shouldParseFileWithSingleImportedFile() {
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
            .build();

    assertEquals(expectedProjectView, projectView);
  }

  // ProjectView parse(String projectViewFileContent, String defaultProjectViewFileContent)

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
                    ImmutableList.of(
                        "//included_target1.1",
                        "//included_target1.2"),
                    ImmutableList.of(
                        "//excluded_target1.1")))
            .build();

    assertEquals(expectedProjectView, projectView);
  }

  @Test
  public void shouldParseDefaultsForNotExistingFile() {
    // given
    Path projectViewFilePath = Paths.get("/doesnt/exist.bazelproject");
    Path defaultProjectViewFilePath = Paths.get("/projectview/file2.bazelproject");

    // when
    ProjectView projectView = parser.parse(projectViewFilePath, defaultProjectViewFilePath);

    // then
    ProjectView expectedProjectView =
        ProjectView.builder()
            .targets(
                new ProjectViewTargetsSection(
                    ImmutableList.of(
                        "//included_target2.1"),
                    ImmutableList.of(
                        "//excluded_target2.1")))
            .build();

    assertEquals(expectedProjectView, projectView);
  }

  @Test
  public void shouldParseFileAndUseDefaults() {
    // given
    Path projectViewFilePath = Paths.get("/projectview/empty.bazelproject");
    Path defaultProjectViewFilePath = Paths.get("/projectview/file2.bazelproject");

    // when
    ProjectView projectView = parser.parse(projectViewFilePath, defaultProjectViewFilePath);

    // then
    ProjectView expectedProjectView =
        ProjectView.builder()
            .targets(
                new ProjectViewTargetsSection(
                    ImmutableList.of(
                        "//included_target2.1"),
                    ImmutableList.of(
                        "//excluded_target2.1")))
            .build();

    assertEquals(expectedProjectView, projectView);
  }
}
