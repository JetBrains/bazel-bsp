package org.jetbrains.bsp.bazel.server.sync;

import java.util.ArrayList;
import org.jetbrains.bsp.bazel.server.sync.model.Project;

public class ProjectProvider {
  private Project project;
  private final ProjectResolver projectResolver;
  private final ProjectStorage projectStorage;
  private final java.util.List<ProjectChangeListener> listeners = new ArrayList<>();

  public ProjectProvider(ProjectResolver projectResolver, ProjectStorage projectStorage) {
    this.projectResolver = projectResolver;
    this.projectStorage = projectStorage;
  }

  public void addListener(ProjectChangeListener listener) {
    listeners.add(listener);
  }

  private void notifyListeners() {
    listeners.forEach(listener -> listener.onProjectChange(project));
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
    notifyListeners();
  }

  private void loadFromDisk() {
    projectStorage
        .load()
        .forEach(
            p -> {
              project = p;
              notifyListeners();
            });
  }

  private void storeOnDisk() {
    projectStorage.store(project);
  }
}
