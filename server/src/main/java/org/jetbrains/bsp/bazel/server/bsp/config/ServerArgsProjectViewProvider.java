package org.jetbrains.bsp.bazel.server.bsp.config;

import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import io.vavr.collection.List;
import io.vavr.control.Option;
import io.vavr.control.Try;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.jetbrains.bsp.bazel.projectview.model.ProjectView;
import org.jetbrains.bsp.bazel.projectview.model.ProjectViewProvider;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBazelPathSection;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewTargetsSection;
import org.jetbrains.bsp.bazel.projectview.parser.ProjectViewDefaultParserProvider;

@Deprecated
public class ServerArgsProjectViewProvider implements ProjectViewProvider {

  private final ProjectViewBazelPathSection pathToBazel;
  private final Option<ProjectViewTargetsSection> targets;

  private final ProjectViewDefaultParserProvider defaultParserProvider;

  public ServerArgsProjectViewProvider(Path bspProjectRoot, String pathToBazel, String targets) {
    this.pathToBazel = new ProjectViewBazelPathSection(Paths.get(pathToBazel));
    this.targets =
        Option.of(new ProjectViewTargetsSection(calculateIncludedTargets(targets), List.of()));
    this.defaultParserProvider = new ProjectViewDefaultParserProvider(bspProjectRoot);
  }

  private List<BuildTargetIdentifier> calculateIncludedTargets(String targets) {
    return List.of(targets.split(",")).map(BuildTargetIdentifier::new);
  }

  public ServerArgsProjectViewProvider(Path bspProjectRoot, String pathToBazel) {
    this.pathToBazel = new ProjectViewBazelPathSection(Paths.get(pathToBazel));
    this.targets = Option.none();
    this.defaultParserProvider = new ProjectViewDefaultParserProvider(bspProjectRoot);
  }

  @Override
  public Try<ProjectView> create() {
    var parsedProjectView = defaultParserProvider.create().get();

    return ProjectView.builder()
        .targets(targets.orElse(parsedProjectView::getTargets))
        .bazelPath(Option.of(pathToBazel))
        .debuggerAddress(parsedProjectView.getDebuggerAddress())
        .javaPath(parsedProjectView.getJavaPath())
        .build();
  }
}
