package org.jetbrains.bsp.bazel.projectview.parser.sections.specific;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.jetbrains.bsp.bazel.projectview.model.sections.specific.DirectoriesSection;
import org.jetbrains.bsp.bazel.projectview.parser.sections.ProjectViewSectionParser;

public class DirectoriesSectionParserTest {

  private ProjectViewSectionParser<DirectoriesSection> parser;

  @Before
  public void before() {
    this.parser = new DirectoriesSectionParser();
  }

  @Test
  public void shouldRecognizeSectionHeader() {
    String directoriesSectionHeader = "directories";

    assertTrue(parser.isSectionParsable(directoriesSectionHeader));
  }

  @Test
  public void shouldNotRecognizeInvalidSectionHeader() {
    String directoriesSectionHeader = "invalid_header";

    assertFalse(parser.isSectionParsable(directoriesSectionHeader));
  }

  @Test
  public void shouldParseIncludedDirectories() {
    String entryBody = "ijwb plugin_dev clwb \n";

    DirectoriesSection section = parser.parse(entryBody);

    List<Path> expectedIncludedPaths =
        ImmutableList.of(Paths.get("ijwb"), Paths.get("plugin_dev"), Paths.get("clwb"));
    List<Path> expectedExcludedPaths = ImmutableList.of();

    assertEquals(expectedIncludedPaths, section.getIncludedDirectories());
    assertEquals(expectedExcludedPaths, section.getExcludedDirectories());
  }

  @Test
  public void shouldParseExcludedDirectories() {
    String entryBody = "-ijwb -plugin_dev -clwb \n";

    DirectoriesSection section = parser.parse(entryBody);

    List<Path> expectedIncludedPaths = ImmutableList.of();
    List<Path> expectedExcludedPaths =
        ImmutableList.of(Paths.get("ijwb"), Paths.get("plugin_dev"), Paths.get("clwb"));

    assertEquals(expectedIncludedPaths, section.getIncludedDirectories());
    assertEquals(expectedExcludedPaths, section.getExcludedDirectories());
  }

  @Test
  public void shouldParseIncludedAndExcludedDirectories() {
    String entryBody = "ijwb -plugin_dev clwb \n-test\n";

    DirectoriesSection section = parser.parse(entryBody);

    List<Path> expectedIncludedPaths = ImmutableList.of(Paths.get("ijwb"), Paths.get("clwb"));
    List<Path> expectedExcludedPaths = ImmutableList.of(Paths.get("plugin_dev"), Paths.get("test"));

    assertEquals(expectedIncludedPaths, section.getIncludedDirectories());
    assertEquals(expectedExcludedPaths, section.getExcludedDirectories());
  }
}
