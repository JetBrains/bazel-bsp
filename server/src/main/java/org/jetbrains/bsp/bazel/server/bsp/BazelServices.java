package org.jetbrains.bsp.bazel.server.bsp;

import org.jetbrains.bsp.bazel.server.sync.ExecuteService;
import org.jetbrains.bsp.bazel.server.sync.ProjectSyncService;

public class BazelServices {
  private final BazelBspServerLifetime serverLifetime;
  private final BspRequestsRunner bspRequestsRunner;
  private final ProjectSyncService projectSyncService;
  private final ExecuteService executeService;

  public BazelServices(
      BazelBspServerLifetime serverLifetime,
      BspRequestsRunner bspRequestsRunner,
      ProjectSyncService projectSyncService,
      ExecuteService executeService) {
    this.serverLifetime = serverLifetime;
    this.bspRequestsRunner = bspRequestsRunner;
    this.projectSyncService = projectSyncService;
    this.executeService = executeService;
  }

  public BazelBspServerLifetime getServerLifetime() {
    return serverLifetime;
  }

  public BspRequestsRunner getBspRequestsRunner() {
    return bspRequestsRunner;
  }

  public ProjectSyncService getProjectSyncService() {
    return projectSyncService;
  }

  public ExecuteService getExecuteService() {
    return executeService;
  }
}
