package org.jetbrains.bsp.bazel.server.sync.languages.thrift;

import ch.epfl.scala.bsp4j.BuildTarget;
import io.vavr.collection.Set;
import java.nio.file.Path;
import org.jetbrains.bsp.bazel.info.BspTargetInfo.TargetInfo;
import org.jetbrains.bsp.bazel.server.sync.BazelPathsResolver;
import org.jetbrains.bsp.bazel.server.sync.dependencytree.DependencyTree;
import org.jetbrains.bsp.bazel.server.sync.languages.LanguagePlugin;

public class ThriftLanguagePlugin extends LanguagePlugin<ThriftModule> {
  private static final String THRIFT_LIBRARY_RULE_NAME = "thrift_library";

  private final BazelPathsResolver bazelPathsResolver;

  public ThriftLanguagePlugin(BazelPathsResolver bazelPathsResolver) {
    this.bazelPathsResolver = bazelPathsResolver;
  }

  @Override
  public Set<Path> dependencySources(TargetInfo targetInfo, DependencyTree dependencyTree) {
    return dependencyTree
        .transitiveDependenciesWithoutRootTargets(targetInfo.getId())
        .filter(this::isThriftLibrary)
        .flatMap(TargetInfo::getSourcesList)
        .map(bazelPathsResolver::resolve);
  }

  private boolean isThriftLibrary(TargetInfo target) {
    return target.getKind().equals(THRIFT_LIBRARY_RULE_NAME);
  }

  @Override
  protected void applyModuleData(ThriftModule moduleData, BuildTarget buildTarget) {
    // no actions needed
  }
}
