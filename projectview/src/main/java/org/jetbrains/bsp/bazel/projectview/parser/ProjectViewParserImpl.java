package org.jetbrains.bsp.bazel.projectview.parser;

import io.vavr.control.Try;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.bsp.bazel.projectview.model.ProjectView;
import org.jetbrains.bsp.bazel.projectview.parser.sections.ProjectViewBazelPathSectionParser;
import org.jetbrains.bsp.bazel.projectview.parser.sections.ProjectViewDebuggerAddressSectionParser;
import org.jetbrains.bsp.bazel.projectview.parser.sections.ProjectViewJavaPathSectionParser;
import org.jetbrains.bsp.bazel.projectview.parser.sections.ProjectViewTargetsSectionParser;
import org.jetbrains.bsp.bazel.projectview.parser.splitter.ProjectViewRawSection;
import org.jetbrains.bsp.bazel.projectview.parser.splitter.ProjectViewRawSections;
import org.jetbrains.bsp.bazel.projectview.parser.splitter.ProjectViewSectionSplitter;

/**
 * Default implementation of ProjectViewParser.
 *
 * @see org.jetbrains.bsp.bazel.projectview.parser.ProjectViewParser
 * @see org.jetbrains.bsp.bazel.projectview.parser.splitter.ProjectViewSectionSplitter
 */
class ProjectViewParserImpl implements ProjectViewParser {

  private static final String IMPORT_STATEMENT = "import";

  private static final ProjectViewTargetsSectionParser targetsParser =
      new ProjectViewTargetsSectionParser();

  private static final ProjectViewBazelPathSectionParser bazelPathParser =
      new ProjectViewBazelPathSectionParser();

  private static final ProjectViewDebuggerAddressSectionParser debuggerAddressParser =
      new ProjectViewDebuggerAddressSectionParser();

  private static final ProjectViewJavaPathSectionParser javaPathParser =
      new ProjectViewJavaPathSectionParser();

  @Override
  public Try<ProjectView> parse(
      String projectViewFileContent, String defaultProjectViewFileContent) {
    return parse(defaultProjectViewFileContent)
        .flatMap(
            defaultProjectView -> parseWithDefault(projectViewFileContent, defaultProjectView));
  }

  private Try<ProjectView> parseWithDefault(
      String projectViewFileContent, ProjectView defaultProjectView) {
    ProjectViewRawSections rawSections =
        ProjectViewSectionSplitter.getRawSectionsForFileContent(projectViewFileContent);

    return ProjectView.builder()
        .imports(findImportedProjectViews(rawSections))
        .targets(targetsParser.parseOrDefault(rawSections, defaultProjectView.getTargets()))
        .bazelPath(bazelPathParser.parseOrDefault(rawSections, defaultProjectView.getBazelPath()))
        .debuggerAddress(
            debuggerAddressParser.parseOrDefault(
                rawSections, defaultProjectView.getDebuggerAddress()))
        .javaPath(javaPathParser.parseOrDefault(rawSections, defaultProjectView.getJavaPath()))
        .build();
  }

  @Override
  public Try<ProjectView> parse(String projectViewFileContent) {
    ProjectViewRawSections rawSections =
        ProjectViewSectionSplitter.getRawSectionsForFileContent(projectViewFileContent);

    return ProjectView.builder()
        .imports(findImportedProjectViews(rawSections))
        .targets(targetsParser.parse(rawSections))
        .bazelPath(bazelPathParser.parse(rawSections))
        .debuggerAddress(debuggerAddressParser.parse(rawSections))
        .javaPath(javaPathParser.parse(rawSections))
        .build();
  }

  private List<Try<ProjectView>> findImportedProjectViews(ProjectViewRawSections rawSections) {
    return rawSections
        .getAllWithName(IMPORT_STATEMENT)
        .map(ProjectViewRawSection::getSectionBody)
        .map(String::trim)
        .map(Paths::get)
        .map(this::parse)
        .collect(Collectors.toList());
  }
}
