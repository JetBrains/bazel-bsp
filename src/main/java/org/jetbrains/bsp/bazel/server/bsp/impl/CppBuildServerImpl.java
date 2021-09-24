package org.jetbrains.bsp.bazel.server.bsp.impl;

import ch.epfl.scala.bsp4j.CppBuildServer;
import ch.epfl.scala.bsp4j.CppOptionsParams;
import ch.epfl.scala.bsp4j.CppOptionsResult;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.bsp.bazel.server.bsp.BazelBspServerRequestHelpers;
import org.jetbrains.bsp.bazel.server.bsp.services.CppBuildServerService;

public class CppBuildServerImpl implements CppBuildServer {
  private final CppBuildServerService cppBuildServerService;
  private final BazelBspServerRequestHelpers serverRequestHelpers;

  public CppBuildServerImpl(
      CppBuildServerService cppBuildServerService,
      BazelBspServerRequestHelpers serverRequestHelpers) {
    this.cppBuildServerService = cppBuildServerService;
    this.serverRequestHelpers = serverRequestHelpers;
  }

  @Override
  public CompletableFuture<CppOptionsResult> buildTargetCppOptions(
      CppOptionsParams cppOptionsParams) {
    return serverRequestHelpers.executeCommand(
        "buildTargetCppOptions",
        () -> cppBuildServerService.buildTargetCppOptions(cppOptionsParams));
  }
}
