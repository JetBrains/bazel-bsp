package org.jetbrains.bsp.bazel.projectview.parser.sections.specific;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewSectionHeader;
import org.jetbrains.bsp.bazel.projectview.model.sections.specific.DirectoriesSection;
import org.jetbrains.bsp.bazel.projectview.parser.sections.ProjectViewListSectionParser;

public class DirectoriesSectionParser extends ProjectViewListSectionParser<DirectoriesSection> {

  public DirectoriesSectionParser() {
    super(ProjectViewSectionHeader.DIRECTORIES, true);
  }

  @Override
  public DirectoriesSection parse(String sectionBody) {
    List<String> includedEntries = parseIncludedEntries(sectionBody);
    List<Path> includedPaths = mapEntriesToPaths(includedEntries);

    List<String> excludedEntries = parseExcludedEntries(sectionBody);
    List<Path> excludedPaths = mapEntriesToPaths(excludedEntries);

    return new DirectoriesSection(includedPaths, excludedPaths);
  }

  private List<Path> mapEntriesToPaths(List<String> entries) {
    return entries.stream().map(Paths::get).collect(Collectors.toList());
  }
}
