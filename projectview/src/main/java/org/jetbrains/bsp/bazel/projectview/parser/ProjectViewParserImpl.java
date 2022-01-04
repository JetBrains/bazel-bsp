package org.jetbrains.bsp.bazel.projectview.parser;

import org.jetbrains.bsp.bazel.projectview.model.ProjectView;
import org.jetbrains.bsp.bazel.projectview.parser.sections.ProjectViewTargetsSectionParser;
import org.jetbrains.bsp.bazel.projectview.parser.splitter.ProjectViewRawSection;
import org.jetbrains.bsp.bazel.projectview.parser.splitter.ProjectViewRawSections;
import org.jetbrains.bsp.bazel.projectview.parser.splitter.ProjectViewSectionSplitter;

import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

// TODO

/**
 * Our default implementation of ProjectViewParser
 *
 * <p>Logic schema:
 *
 * <p>1. extracting blocks: <section header>: <included section value 1> <included section value 2>
 * -<excluded section value 1> -<excluded section value 2>
 *
 * <p>or: <section header>: <section value> `
 *
 * <p>2. looping through extracted and checking which block could be parsed by the given section
 * parser
 *
 * <p>3. applying section specific parser to the chosen section
 *
 * @see org.jetbrains.bsp.bazel.projectview.parser.ProjectViewParser
 * @see org.jetbrains.bsp.bazel.projectview.parser.splitter.ProjectViewSectionSplitter
 */
class ProjectViewParserImpl implements ProjectViewParser {

  private static final String IMPORT_STATEMENT = "import";

  private static final ProjectViewTargetsSectionParser TARGETS_PARSER =
      new ProjectViewTargetsSectionParser();

  @Override
  public ProjectView parse(String projectViewFileContent, String defaultProjectViewFileContent) {
    ProjectView defaultProjectView = parse(defaultProjectViewFileContent);
    ProjectViewRawSections rawSections = ProjectViewSectionSplitter.split(projectViewFileContent);

    return ProjectView.builder()
        .imports(findImportedProjectViews(rawSections))
        .targets(TARGETS_PARSER.parseOrDefault(rawSections, defaultProjectView.getTargets()))
        .build();
  }

  @Override
  public ProjectView parse(String projectViewFileContent) {
    ProjectViewRawSections rawSections = ProjectViewSectionSplitter.split(projectViewFileContent);

    return ProjectView.builder()
        .imports(findImportedProjectViews(rawSections))
        .targets(TARGETS_PARSER.parse(rawSections))
        .build();
  }

  private List<ProjectView> findImportedProjectViews(ProjectViewRawSections rawSections) {
    return rawSections.getAllWithName(IMPORT_STATEMENT).stream()
        .map(ProjectViewRawSection::getSectionBody)
        .map(String::trim)
        .map(Paths::get)
        .map(this::parse)
        .collect(Collectors.toList());
  }
}
