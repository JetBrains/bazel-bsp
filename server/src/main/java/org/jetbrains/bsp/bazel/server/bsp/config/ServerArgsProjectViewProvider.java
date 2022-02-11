package org.jetbrains.bsp.bazel.server.bsp.config;

import com.google.common.collect.ImmutableList;
import io.vavr.control.Try;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import org.jetbrains.bsp.bazel.projectview.model.ProjectView;
import org.jetbrains.bsp.bazel.projectview.model.ProjectViewProvider;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBazelPathSection;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewTargetsSection;
import org.jetbrains.bsp.bazel.projectview.parser.ProjectViewDefaultParserProvider;

@Deprecated
public class ServerArgsProjectViewProvider implements ProjectViewProvider {

  private final ProjectViewBazelPathSection pathToBazel;
  private final Optional<ProjectViewTargetsSection> targets;

  private final ProjectViewDefaultParserProvider defaultParserProvider;

  public ServerArgsProjectViewProvider(Path bspProjectRoot, String pathToBazel, String targets) {
    this.pathToBazel = new ProjectViewBazelPathSection(pathToBazel);
    this.targets =
        Optional.of(
            new ProjectViewTargetsSection(Arrays.asList(targets.split(",")), ImmutableList.of()));
    this.defaultParserProvider = new ProjectViewDefaultParserProvider(bspProjectRoot);
  }

  public ServerArgsProjectViewProvider(Path bspProjectRoot, String pathToBazel) {
    this.pathToBazel = new ProjectViewBazelPathSection(pathToBazel);
    this.targets = Optional.empty();
    this.defaultParserProvider = new ProjectViewDefaultParserProvider(bspProjectRoot);
  }

  @Override
  public Try<ProjectView> create() {
    var parsedProjectView = defaultParserProvider.create().get();

    return ProjectView.builder()
        .targets(targets.orElse(parsedProjectView.getTargets()))
        .bazelPath(Optional.of(pathToBazel))
        .debuggerAddress(parsedProjectView.getDebuggerAddress())
        .javaPath(parsedProjectView.getJavaPath())
        .build();
  }
}
