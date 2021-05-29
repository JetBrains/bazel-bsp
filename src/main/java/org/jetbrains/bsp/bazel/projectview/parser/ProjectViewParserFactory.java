package org.jetbrains.bsp.bazel.projectview.parser;

public final class ProjectViewParserFactory {

  public static ProjectViewParser getBasic() {
    return new ProjectViewParserImpl();
  }
}
