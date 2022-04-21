package org.jetbrains.bsp.bazel.server.sync;

import org.jetbrains.bsp.bazel.server.sync.model.Project;

public class ProjectProvider {
  private Project project;
  private final ProjectResolver projectResolver;
  private final ProjectStorage projectStorage;

  public ProjectProvider(ProjectResolver projectResolver, ProjectStorage projectStorage) {
    this.projectResolver = projectResolver;
    this.projectStorage = projectStorage;
  }

  public synchronized Project refreshAndGet() {
    loadFromBazel();
    return project;
  }

  public synchronized Project get() {
    if (project == null) {
      loadFromDisk();
    }
    if (project == null) {
      loadFromBazel();
    }

    return project;
  }

  private void loadFromBazel() {
    project = projectResolver.resolve();
    storeOnDisk();
  }

  private void loadFromDisk() {
    projectStorage.load().forEach(p -> project = p);
  }

  private void storeOnDisk() {
    projectStorage.store(project);
  }
}
