package org.jetbrains.bsp.bazel.projectview.parser.sections;

import com.google.common.base.Splitter;
import java.util.List;
import java.util.regex.Pattern;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewSection;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewSectionHeader;

/**
 * Project view file section parser, implementation should parse raw string body into given section
 * class
 *
 * @param <T> project view section which can be parsed with implementation
 */
public abstract class ProjectViewSectionParser<T extends ProjectViewSection> {

  private final ProjectViewSectionHeader sectionHeader;

  protected ProjectViewSectionParser(ProjectViewSectionHeader sectionHeader) {
    this.sectionHeader = sectionHeader;
  }

  public abstract T parse(String sectionBody);

  public boolean isSectionParsable(String sectionHeader) {
    return sectionHeader.equals(this.sectionHeader.toString());
  }

  public String sectionName() {
    return sectionHeader.toString();
  }

  protected List<String> splitSectionEntries(String sectionBody) {
    return Splitter.on(Pattern.compile("[ \n]+")).omitEmptyStrings().splitToList(sectionBody);
  }
}
