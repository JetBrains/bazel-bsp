package org.jetbrains.bsp.bazel.server.bsp

import org.jetbrains.bsp.bazel.server.sync.ExecuteService
import org.jetbrains.bsp.bazel.server.sync.ProjectSyncService

data class BazelServices(
  val serverLifetime: BazelBspServerLifetime,
  val bspRequestsRunner: BspRequestsRunner,
  val projectSyncService: ProjectSyncService,
  val executeService: ExecuteService
)
