package org.jetbrains.bsp.bazel.server.bsp.impl;

import ch.epfl.scala.bsp4j.JavaBuildServer;
import ch.epfl.scala.bsp4j.JavacOptionsParams;
import ch.epfl.scala.bsp4j.JavacOptionsResult;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.bsp.bazel.server.bsp.BazelBspServerRequestHelpers;
import org.jetbrains.bsp.bazel.server.bsp.services.JavaBuildServerService;

public class JavaBuildServerImpl implements JavaBuildServer {

  private final JavaBuildServerService javaBuildServerService;
  private final BazelBspServerRequestHelpers serverRequestHelpers;

  public JavaBuildServerImpl(
      JavaBuildServerService javaBuildServerService,
      BazelBspServerRequestHelpers serverRequestHelpers) {
    this.javaBuildServerService = javaBuildServerService;
    this.serverRequestHelpers = serverRequestHelpers;
  }

  @Override
  public CompletableFuture<JavacOptionsResult> buildTargetJavacOptions(
      JavacOptionsParams javacOptionsParams) {
    return serverRequestHelpers.executeCommand(
        "buildTargetJavacOptions",
        () -> javaBuildServerService.buildTargetJavacOptions(javacOptionsParams));
  }
}
