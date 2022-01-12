package org.jetbrains.bsp.bazel.server.bsp.config;

import java.nio.file.Path;
import org.jetbrains.bsp.bazel.projectview.model.ProjectView;

public class BazelBspServerConfig {

  private Path bspProjectRoot;
  private final String pathToBazel;
  private final ProjectView projectView;

  public BazelBspServerConfig(Path bspProjectRoot, String pathToBazel, ProjectView projectView) {
    this.bspProjectRoot = bspProjectRoot;
    this.pathToBazel = pathToBazel;
    this.projectView = projectView;
  }

  public String getBazelPath() {
    return pathToBazel;
  }

  public ProjectView getProjectView() {
    return projectView;
  }

  public Path getBspProjectRoot() {
    return bspProjectRoot;
  }
}
