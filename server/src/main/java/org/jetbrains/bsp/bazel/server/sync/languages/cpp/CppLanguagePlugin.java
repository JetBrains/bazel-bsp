package org.jetbrains.bsp.bazel.server.sync.languages.cpp;

import ch.epfl.scala.bsp4j.BuildTarget;
import io.vavr.control.Option;
import java.nio.file.Path;
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

  @Override
  public Option<Path> calculateSourceRoot(Path source) {
    return Option.none();
  }
}
