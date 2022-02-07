package org.jetbrains.bsp.bazel.server.bsp.config;

import com.google.common.collect.ImmutableList;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import org.jetbrains.bsp.bazel.projectview.model.ProjectView;
import org.jetbrains.bsp.bazel.projectview.model.ProjectViewProvider;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBazelPathSection;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewTargetsSection;
import org.jetbrains.bsp.bazel.projectview.parser.ProjectViewDefaultParserProvider;

public class ServerArgsProjectViewProvider implements ProjectViewProvider {

  private final String pathToBazel;
  private final String targets;

  private final ProjectViewDefaultParserProvider defaultParserProvider;

  public ServerArgsProjectViewProvider(Path bspProjectRoot, String pathToBazel, String targets) {
    this.pathToBazel = pathToBazel;
    this.targets = targets;
    this.defaultParserProvider = new ProjectViewDefaultParserProvider(bspProjectRoot);
  }

  @Override
  public ProjectView create() {
    var targetsSection =
        new ProjectViewTargetsSection(Arrays.asList(targets.split(",")), ImmutableList.of());
    var bazelPathSection = new ProjectViewBazelPathSection(pathToBazel);

    ProjectView parsedProjectView = defaultParserProvider.create();

    return ProjectView.builder()
        .targets(targetsSection)
        .bazelPath(Optional.of(bazelPathSection))
        .debuggerAddress(parsedProjectView.getDebuggerAddress())
        .javaPath(parsedProjectView.getJavaPath())
        .build();
  }
}
