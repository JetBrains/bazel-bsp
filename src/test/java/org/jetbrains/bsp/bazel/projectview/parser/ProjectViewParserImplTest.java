package org.jetbrains.bsp.bazel.projectview.parser;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.jetbrains.bsp.bazel.projectview.model.ProjectView;
import org.jetbrains.bsp.bazel.projectview.model.sections.specific.DirectoriesSection;
import org.jetbrains.bsp.bazel.projectview.model.sections.specific.TargetsSection;
import org.junit.Before;
import org.junit.Test;

public class ProjectViewParserImplTest {

  private static final DirectoriesSection EXPECTED_DIRECTORIES_SECTION =
      new DirectoriesSection(
          ImmutableList.of(Paths.get(".")),
          ImmutableList.of(
              Paths.get("excluded_dir1"), Paths.get("excluded_dir2"), Paths.get("excluded_dir3")));

  private static final TargetsSection EXPECTED_TARGETS_SECTION =
      new TargetsSection(
          ImmutableList.of("//included_target1:test1", "//included_target1:test2"),
          ImmutableList.of("//excluded_target1:test1"));

  private static final ProjectView EXPECTED_PROJECT_VIEW =
      ProjectView.builder()
          .directories(EXPECTED_DIRECTORIES_SECTION)
          .targets(EXPECTED_TARGETS_SECTION)
          .build();

  private ProjectViewParser parser;

  @Before
  public void before() {
    this.parser = new ProjectViewParserMockTestImpl();
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowMissingDirectoriesSection() throws IOException {
    Path projectViewFilePath = Paths.get("/projectview/projectViewWithoutDirectories.bazelproject");
    parser.parse(projectViewFilePath);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowMissingTargetsSection() throws IOException {
    Path projectViewFilePath = Paths.get("/projectview/projectViewWithoutTargets.bazelproject");
    parser.parse(projectViewFilePath);
  }

  @Test
  public void shouldParseFile() throws IOException {
    Path projectViewFilePath = Paths.get("/projectview/projectView.bazelproject");
    ProjectView projectView = parser.parse(projectViewFilePath);

    assertEquals(EXPECTED_PROJECT_VIEW, projectView);
  }

  @Test
  public void shouldParseFileWithTabs() throws IOException {
    Path projectViewFilePath = Paths.get("/projectview/projectViewWithTabs.bazelproject");
    ProjectView projectView = parser.parse(projectViewFilePath);

    assertEquals(EXPECTED_PROJECT_VIEW, projectView);
  }

  @Test
  public void shouldParseFileWithImport() throws IOException {
    Path projectViewFilePath = Paths.get("/projectview/projectViewWithImport.bazelproject");
    ProjectView projectView = parser.parse(projectViewFilePath);

    assertEquals(EXPECTED_PROJECT_VIEW, projectView);
  }
}
