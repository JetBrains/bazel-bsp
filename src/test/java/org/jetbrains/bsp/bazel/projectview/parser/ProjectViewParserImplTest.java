package org.jetbrains.bsp.bazel.projectview.parser;

import com.google.common.collect.ImmutableList;
import org.jetbrains.bsp.bazel.projectview.model.ProjectView;
import org.jetbrains.bsp.bazel.projectview.model.sections.specific.DirectoriesSection;
import org.jetbrains.bsp.bazel.projectview.model.sections.specific.TargetsSection;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class ProjectViewParserImplTest {

  private ProjectViewParser parser;

  @Before
  public void before() {
    this.parser = new ProjectViewParserImpl();
  }

  @Test
  public void shouldParseDirectoriesSection() {
    String fileContentWithDirectories =
        "directories: "
            + ". "
            + "-ijwb "
            + "-plugin_dev "
            + "-clwb "
            + "\n"
            + "workspace_type: intellij_plugin\n"
            + "\n"
            + "build_flags:\n"
            + "  --define=ij_product=android-studio-latest\n"
            + "\n"
            + "test_sources:\n"
            + "  */tests/unittests*\n"
            + "  */tests/integrationtests*\n"
            + "  */tests/utils/integration*\n"
            + "  */testcompat/unittests*\n"
            + "  */testcompat/integrationtests*\n"
            + "  */testcompat/utils/integration*\n"
            + "\n";

    ProjectView projectView = parser.parse(fileContentWithDirectories);

    DirectoriesSection expectedDirectoriesSection =
        new DirectoriesSection(
            ImmutableList.of(Paths.get(".")),
            ImmutableList.of(Paths.get("ijwb"), Paths.get("plugin_dev"), Paths.get("clwb")));

    assertEquals(Optional.of(expectedDirectoriesSection), projectView.getDirectories());
    assertEquals(Optional.empty(), projectView.getTargets());
  }

  @Test
  public void shouldParseTargetsSection() {
    String fileContentWithTargets =
        "workspace_type: intellij_plugin\n"
            + "\n"
            + "build_flags:\n"
            + "  --define=ij_product=android-studio-latest\n"
            + "\n"
            + "targets:\n"
            + "  //aswb:aswb_bazel_dev\n"
            + "  -//:aswb_tests\n"
            + "  //:aswb_python_tests\n"
            + "\n"
            + "test_sources:\n"
            + "  */tests/unittests*\n"
            + "  */tests/integrationtests*\n"
            + "  */tests/utils/integration*\n"
            + "  */testcompat/unittests*\n"
            + "  */testcompat/integrationtests*\n"
            + "  */testcompat/utils/integration*\n"
            + "\n";

    ProjectView projectView = parser.parse(fileContentWithTargets);

    TargetsSection expectedTargetsSection =
        new TargetsSection(
            ImmutableList.of("//aswb:aswb_bazel_dev", "//:aswb_python_tests"),
            ImmutableList.of("//:aswb_tests"));

    assertEquals(Optional.empty(), projectView.getDirectories());
    assertEquals(Optional.of(expectedTargetsSection), projectView.getTargets());
  }

  @Test
  public void shouldParseSections() {
    String fileContent =
        "directories: "
            + ". "
            + "-ijwb "
            + "-plugin_dev "
            + "-clwb "
            + "\n"
            + "workspace_type: intellij_plugin\n"
            + "\n"
            + "targets:\n"
            + "  //aswb:aswb_bazel_dev\n"
            + "  -//:aswb_tests\n"
            + "  //:aswb_python_tests\n"
            + "\n"
            + "build_flags:\n"
            + "  --define=ij_product=android-studio-latest\n"
            + "\n"
            + "test_sources:\n"
            + "  */tests/unittests*\n"
            + "  */tests/integrationtests*\n"
            + "  */tests/utils/integration*\n"
            + "  */testcompat/unittests*\n"
            + "  */testcompat/integrationtests*\n"
            + "  */testcompat/utils/integration*\n"
            + "\n";

    ProjectView projectView = parser.parse(fileContent);

    DirectoriesSection expectedDirectoriesSection =
        new DirectoriesSection(
            ImmutableList.of(Paths.get(".")),
            ImmutableList.of(Paths.get("ijwb"), Paths.get("plugin_dev"), Paths.get("clwb")));
    TargetsSection expectedTargetsSection =
        new TargetsSection(
            ImmutableList.of("//aswb:aswb_bazel_dev", "//:aswb_python_tests"),
            ImmutableList.of("//:aswb_tests"));

    assertEquals(Optional.of(expectedDirectoriesSection), projectView.getDirectories());
    assertEquals(Optional.of(expectedTargetsSection), projectView.getTargets());
  }
}
