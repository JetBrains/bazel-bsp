package org.jetbrains.bsp.bazel.projectview.parser;

import io.vavr.control.Try;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.bsp.bazel.projectview.model.ProjectView;
import org.jetbrains.bsp.bazel.projectview.parser.splitter.ProjectViewRawSection;

import java.io.File;
import java.util.List;
import java.util.Optional;

public class ProjectImportParser {

  private static final Logger LOGGER = LogManager.getLogger(ProjectImportParser.class);

  private static final String IMPORT_SECTION_HEADER = "import";

  private static final ProjectViewParser PROJECT_VIEW_PARSER = new ProjectViewParserImpl();

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

  private File getProjectViewFile(ProjectViewRawSection rawSection) {
    String projectViewFilePath = rawSection.getSectionBody().trim();

    return new File(projectViewFilePath);
  }

  private Optional<ProjectView> parseFile(File projectViewFile) {
    return Try.success(projectViewFile)
        .mapTry(PROJECT_VIEW_PARSER::parse)
        .onFailure(LOGGER::error)
        .toJavaOptional();
  }
}
