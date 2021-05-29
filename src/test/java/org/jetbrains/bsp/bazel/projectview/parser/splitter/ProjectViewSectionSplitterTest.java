package org.jetbrains.bsp.bazel.projectview.parser.splitter;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.junit.Test;

public class ProjectViewSectionSplitterTest {

  @Test
  public void shouldParseEmptyFile() {
    String emptyContent = "";

    List<ProjectViewRawSection> result = ProjectViewSectionSplitter.split(emptyContent);
    List<ProjectViewRawSection> expectedResult = ImmutableList.of();

    assertEquals(expectedResult, result);
  }

  @Test
  public void shouldParseRegularFile() {
    String fileContent =
        "directories: "
            + ". "
            + "-ijwb "
            + "-plugin_dev "
            + "-clwb "
            + "\n"
            + "targets:\n"
            + "  //aswb:aswb_bazel_dev\n"
            + "  //:aswb_tests\n"
            + "  //:aswb_python_tests\n"
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

    List<ProjectViewRawSection> result = ProjectViewSectionSplitter.split(fileContent);
    List<ProjectViewRawSection> expectedResult =
        ImmutableList.of(
            new ProjectViewRawSection("directories", " . -ijwb -plugin_dev -clwb \n"),
            new ProjectViewRawSection(
                "targets",
                "\n"
                    + "  //aswb:aswb_bazel_dev\n"
                    + "  //:aswb_tests\n"
                    + "  //:aswb_python_tests\n"
                    + "\n"),
            new ProjectViewRawSection("workspace_type", " intellij_plugin\n\n"),
            new ProjectViewRawSection(
                "build_flags", "\n  --define=ij_product=android-studio-latest\n\n"),
            new ProjectViewRawSection(
                "test_sources",
                "\n"
                    + "  */tests/unittests*\n"
                    + "  */tests/integrationtests*\n"
                    + "  */tests/utils/integration*\n"
                    + "  */testcompat/unittests*\n"
                    + "  */testcompat/integrationtests*\n"
                    + "  */testcompat/utils/integration*\n"
                    + "\n"));

    assertEquals(expectedResult, result);
  }
}
