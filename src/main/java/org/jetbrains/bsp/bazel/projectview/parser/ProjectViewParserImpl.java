package org.jetbrains.bsp.bazel.projectview.parser;

import com.google.common.io.Files;
import org.jetbrains.bsp.bazel.projectview.model.ProjectView;
import org.jetbrains.bsp.bazel.projectview.model.sections.specific.DirectoriesSection;
import org.jetbrains.bsp.bazel.projectview.model.sections.specific.TargetsSection;
import org.jetbrains.bsp.bazel.projectview.parser.sections.specific.DirectoriesSectionParser;
import org.jetbrains.bsp.bazel.projectview.parser.sections.specific.TargetsSectionParser;
import org.jetbrains.bsp.bazel.projectview.parser.splitter.ProjectViewRawSection;
import org.jetbrains.bsp.bazel.projectview.parser.splitter.ProjectViewSectionSplitter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

class ProjectViewParserImpl implements ProjectViewParser {

  private static final ProjectViewRawSectionParser<DirectoriesSection> DIRECTORY_PARSER =
      ProjectViewRawSectionParser.forParser(new DirectoriesSectionParser());

  private static final ProjectViewRawSectionParser<TargetsSection> TARGETS_PARSER =
      ProjectViewRawSectionParser.forParser(new TargetsSectionParser());

  @Override
  public ProjectView parse(File projectViewFile) throws IOException {
    String fileContent = Files.asCharSource(projectViewFile, Charset.defaultCharset()).read();

    return parse(fileContent);
  }

  @Override
  public ProjectView parse(String projectViewFileContent) {
    List<ProjectViewRawSection> rawSections =
        ProjectViewSectionSplitter.split(projectViewFileContent);

    return buildFile(rawSections);
  }

  private ProjectView buildFile(List<ProjectViewRawSection> rawSections) {
    return ProjectView.builder()
        .directories(DIRECTORY_PARSER.parseRawSections(rawSections))
        .targets(TARGETS_PARSER.parseRawSections(rawSections))
        .build();
  }
}
