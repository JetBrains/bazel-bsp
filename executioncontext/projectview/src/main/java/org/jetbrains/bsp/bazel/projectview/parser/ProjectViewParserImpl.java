package org.jetbrains.bsp.bazel.projectview.parser;

import io.vavr.collection.List;
import io.vavr.control.Try;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.bsp.bazel.commons.BetterFiles;
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
public class ProjectViewParserImpl implements ProjectViewParser {

  private static final Logger log = LogManager.getLogger(ProjectViewParserImpl.class);

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
  public Try<ProjectView> parse(Path projectViewFilePath, Path defaultProjectViewFilePath) {
    log.info(
        "Parsing project view from {} with default from {}.",
        projectViewFilePath,
        defaultProjectViewFilePath);

    return BetterFiles.tryReadFileContent(defaultProjectViewFilePath)
        .onFailure(
            exception ->
                log.error(
                    "Failed to read default file {}. Parsing failed!",
                    defaultProjectViewFilePath,
                    exception))
        .flatMap(
            defaultProjectViewFileContent ->
                parseWithDefault(projectViewFilePath, defaultProjectViewFileContent))
        .onSuccess(
            projectView ->
                log.info(
                    "Project view from {} with default from {} parsed!\n{}",
                    projectViewFilePath,
                    defaultProjectViewFilePath,
                    projectView))
        .onFailure(
            exception ->
                log.error(
                    "Failed to parse default file {}. Parsing failed!",
                    defaultProjectViewFilePath,
                    exception));
  }

  private Try<ProjectView> parseWithDefault(
      Path projectViewFilePath, String defaultProjectViewFileContent) {
    return BetterFiles.tryReadFileContent(projectViewFilePath)
        .onFailure(
            exception ->
                log.info("Failed to read file {}. Parsing default file.", projectViewFilePath))
        .flatMap(
            projectViewFilePathContent ->
                parse(projectViewFilePathContent, defaultProjectViewFileContent))
        .onFailure(
            exception ->
                log.info("Failed to parse file {}. Parsing default file.", projectViewFilePath))
        .orElse(() -> parse(defaultProjectViewFileContent))
        .onFailure(
            exception -> log.error("Failed to parse default file. Parsing failed!", exception));
  }

  @Override
  public Try<ProjectView> parse(
      String projectViewFileContent, String defaultProjectViewFileContent) {
    log.debug(
        "Parsing project view for the content:\n'{}'\n\nand the default content:\n'{}'.",
        projectViewFileContent,
        defaultProjectViewFileContent);

    return parse(defaultProjectViewFileContent)
        .onFailure(
            exception -> log.error("Failed to parse default content. Parsing failed!", exception))
        .flatMap(defaultProjectView -> parseWithDefault(projectViewFileContent, defaultProjectView))
        .onFailure(exception -> log.error("Failed to parse content. Parsing failed!", exception));
  }

  private Try<ProjectView> parseWithDefault(
      String projectViewFileContent, ProjectView defaultProjectView) {
    ProjectViewRawSections rawSections =
        ProjectViewSectionSplitter.getRawSectionsForFileContent(projectViewFileContent);

    log.debug("Parsing project view with default project view {}.", defaultProjectView);

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
  public Try<ProjectView> parse(Path projectViewFilePath) {
    log.info("Parsing project view from {}.", projectViewFilePath);

    return BetterFiles.tryReadFileContent(projectViewFilePath)
        .onFailure(
            exception ->
                log.error(
                    "Failed to read file {}. Parsing failed!", projectViewFilePath, exception))
        .flatMap(this::parse)
        .onSuccess(
            projectView ->
                log.info("Project view from {} parsed!\n{}", projectViewFilePath, projectView))
        .onFailure(
            exception ->
                log.error(
                    "Failed to parse file {}. Parsing failed!", projectViewFilePath, exception));
  }

  @Override
  public Try<ProjectView> parse(String projectViewFileContent) {
    log.debug("Parsing project view for the content:\n'{}'.", projectViewFileContent);

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
        .peek(path -> log.debug("Parsing imported file {}.", path))
        .map(this::parse);
  }
}
