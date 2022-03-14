package org.jetbrains.bsp.bazel.server.sync.languages;

import ch.epfl.scala.bsp4j.BuildTarget;
import io.vavr.collection.HashSet;
import io.vavr.collection.Seq;
import io.vavr.collection.Set;
import io.vavr.control.Option;
import java.net.URI;
import org.jetbrains.bsp.bazel.info.BspTargetInfo.TargetInfo;

public abstract class LanguagePlugin<T> {
  public void prepareSync(Seq<TargetInfo> targets) {}

  public Option<T> resolveModule(TargetInfo targetInfo) {
    return Option.none();
  }

  public Set<URI> dependencySources(TargetInfo targetInfo) {
    return HashSet.empty();
  }

  public final void setModuleData(Object moduleData, BuildTarget buildTarget) {
    applyModuleData((T) moduleData, buildTarget);
  }

  protected abstract void applyModuleData(T moduleData, BuildTarget buildTarget);
}
