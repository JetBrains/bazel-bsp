package org.jetbrains.bsp.bazel.projectview.parser.sections.specific;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.jetbrains.bsp.bazel.projectview.model.sections.specific.TargetsSection;
import org.jetbrains.bsp.bazel.projectview.parser.sections.ProjectViewSectionParser;
import org.junit.Before;
import org.junit.Test;

public class TargetsSectionParserTest {

  private ProjectViewSectionParser<TargetsSection> parser;

  @Before
  public void before() {
    this.parser = new TargetsSectionParser();
  }

  @Test
  public void shouldRecognizeSectionHeader() {
    String directoriesSectionHeader = "targets";

    assertTrue(parser.isSectionParsable(directoriesSectionHeader));
  }

  @Test
  public void shouldNotRecognizeInvalidSectionHeader() {
    String directoriesSectionHeader = "invalid_header";

    assertFalse(parser.isSectionParsable(directoriesSectionHeader));
  }

  @Test
  public void shouldParseIncludedTargets() {
    String entryBody =
        "  //test_included1:test1\n  //:test_included1:test2\n  //:test_included2:test1\n\n";

    TargetsSection section = parser.parse(entryBody);

    List<String> expectedIncludedTargets =
        ImmutableList.of(
            "//test_included1:test1", "//:test_included1:test2", "//:test_included2:test1");
    List<String> expectedExcludedTargets = ImmutableList.of();

    assertEquals(expectedIncludedTargets, section.getIncludedTargets());
    assertEquals(expectedExcludedTargets, section.getExcludedTargets());
  }

  @Test
  public void shouldParseExcludedTargets() {
    String entryBody =
        "  -//test_excluded1:test1\n  -//test_excluded1:test2\n  -//test_excluded2:test1\n\n";

    TargetsSection section = parser.parse(entryBody);

    List<String> expectedIncludedTargets = ImmutableList.of();
    List<String> expectedExcludedTargets =
        ImmutableList.of(
            "//test_excluded1:test1", "//test_excluded1:test2", "//test_excluded2:test1");

    assertEquals(expectedIncludedTargets, section.getIncludedTargets());
    assertEquals(expectedExcludedTargets, section.getExcludedTargets());
  }

  @Test
  public void shouldParseIncludedAndExcludedTargets() {
    String entryBody =
        "  -//test_excluded1:test1\n  //test_included1:test1\n  -//test_excluded1:test2\n\n";

    TargetsSection section = parser.parse(entryBody);

    List<String> expectedIncludedTargets = ImmutableList.of("//test_included1:test1");
    List<String> expectedExcludedTargets =
        ImmutableList.of("//test_excluded1:test1", "//test_excluded1:test2");

    assertEquals(expectedIncludedTargets, section.getIncludedTargets());
    assertEquals(expectedExcludedTargets, section.getExcludedTargets());
  }
}
