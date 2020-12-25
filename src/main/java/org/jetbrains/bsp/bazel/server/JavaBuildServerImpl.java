package org.jetbrains.bsp.bazel.server;

import ch.epfl.scala.bsp4j.JavaBuildServer;
import ch.epfl.scala.bsp4j.JavacOptionsParams;
import ch.epfl.scala.bsp4j.JavacOptionsResult;
import java.util.concurrent.CompletableFuture;

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
        () -> javaBuildServerService.buildTargetJavacOptions(javacOptionsParams));
  }
}
