package org.jetbrains.bsp.bazel.server;

import ch.epfl.scala.bsp4j.ScalaBuildServer;
import ch.epfl.scala.bsp4j.ScalaMainClassesParams;
import ch.epfl.scala.bsp4j.ScalaMainClassesResult;
import ch.epfl.scala.bsp4j.ScalaTestClassesParams;
import ch.epfl.scala.bsp4j.ScalaTestClassesResult;
import ch.epfl.scala.bsp4j.ScalacOptionsParams;
import ch.epfl.scala.bsp4j.ScalacOptionsResult;
import java.util.concurrent.CompletableFuture;

public class ScalaBuildServerImpl implements ScalaBuildServer {

  private final ScalaBuildServerService scalaBuildServerService;
  private final BazelBspServerRequestHelpers serverRequestHelpers;

  public ScalaBuildServerImpl(
      ScalaBuildServerService scalaBuildServerService,
      BazelBspServerRequestHelpers serverRequestHelpers) {
    this.scalaBuildServerService = scalaBuildServerService;
    this.serverRequestHelpers = serverRequestHelpers;
  }

  @Override
  public CompletableFuture<ScalacOptionsResult> buildTargetScalacOptions(
      ScalacOptionsParams scalacOptionsParams) {
    return serverRequestHelpers.executeCommand(
        () -> scalaBuildServerService.buildTargetScalacOptions(scalacOptionsParams));
  }

  @Override
  public CompletableFuture<ScalaTestClassesResult> buildTargetScalaTestClasses(
      ScalaTestClassesParams scalaTestClassesParams) {
    return scalaBuildServerService.buildTargetScalaTestClasses(scalaTestClassesParams);
  }

  @Override
  public CompletableFuture<ScalaMainClassesResult> buildTargetScalaMainClasses(
      ScalaMainClassesParams scalaMainClassesParams) {
    return scalaBuildServerService.buildTargetScalaMainClasses(scalaMainClassesParams);
  }
}
