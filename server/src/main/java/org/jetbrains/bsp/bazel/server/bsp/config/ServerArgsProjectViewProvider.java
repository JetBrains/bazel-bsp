package org.jetbrains.bsp.bazel.server.bsp.config;

import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import io.vavr.control.Try;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
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
    this.pathToBazel = new ProjectViewBazelPathSection(Paths.get(pathToBazel));
    this.targets =
        Optional.of(new ProjectViewTargetsSection(calculateIncludedTargets(targets), List.of()));
    this.defaultParserProvider = new ProjectViewDefaultParserProvider(bspProjectRoot);
  }

  private List<BuildTargetIdentifier> calculateIncludedTargets(String targets) {
    return Arrays.stream(targets.split(","))
        .map(BuildTargetIdentifier::new)
        .collect(Collectors.toList());
  }

  public ServerArgsProjectViewProvider(Path bspProjectRoot, String pathToBazel) {
    this.pathToBazel = new ProjectViewBazelPathSection(Paths.get(pathToBazel));
    this.targets = Optional.empty();
    this.defaultParserProvider = new ProjectViewDefaultParserProvider(bspProjectRoot);
  }

  @Override
  public Try<ProjectView> create() {
    var parsedProjectView = defaultParserProvider.create().get();

    return ProjectView.builder()
        .targets(targets.or(parsedProjectView::getTargets))
        .bazelPath(Optional.of(pathToBazel))
        .debuggerAddress(parsedProjectView.getDebuggerAddress())
        .javaPath(parsedProjectView.getJavaPath())
        .build();
  }
}
