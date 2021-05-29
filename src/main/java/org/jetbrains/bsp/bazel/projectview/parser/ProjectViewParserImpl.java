package org.jetbrains.bsp.bazel.projectview.parser;

import java.util.List;
import org.jetbrains.bsp.bazel.projectview.model.ProjectView;
import org.jetbrains.bsp.bazel.projectview.model.sections.specific.DirectoriesSection;
import org.jetbrains.bsp.bazel.projectview.model.sections.specific.TargetsSection;
import org.jetbrains.bsp.bazel.projectview.parser.sections.specific.DirectoriesSectionParser;
import org.jetbrains.bsp.bazel.projectview.parser.sections.specific.TargetsSectionParser;
import org.jetbrains.bsp.bazel.projectview.parser.splitter.ProjectViewRawSection;
import org.jetbrains.bsp.bazel.projectview.parser.splitter.ProjectViewSectionSplitter;

//@formatter:off
/**
 * Our default implementation of ProjectViewParser
 *
 * <p>Logic schema:
 *
 * <p>Logic schema: 1. extracting blocks: <section header>: <included section value 1> <included 119
 * + * section value 2> - <excluded section value 1> - <excluded section value 2> or: <section
 * header>: 120 + * <section value> or: <section header>: <section value>
 *
 * <p>2. looping through extracted and checking which block could be parsed by the given section
 * parser
 *
 * <p>3. applying section specific parser to the chosen section
 *
 * @see org.jetbrains.bsp.bazel.projectview.parser.ProjectViewParser
 * @see org.jetbrains.bsp.bazel.projectview.parser.splitter.ProjectViewSectionSplitter
 * @see org.jetbrains.bsp.bazel.projectview.parser.sections.ProjectViewSectionParser
 */
class ProjectViewParserImpl implements ProjectViewParser {

  private static final ProjectViewRawSectionParser<DirectoriesSection> DIRECTORY_PARSER =
      ProjectViewRawSectionParser.forParser(new DirectoriesSectionParser());

  private static final ProjectViewRawSectionParser<TargetsSection> TARGETS_PARSER =
      ProjectViewRawSectionParser.forParser(new TargetsSectionParser());

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
