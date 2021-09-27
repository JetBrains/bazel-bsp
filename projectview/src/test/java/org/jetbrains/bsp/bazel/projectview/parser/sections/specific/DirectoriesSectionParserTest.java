package org.jetbrains.bsp.bazel.projectview.parser.sections.specific;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.jetbrains.bsp.bazel.projectview.model.sections.specific.DirectoriesSection;
import org.jetbrains.bsp.bazel.projectview.parser.sections.ProjectViewSectionParser;
import org.junit.Before;
import org.junit.Test;

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
    String entryBody = "test_included1 test_included2 test_included3 \n";

    DirectoriesSection section = parser.parse(entryBody);

    List<Path> expectedIncludedPaths =
        ImmutableList.of(
            Paths.get("test_included1"), Paths.get("test_included2"), Paths.get("test_included3"));
    List<Path> expectedExcludedPaths = ImmutableList.of();

    assertEquals(expectedIncludedPaths, section.getIncludedDirectories());
    assertEquals(expectedExcludedPaths, section.getExcludedDirectories());
  }

  @Test
  public void shouldParseExcludedDirectories() {
    String entryBody = "-test_excluded1 -test_excluded2 -test_excluded3 \n";

    DirectoriesSection section = parser.parse(entryBody);

    List<Path> expectedIncludedPaths = ImmutableList.of();
    List<Path> expectedExcludedPaths =
        ImmutableList.of(
            Paths.get("test_excluded1"), Paths.get("test_excluded2"), Paths.get("test_excluded3"));

    assertEquals(expectedIncludedPaths, section.getIncludedDirectories());
    assertEquals(expectedExcludedPaths, section.getExcludedDirectories());
  }

  @Test
  public void shouldParseIncludedAndExcludedDirectories() {
    String entryBody = "test_included1 -test_excluded1 test_included2 \n-test_excluded2\n";

    DirectoriesSection section = parser.parse(entryBody);

    List<Path> expectedIncludedPaths =
        ImmutableList.of(Paths.get("test_included1"), Paths.get("test_included2"));
    List<Path> expectedExcludedPaths =
        ImmutableList.of(Paths.get("test_excluded1"), Paths.get("test_excluded2"));

    assertEquals(expectedIncludedPaths, section.getIncludedDirectories());
    assertEquals(expectedExcludedPaths, section.getExcludedDirectories());
  }
}
