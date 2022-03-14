package org.jetbrains.bsp.bazel.server.sync.languages;

import ch.epfl.scala.bsp4j.BuildTarget;
import io.vavr.control.Option;
import org.jetbrains.bsp.bazel.info.BspTargetInfo;

public class EmptyLanguagePlugin extends LanguagePlugin<Object> {
  @Override
  public Option<Object> resolveModule(BspTargetInfo.TargetInfo targetInfo) {
    return Option.none();
  }

  @Override
  protected void applyModuleData(Object moduleData, BuildTarget buildTarget) {}
}
