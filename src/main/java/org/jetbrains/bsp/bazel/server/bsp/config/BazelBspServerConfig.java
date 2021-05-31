package org.jetbrains.bsp.bazel.server.bsp.config;

import org.jetbrains.bsp.bazel.projectview.model.ProjectView;

public class BazelBspServerConfig {

  private final String pathToBazel;
  private final ProjectView projectView;

  public BazelBspServerConfig(String pathToBazel, ProjectView projectView) {
    this.pathToBazel = pathToBazel;
    this.projectView = projectView;
  }

  public String getBazelPath() {
    return pathToBazel;
  }

  public ProjectView getProjectView() {
    return projectView;
  }
}
