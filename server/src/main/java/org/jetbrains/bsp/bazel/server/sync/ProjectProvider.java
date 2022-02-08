package org.jetbrains.bsp.bazel.server.sync;

import java.util.ArrayList;
import org.jetbrains.bsp.bazel.server.sync.model.Project;

public class ProjectProvider {
  private Project project;
  private final ProjectResolver projectResolver;
  private final java.util.List<ProjectChangeListener> listeners = new ArrayList<>();

  public ProjectProvider(ProjectResolver projectResolver) {
    this.projectResolver = projectResolver;
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
    // TODO implement; do nothing if no project cache data is present
    // notifyListeners(); // only if actually loaded
  }

  private void storeOnDisk() {
    // TODO save project data to disk
  }
}
