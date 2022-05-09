package org.jetbrains.bsp.bazel.server.bsp.config;

import com.google.common.base.Splitter;
import io.vavr.Lazy;
import io.vavr.control.Option;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.NoSuchElementException;

import org.jetbrains.bsp.bazel.bazelrunner.BazelFlagsProvider;
import org.jetbrains.bsp.bazel.bazelrunner.BazelPathProvider;
import org.jetbrains.bsp.bazel.projectview.model.ProjectView;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBuildFlagsSection;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewSingletonSection;
import org.jetbrains.bsp.bazel.projectview.parser.ProjectViewDefaultParserProvider;
import org.jetbrains.bsp.bazel.server.bsp.info.BspInfo;

public class BazelBspServerConfig implements ProjectViewProvider, BazelPathProvider, BazelFlagsProvider {

  private final Lazy<String> bazelFromPath = Lazy.of(this::findBazelOnPath);
  private final ProjectViewDefaultParserProvider projectViewProvider;

  public BazelBspServerConfig(Option<Path> projectViewPath, BspInfo bspInfo) {
    var bspProjectRoot = bspInfo.bspProjectRoot();
    this.projectViewProvider =
        new ProjectViewDefaultParserProvider(bspProjectRoot, projectViewPath);
  }

  @Override
  public ProjectView currentProjectView() {
    return projectViewProvider.create().get();
  }

  @Override
  public String currentBazelPath() {
    return currentBazelPath(currentProjectView());
  }

  private String currentBazelPath(ProjectView projectView) {
    return Option.of(projectView.getBazelPath())
        .map(ProjectViewSingletonSection::getValue)
        .map(Path::toString)
        .getOrElse(bazelFromPath);
  }

  private String findBazelOnPath() {
    var pathElements = Splitter.on(File.pathSeparator).splitToList(System.getenv("PATH"));

    return pathElements.stream()
        .filter(this::isItNotBazeliskPath)
        .map(element -> new File(element, "bazel"))
        .filter(File::canExecute)
        .findFirst()
        .map(File::toString)
        .orElseThrow(() -> new NoSuchElementException("Could not find bazel on your PATH"));
  }

  private boolean isItNotBazeliskPath(String path) {
    return !path.contains("bazelisk/");
  }

  @Override
  public List<String> currentBazelFlags(){
    return currentBazelFlags(currentProjectView());
  }

  public List<String> currentBazelFlags(ProjectView projectView){
    return Option.of(projectView.getBuildFlags())
        .map(ProjectViewBuildFlagsSection::getValues)
        .map(io.vavr.collection.List::toJavaList)
        .getOrElse(List.of());
  }
}
