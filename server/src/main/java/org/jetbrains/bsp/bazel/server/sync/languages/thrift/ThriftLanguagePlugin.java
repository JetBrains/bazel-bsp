package org.jetbrains.bsp.bazel.server.sync.languages.thrift;

import ch.epfl.scala.bsp4j.BuildTarget;
import io.vavr.collection.Set;
import io.vavr.control.Option;
import java.net.URI;
import org.jetbrains.bsp.bazel.info.BspTargetInfo;
import org.jetbrains.bsp.bazel.info.BspTargetInfo.TargetInfo;
import org.jetbrains.bsp.bazel.server.sync.BazelPathsResolver;
import org.jetbrains.bsp.bazel.server.sync.languages.LanguagePlugin;

public class ThriftLanguagePlugin extends LanguagePlugin<Object> {
  private final BazelPathsResolver bazelPathsResolver;

  public ThriftLanguagePlugin(BazelPathsResolver bazelPathsResolver) {
    this.bazelPathsResolver = bazelPathsResolver;
  }

  @Override
  public Set<URI> dependencySources(TargetInfo targetInfo) {
    return Option.when(targetInfo.hasThriftTargetInfo(), targetInfo.getThriftTargetInfo())
        .toSet()
        .flatMap(BspTargetInfo.ThriftTargetInfo::getDependencySourcesList)
        .map(bazelPathsResolver::resolveUri);
  }

  @Override
  protected void applyModuleData(Object moduleData, BuildTarget buildTarget) {
    // no actions needed
  }
}
