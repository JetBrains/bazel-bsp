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

  public DependencyTree(Set<TargetInfo> targets, Set<String> rootTargets) {
    this.rootTargets = rootTargets;
    this.idToTargetInfo = createIdToTargetInfoMap(targets);
    this.idToLazyTransitiveDependencies = createIdToLazyTransitiveDependenciesMap(targets);
  }

  private Map<String, TargetInfo> createIdToTargetInfoMap(Set<TargetInfo> targets) {
    return targets.toMap(TargetInfo::getId, x -> x);
  }

  private Map<String, Lazy<Set<TargetInfo>>> createIdToLazyTransitiveDependenciesMap(
      Set<TargetInfo> targets) {
    return targets.toMap(TargetInfo::getId, this::calculateLazyTransitiveDependenciesForTarget);
  }

  private Lazy<Set<TargetInfo>> calculateLazyTransitiveDependenciesForTarget(
      TargetInfo targetInfo) {
    return Lazy.of(() -> calculateTransitiveDependenciesForTarget(targetInfo));
  }

  private Set<TargetInfo> calculateTransitiveDependenciesForTarget(TargetInfo targetInfo) {
    var strictlyTransitiveDependencies = calculateStrictlyTransitiveDependencies(targetInfo);
    var directDependencies = calculateDirectDependencies(targetInfo);

    return strictlyTransitiveDependencies.addAll(directDependencies);
  }

  private Set<TargetInfo> calculateStrictlyTransitiveDependencies(TargetInfo targetInfo) {
    return getDependencies(targetInfo)
        .flatMap(idToLazyTransitiveDependencies::get)
        .flatMap(Lazy::get);
  }

  private Set<TargetInfo> calculateDirectDependencies(TargetInfo targetInfo) {
    return getDependencies(targetInfo).flatMap(idToTargetInfo::get);
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
