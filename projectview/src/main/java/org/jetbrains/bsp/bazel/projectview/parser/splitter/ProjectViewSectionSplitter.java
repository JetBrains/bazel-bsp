package org.jetbrains.bsp.bazel.projectview.parser.splitter;

import com.google.common.collect.Streams;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ProjectViewSectionSplitter {

  private static final Pattern SECTION_HEADER_REGEX =
      Pattern.compile("((^[^:\\s]+)([: ]))", Pattern.MULTILINE);
  private static final int SECTION_HEADER_NAME_GROUP_ID = 2;

  public static ProjectViewRawSections split(String fileContent) {
    List<ProjectViewRawSection> rawSections = findRawSections(fileContent);

    return new ProjectViewRawSections(rawSections);
  }

  private static List<ProjectViewRawSection> findRawSections(String fileContent) {
    List<String> sectionHeadersNames = findSectionsHeadersNames(fileContent);
    Stream<String> sectionBodies = findSectionsBodiesAndSkipFirstEmptyEntry(fileContent);

    return Streams.zip(sectionHeadersNames.stream(), sectionBodies, ProjectViewRawSection::new)
        .collect(Collectors.toList());
  }

  private static List<String> findSectionsHeadersNames(String fileContent) {
    Matcher matcher = SECTION_HEADER_REGEX.matcher(fileContent);
    ArrayList<String> result = new ArrayList<>();

    while (matcher.find()) {
      result.add(matcher.group(SECTION_HEADER_NAME_GROUP_ID));
    }

    return result;
  }

  private static Stream<String> findSectionsBodiesAndSkipFirstEmptyEntry(String fileContent) {
    return SECTION_HEADER_REGEX.splitAsStream(fileContent).skip(1);
  }
}
