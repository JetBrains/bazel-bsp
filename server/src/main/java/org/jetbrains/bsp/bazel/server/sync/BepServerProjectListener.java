package org.jetbrains.bsp.bazel.server.sync;

import static org.jetbrains.bsp.bazel.server.sync.BspMappings.toBspId;

import org.jetbrains.bsp.bazel.server.bep.BepServer;
import org.jetbrains.bsp.bazel.server.sync.model.Project;

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
