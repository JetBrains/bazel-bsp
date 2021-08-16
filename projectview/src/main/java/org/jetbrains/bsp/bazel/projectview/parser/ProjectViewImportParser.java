package org.jetbrains.bsp.bazel.projectview.parser;

import io.vavr.control.Try;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.bsp.bazel.projectview.model.ProjectView;
import org.jetbrains.bsp.bazel.projectview.parser.splitter.ProjectViewRawSection;

public class ProjectViewImportParser {

  private static final Logger LOGGER = LogManager.getLogger(ProjectViewImportParser.class);

  private static final String IMPORT_SECTION_HEADER = "import";

  private final ProjectViewParser projectViewParser;

  public ProjectViewImportParser(ProjectViewParser projectViewParser) {
    this.projectViewParser = projectViewParser;
  }

  public Optional<ProjectView> parseRawSections(List<ProjectViewRawSection> rawSections) {
    return rawSections.stream()
        .filter(this::isImportSection)
        .findFirst()
        .map(this::getProjectViewFile)
        .flatMap(this::parseFile);
  }

  private boolean isImportSection(ProjectViewRawSection rawSection) {
    return rawSection.getSectionHeader().equals(IMPORT_SECTION_HEADER);
  }

  private Path getProjectViewFile(ProjectViewRawSection rawSection) {
    String projectViewFilePath = rawSection.getSectionBody().trim();

    return Paths.get(projectViewFilePath);
  }

  private Optional<ProjectView> parseFile(Path projectViewPath) {
    return Try.success(projectViewPath)
        .mapTry(projectViewParser::parse)
        .onFailure(LOGGER::error)
        .toJavaOptional();
  }
}
