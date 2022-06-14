package org.jetbrains.bsp.bazel.server.sync.languages.thrift;

import ch.epfl.scala.bsp4j.BuildTarget;
import io.vavr.collection.HashSet;
import io.vavr.collection.Set;
import io.vavr.control.Option;
import java.net.URI;
import java.nio.file.Path;
import org.jetbrains.bsp.bazel.info.BspTargetInfo.TargetInfo;
import org.jetbrains.bsp.bazel.server.bsp.utils.SourceRootGuesser;
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
  public Set<URI> dependencySources(TargetInfo targetInfo, DependencyTree dependencyTree) {
    return HashSet.ofAll(
            dependencyTree.transitiveDependenciesWithoutRootTargets(targetInfo.getId()))
        .filter(this::isThriftLibrary)
        .flatMap(TargetInfo::getSourcesList)
        .map(bazelPathsResolver::resolveUri);
  }

  private boolean isThriftLibrary(TargetInfo target) {
    return target.getKind().equals(THRIFT_LIBRARY_RULE_NAME);
  }

  @Override
  protected void applyModuleData(ThriftModule moduleData, BuildTarget buildTarget) {
    // no actions needed
  }

  @Override
  public Option<Path> calculateSourceRoot(Path source) {
    return Option.some(SourceRootGuesser.getSourcesRoot(source));
  }
}
