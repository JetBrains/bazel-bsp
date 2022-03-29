package org.jetbrains.bsp.bazel.server.sync;

import org.jetbrains.bsp.bazel.server.bep.BepServer;
import org.jetbrains.bsp.bazel.server.sync.model.Project;

import static org.jetbrains.bsp.bazel.server.sync.BspMappings.toBspId;

public class BepServerProjectListener implements ProjectChangeListener {
  private final BepServer bepServer;

  public BepServerProjectListener(BepServer bepServer) {
    this.bepServer = bepServer;
  }

  @Override
  public void onProjectChange(Project project) {
    var sourcesMap = bepServer.getBuildTargetsSources();
    sourcesMap.clear();
    project
        .modules()
        .forEach(
            module -> {
              var id = toBspId(module);
              var sources = module.sourceSet().sources().toJavaList();
              sourcesMap.put(id, sources);
            });
  }
}
