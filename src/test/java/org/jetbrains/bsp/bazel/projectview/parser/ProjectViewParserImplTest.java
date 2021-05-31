package org.jetbrains.bsp.bazel.projectview.parser;

import static org.junit.Assert.assertEquals;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.io.CharStreams;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
    this.parser = new ProjectViewParserImpl();
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowMissingDirectoriesSection() throws IOException {
    String projectViewFileContent = loadFileFromResources("projectViewWithoutDirectories.bazelproject");

    parser.parse(projectViewFileContent);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowMissingTargetsSection() throws IOException {
    String projectViewFileContent =
        loadFileFromResources("projectViewWithoutTargets.bazelproject");

    parser.parse(projectViewFileContent);
  }

  @Test
  public void shouldParseSections() throws IOException {
    String projectViewFileContent = loadFileFromResources("projectView.bazelproject");

    ProjectView projectView = parser.parse(projectViewFileContent);

    DirectoriesSection expectedDirectoriesSection =
        new DirectoriesSection(
            ImmutableList.of(Paths.get(".")),
            ImmutableList.of(Paths.get("ijwb"), Paths.get("plugin_dev"), Paths.get("clwb")));
    TargetsSection expectedTargetsSection =
        new TargetsSection(
            ImmutableList.of("//aswb:aswb_bazel_dev", "//:aswb_python_tests"),
            ImmutableList.of("//:aswb_tests"));

    assertEquals(expectedDirectoriesSection, projectView.getDirectories());
    assertEquals(expectedTargetsSection, projectView.getTargets());
  }

  private String loadFileFromResources(String fileName) throws IOException {
    String filePath = String.format("/projectview/%s", fileName);
    InputStream inputStream = ProjectViewParserImplTest.class.getResourceAsStream(filePath);

    // we read file content instead of passing plain file due to bazel resources packaging
    return CharStreams.toString(new InputStreamReader(inputStream, Charsets.UTF_8));
  }
}
