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
        "import path/to/file.bazelproject"
            + "\n"
            + "directories: "
            + ". "
            + "-excluded_dir1 "
            + "-excluded_dir2 "
            + "-excluded_dir3"
            + "\n"
            + "targets:\n"
            + "  //included_target1:test1\n"
            + "  -//excluded_target1:test1\n"
            + "  //included_target1:test2\n"
            + "\n"
            + "workspace_type: not_parsed\n"
            + "\n"
            + "build_flags:\n"
            + "  --not_parsed_flag\n"
            + "\n"
            + "test_sources:\n"
            + "  *test/not/parsed1/*\n"
            + "  *test/not/parsed2/*\n"
            + "\n";

    List<ProjectViewRawSection> result = ProjectViewSectionSplitter.split(fileContent);
    List<ProjectViewRawSection> expectedResult =
        ImmutableList.of(
            new ProjectViewRawSection("import", " path/to/file.bazelproject\n"),
            new ProjectViewRawSection(
                "directories", " . -excluded_dir1 -excluded_dir2 -excluded_dir3\n"),
            new ProjectViewRawSection(
                "targets",
                "\n"
                    + "  //included_target1:test1\n"
                    + "  -//excluded_target1:test1\n"
                    + "  //included_target1:test2\n"
                    + "\n"),
            new ProjectViewRawSection("workspace_type", " not_parsed\n\n"),
            new ProjectViewRawSection("build_flags", "\n  --not_parsed_flag\n\n"),
            new ProjectViewRawSection(
                "test_sources", "\n  *test/not/parsed1/*\n  *test/not/parsed2/*\n\n"));

    assertEquals(expectedResult, result);
  }
}
