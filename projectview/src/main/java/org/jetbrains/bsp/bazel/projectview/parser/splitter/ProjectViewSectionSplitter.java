package org.jetbrains.bsp.bazel.projectview.parser.splitter;

import com.google.common.collect.Streams;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ProjectViewSectionSplitter {

  private static final Pattern SECTION_HEADER_REGEX =
      Pattern.compile("((^[^:\\-/*\\s]+)([: ]))", Pattern.MULTILINE);
  private static final int SECTION_HEADER_NAME_GROUP_ID = 2;

  private static final String COMMENT_LINE_REGEX = "#(.)*(\\n|\\z)";
  private static final String COMMENT_LINE_REPLACEMENT = "\n";

  public static ProjectViewRawSections split(String fileContent) {
    var fileContentWithoutComments = removeLinesWithComments(fileContent);
    var rawSections = findRawSections(fileContentWithoutComments);

    return new ProjectViewRawSections(rawSections);
  }

  private static String removeLinesWithComments(String fileContent) {
    return fileContent.replaceAll(COMMENT_LINE_REGEX, COMMENT_LINE_REPLACEMENT);
  }

  private static List<ProjectViewRawSection> findRawSections(String fileContent) {
    var sectionHeadersNames = findSectionsHeadersNames(fileContent);
    var sectionBodies = findSectionsBodiesAndSkipFirstEmptyEntry(fileContent);

    return Streams.zip(sectionHeadersNames.stream(), sectionBodies, ProjectViewRawSection::new)
        .collect(Collectors.toList());
  }

  private static List<String> findSectionsHeadersNames(String fileContent) {
    var matcher = SECTION_HEADER_REGEX.matcher(fileContent);
    var result = new ArrayList<String>();

    while (matcher.find()) {
      result.add(matcher.group(SECTION_HEADER_NAME_GROUP_ID));
    }

    return result;
  }

  private static Stream<String> findSectionsBodiesAndSkipFirstEmptyEntry(String fileContent) {
    return SECTION_HEADER_REGEX.splitAsStream(fileContent).skip(1);
  }
}
