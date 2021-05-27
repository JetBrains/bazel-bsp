package org.jetbrains.bsp.bazel.projectview.parser.splitter;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ProjectViewSectionSplitter {

  private static final String SECTION_HEADER_REGEX = "(^[a-z_]+[:]?)";
  private static final String POSSIBLE_HEADER_ENDING_CHARACTER = ":";

  public static List<ProjectViewRawSection> split(String fileContent) {
    List<ProjectViewSectionHeaderPosition> sectionsHeadersPositions =
        splitIntoSectionsHeadersPositions(fileContent);

    return parseRawSections(fileContent, sectionsHeadersPositions);
  }

  private static List<ProjectViewSectionHeaderPosition> splitIntoSectionsHeadersPositions(
      String fileContent) {
    Pattern pattern = Pattern.compile(SECTION_HEADER_REGEX, Pattern.MULTILINE);
    Matcher matcher = pattern.matcher(fileContent);

    return getStartingAndEndingIndexes(matcher);
  }

  private static List<ProjectViewSectionHeaderPosition> getStartingAndEndingIndexes(
      Matcher matcher) {
    ArrayList<ProjectViewSectionHeaderPosition> result = new ArrayList<>();

    while (matcher.find()) {
      ProjectViewSectionHeaderPosition position =
          new ProjectViewSectionHeaderPosition(matcher.start(), matcher.end(), matcher.group());

      result.add(position);
    }

    return result;
  }

  private static List<ProjectViewRawSection> parseRawSections(
      String fileContent, List<ProjectViewSectionHeaderPosition> sectionsHeadersPositions) {
    ListIterator<ProjectViewSectionHeaderPosition> iterator =
        sectionsHeadersPositions.listIterator();
    List<ProjectViewRawSection> result = new ArrayList<>();

    while (iterator.hasNext()) {
      ProjectViewSectionHeaderPosition currentPosition = iterator.next();
      Optional<ProjectViewSectionHeaderPosition> nextPosition = getNextPosition(iterator);

      ProjectViewRawSection rawSection =
          parseProjectViewRawSection(currentPosition, nextPosition, fileContent);
      result.add(rawSection);
    }

    return result;
  }

  private static Optional<ProjectViewSectionHeaderPosition> getNextPosition(
      ListIterator<ProjectViewSectionHeaderPosition> iterator) {
    if (iterator.hasNext()) {
      ProjectViewSectionHeaderPosition nextPosition = iterator.next();
      iterator.previous();
      return Optional.of(nextPosition);
    }

    return Optional.empty();
  }

  private static ProjectViewRawSection parseProjectViewRawSection(
      ProjectViewSectionHeaderPosition currentPosition,
      Optional<ProjectViewSectionHeaderPosition> nextPosition,
      String fileContent) {

    String sectionBody =
        nextPosition
            .map(ProjectViewSectionHeaderPosition::getStartIndex)
            .map(startIndex -> fileContent.substring(currentPosition.getEndIndex(), startIndex))
            .orElse(fileContent.substring(currentPosition.getEndIndex()));
    String sectionHeader = removeEndingHeaderColonAndSpaces(currentPosition.getHeader());

    return new ProjectViewRawSection(sectionHeader, sectionBody);
  }

  private static String removeEndingHeaderColonAndSpaces(String header) {
    return header.replace(POSSIBLE_HEADER_ENDING_CHARACTER, "").trim();
  }
}
