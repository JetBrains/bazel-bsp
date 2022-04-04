package org.jetbrains.bsp.bazel.server.sync.dependencytree;

import io.vavr.Lazy;
import io.vavr.collection.HashSet;
import io.vavr.collection.Map;
import io.vavr.collection.Set;
import org.jetbrains.bsp.bazel.info.BspTargetInfo.Dependency;
import org.jetbrains.bsp.bazel.info.BspTargetInfo.TargetInfo;

public class DependencyTree {
  private final Set<String> rootTargets;
  private final Map<String, TargetInfo> idToTargetInfo;
  private final Map<String, Lazy<Set<TargetInfo>>> idToLazyTransitiveDependencies;

  public DependencyTree(Map<String, TargetInfo> idToTargetInfo, Set<String> rootTargets) {
    this.rootTargets = rootTargets;
    this.idToTargetInfo = idToTargetInfo;
    this.idToLazyTransitiveDependencies = createIdToLazyTransitiveDependenciesMap(idToTargetInfo);
  }

  private Map<String, Lazy<Set<TargetInfo>>> createIdToLazyTransitiveDependenciesMap(
      Map<String, TargetInfo> idToTargetInfo) {
    return idToTargetInfo.mapValues(this::calculateLazyTransitiveDependenciesForTarget);
  }

  private Lazy<Set<TargetInfo>> calculateLazyTransitiveDependenciesForTarget(
      TargetInfo targetInfo) {
    return Lazy.of(() -> calculateTransitiveDependenciesForTarget(targetInfo));
  }

  private Set<TargetInfo> calculateTransitiveDependenciesForTarget(TargetInfo targetInfo) {
    var dependencies = getDependencies(targetInfo);
    var strictlyTransitiveDependencies = calculateStrictlyTransitiveDependencies(dependencies);
    var directDependencies = calculateDirectDependencies(dependencies);

    return strictlyTransitiveDependencies.addAll(directDependencies);
  }

  private Set<TargetInfo> calculateStrictlyTransitiveDependencies(Set<String> dependencies) {
    return dependencies.flatMap(idToLazyTransitiveDependencies::get).flatMap(Lazy::get);
  }

  private Set<TargetInfo> calculateDirectDependencies(Set<String> dependencies) {
    return dependencies.flatMap(idToTargetInfo::get);
  }

  public Set<TargetInfo> transitiveDependenciesWithoutRootTargets(String targetId) {
    var target = idToTargetInfo.get(targetId);

    return target
        .toSet()
        .flatMap(this::getDependencies)
        .filter(this::isNotARootTarget)
        .flatMap(this::collectTransitiveDependenciesAndAddTarget);
  }

  private Set<String> getDependencies(TargetInfo target) {
    return HashSet.ofAll(target.getDependenciesList()).map(Dependency::getId);
  }

  private boolean isNotARootTarget(String targetId) {
    return !rootTargets.contains(targetId);
  }

  private Set<TargetInfo> collectTransitiveDependenciesAndAddTarget(String targetId) {
    var target = idToTargetInfo.get(targetId).toSet();
    var dependencies = idToLazyTransitiveDependencies.get(targetId).toSet().flatMap(Lazy::get);

    return dependencies.addAll(target);
  }
}
