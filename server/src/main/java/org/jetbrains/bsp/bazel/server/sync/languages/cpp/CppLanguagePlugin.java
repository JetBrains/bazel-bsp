package org.jetbrains.bsp.bazel.server.sync.languages.cpp;

import ch.epfl.scala.bsp4j.BuildTarget;
import io.vavr.control.Option;
import org.jetbrains.bsp.bazel.info.BspTargetInfo;
import org.jetbrains.bsp.bazel.server.sync.languages.LanguagePlugin;

// TODO implement
public class CppLanguagePlugin extends LanguagePlugin<CppModule> {
  @Override
  public Option<CppModule> resolveModule(BspTargetInfo.TargetInfo targetInfo) {
    return null;
  }

  @Override
  public void applyModuleData(CppModule moduleData, BuildTarget buildTarget) {}
}
