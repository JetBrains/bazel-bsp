package org.jetbrains.bsp.bazel.projectview.parser.sections;

import com.google.common.base.Splitter;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewSection;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewSectionHeader;

import java.util.List;
import java.util.regex.Pattern;

public abstract class ProjectViewSectionParser<T extends ProjectViewSection> {

  private final ProjectViewSectionHeader sectionHeader;

  protected ProjectViewSectionParser(ProjectViewSectionHeader sectionHeader) {
    this.sectionHeader = sectionHeader;
  }

  public abstract T parse(String sectionBody);

  public boolean isSectionParsable(String sectionHeader) {
    return sectionHeader.equals(this.sectionHeader.toString());
  }

  protected List<String> splitSectionEntries(String sectionBody) {
    return Splitter.on(Pattern.compile("[ \n]+")).omitEmptyStrings().splitToList(sectionBody);
  }
}
