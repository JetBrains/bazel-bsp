package org.jetbrains.bsp.bazel.projectview.parser.sections.specific;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import java.util.List;
import org.jetbrains.bsp.bazel.projectview.model.sections.specific.TargetsSection;
import org.jetbrains.bsp.bazel.projectview.parser.sections.ProjectViewSectionParser;

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
    String entryBody = "  //aswb:aswb_bazel_dev\n  //:aswb_tests\n  //:aswb_python_tests\n\n";

    TargetsSection section = parser.parse(entryBody);

    List<String> expectedIncludedTargets =
        ImmutableList.of("//aswb:aswb_bazel_dev", "//:aswb_tests", "//:aswb_python_tests");
    List<String> expectedExcludedTargets = ImmutableList.of();

    assertEquals(expectedIncludedTargets, section.getIncludedTargets());
    assertEquals(expectedExcludedTargets, section.getExcludedTargets());
  }

  @Test
  public void shouldParseExcludedTargets() {
    String entryBody = "  -//aswb:aswb_bazel_dev\n  -//:aswb_tests\n  -//:aswb_python_tests\n\n";

    TargetsSection section = parser.parse(entryBody);

    List<String> expectedIncludedTargets = ImmutableList.of();
    List<String> expectedExcludedTargets =
        ImmutableList.of("//aswb:aswb_bazel_dev", "//:aswb_tests", "//:aswb_python_tests");

    assertEquals(expectedIncludedTargets, section.getIncludedTargets());
    assertEquals(expectedExcludedTargets, section.getExcludedTargets());
  }

  @Test
  public void shouldParseIncludedAndExcludedTargets() {
    String entryBody = "  -//aswb:aswb_bazel_dev\n  //:aswb_tests\n  -//:aswb_python_tests\n\n";

    TargetsSection section = parser.parse(entryBody);

    List<String> expectedIncludedTargets = ImmutableList.of("//:aswb_tests");
    List<String> expectedExcludedTargets =
        ImmutableList.of("//aswb:aswb_bazel_dev", "//:aswb_python_tests");

    assertEquals(expectedIncludedTargets, section.getIncludedTargets());
    assertEquals(expectedExcludedTargets, section.getExcludedTargets());
  }
}
