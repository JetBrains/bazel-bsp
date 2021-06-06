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

    DirectoriesSection expectedDirectoriesSection =
        new DirectoriesSection(
            ImmutableList.of(Paths.get(".")),
            ImmutableList.of(
                Paths.get("excluded_dir1"),
                Paths.get("excluded_dir2"),
                Paths.get("excluded_dir3")));
    TargetsSection expectedTargetsSection =
        new TargetsSection(
            ImmutableList.of("//included_target1:test1", "//included_target1:test2"),
            ImmutableList.of("//excluded_target1:test1"));

    ProjectView expectedProjectView =
        ProjectView.builder()
            .directories(expectedDirectoriesSection)
            .targets(expectedTargetsSection)
            .build();

    assertEquals(expectedProjectView, projectView);
  }

  @Test
  public void shouldParseFileWithImport() throws IOException {
    Path projectViewFilePath = Paths.get("/projectview/projectViewWithImport.bazelproject");
    ProjectView projectView = parser.parse(projectViewFilePath);

    DirectoriesSection expectedDirectoriesSection =
        new DirectoriesSection(
            ImmutableList.of(Paths.get(".")),
            ImmutableList.of(
                Paths.get("excluded_dir1"),
                Paths.get("excluded_dir2"),
                Paths.get("excluded_dir3")));
    TargetsSection expectedTargetsSection =
        new TargetsSection(
            ImmutableList.of("//included_target1:test1", "//included_target1:test2"),
            ImmutableList.of("//excluded_target1:test1"));

    ProjectView expectedProjectView =
        ProjectView.builder()
            .directories(expectedDirectoriesSection)
            .targets(expectedTargetsSection)
            .build();

    assertEquals(expectedProjectView, projectView);
  }
}
