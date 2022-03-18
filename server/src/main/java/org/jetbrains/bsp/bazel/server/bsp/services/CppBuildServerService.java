package org.jetbrains.bsp.bazel.server.bsp.services;

import ch.epfl.scala.bsp4j.CppOptionsResult;
import java.util.List;

public class CppBuildServerService {

  public CppBuildServerService() {}

  public CppOptionsResult buildTargetCppOptions() {
    return new CppOptionsResult(List.of());
  }
}
