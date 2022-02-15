package org.jetbrains.bsp.bazel.server.bsp.impl;

import ch.epfl.scala.bsp4j.JvmBuildServer;
import ch.epfl.scala.bsp4j.JvmRunEnvironmentParams;
import ch.epfl.scala.bsp4j.JvmRunEnvironmentResult;
import ch.epfl.scala.bsp4j.JvmTestEnvironmentParams;
import ch.epfl.scala.bsp4j.JvmTestEnvironmentResult;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.bsp.bazel.server.bsp.BazelBspServerRequestHelpers;
import org.jetbrains.bsp.bazel.server.bsp.services.JvmBuildServerService;

public class JvmBuildServerImpl implements JvmBuildServer {

  private final JvmBuildServerService jvmBuildServerService;
  private final BazelBspServerRequestHelpers serverRequestHelpers;

  public JvmBuildServerImpl(
      JvmBuildServerService jvmBuildServerService,
      BazelBspServerRequestHelpers serverRequestHelpers) {
    this.jvmBuildServerService = jvmBuildServerService;
    this.serverRequestHelpers = serverRequestHelpers;
  }

  @Override
  public CompletableFuture<JvmRunEnvironmentResult> jvmRunEnvironment(
      JvmRunEnvironmentParams params) {
    return serverRequestHelpers.executeCommand(
        "jvmRunEnvironment", () -> jvmBuildServerService.jvmRunEnvironment(params));
  }

  @Override
  public CompletableFuture<JvmTestEnvironmentResult> jvmTestEnvironment(
      JvmTestEnvironmentParams params) {
    return serverRequestHelpers.executeCommand(
        "jvmTestEnvironment", () -> jvmBuildServerService.jvmTestEnvironment(params));
  }
}
