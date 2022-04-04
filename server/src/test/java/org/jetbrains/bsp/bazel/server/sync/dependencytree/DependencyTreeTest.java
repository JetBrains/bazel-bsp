package org.jetbrains.bsp.bazel.server.sync.dependencytree;

import static org.assertj.core.api.Assertions.assertThat;

import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import io.vavr.collection.List;
import org.jetbrains.bsp.bazel.info.BspTargetInfo.Dependency;
import org.jetbrains.bsp.bazel.info.BspTargetInfo.TargetInfo;
import org.junit.jupiter.api.Test;

// graphs generated using: https://arthursonzogni.com/Diagon/#GraphDAG
public class DependencyTreeTest {

  @Test
  public void shouldReturnEmptyListForNotExistingTarget() {
    // given
    var dependencyTree = new DependencyTree(HashMap.empty(), HashSet.empty());

    // when
    var dependencies = dependencyTree.transitiveDependenciesWithoutRootTargets("//does/not/exist");

    // then
    assertThat(dependencies).isEmpty();
  }

  @Test
  public void shouldReturnNoDependenciesForTargetWithoutDependencies() {
    // tree:
    // '?' - queried target
    // '+' - should be returned
    // '-' - shouldn't be returned
    // capital letter - root target
    // ┌─┐
    // │A│
    // └┬┘
    // ┌▽┐
    // │B│
    // │?│
    // └─┘

    // given
    var a = targetInfo("//A", List.of("//B"));
    var b = targetInfo("//B", List.of());

    var idToTargetInfo = HashSet.of(a, b).toMap(TargetInfo::getId, x -> x);
    var rootTargets = HashSet.of("//A", "//B");
    var dependencyTree = new DependencyTree(idToTargetInfo, rootTargets);

    // when
    var dependencies = dependencyTree.transitiveDependenciesWithoutRootTargets("//B");

    // then
    assertThat(dependencies).isEmpty();
  }

  @Test
  public void shouldReturnOnlyDirectDependenciesForTargetWithoutTransitiveDependencies() {
    // tree:
    // '?' - queried target
    // '+' - should be returned
    // '-' - shouldn't be returned
    // capital letter - root target
    // ┌───────┐
    // │   A   │
    // │   ?   │
    // └┬──┬──┬┘
    // ┌▽┐┌▽┐┌▽┐
    // │b││c││d│
    // │+││+││+│
    // └─┘└─┘└─┘

    // given
    var a = targetInfo("//A", List.of("//b", "//c", "//d"));
    var b = targetInfo("//b", List.of());
    var c = targetInfo("//c", List.of());
    var d = targetInfo("//d", List.of());

    var idToTargetInfo = HashSet.of(a, b, c, d).toMap(TargetInfo::getId, x -> x);
    var rootTargets = HashSet.of("//A");
    var dependencyTree = new DependencyTree(idToTargetInfo, rootTargets);

    // when
    var dependencies = dependencyTree.transitiveDependenciesWithoutRootTargets("//A");

    // then
    var expectedDependencies = HashSet.of(b, c, d);
    assertThat(dependencies).isEqualTo(expectedDependencies);
  }

  @Test
  public void shouldReturnDirectAndTransitiveDependenciesForTargetWithTransitiveDependencies() {
    // tree:
    // '?' - queried target
    // '+' - should be returned
    // '-' - shouldn't be returned
    // capital letter - root target
    // ┌───────┐
    // │   A   │
    // │   ?   │
    // └┬─────┬┘
    // ┌▽───┐┌▽┐
    // │ b  ││c│
    // │ +  ││+│
    // └┬──┬┘└─┘
    // ┌▽┐┌▽┐
    // │d││e│
    // │+││+│
    // └─┘└─┘

    // given
    var a = targetInfo("//A", List.of("//b", "//c"));
    var b = targetInfo("//b", List.of("//d", "//e"));
    var c = targetInfo("//c", List.of());
    var d = targetInfo("//d", List.of());
    var e = targetInfo("//e", List.of());

    var idToTargetInfo = HashSet.of(a, b, c, d, e).toMap(TargetInfo::getId, x -> x);
    var rootTargets = HashSet.of("//A");
    var dependencyTree = new DependencyTree(idToTargetInfo, rootTargets);

    // when
    var dependencies = dependencyTree.transitiveDependenciesWithoutRootTargets("//A");

    // then
    var expectedDependencies = HashSet.of(b, c, d, e);
    assertThat(dependencies).isEqualTo(expectedDependencies);
  }

  @Test
  public void
      shouldReturnDirectAndTransitiveDependenciesForTargetWithTransitiveDependenciesIncludingDeepRootTarget() {
    // tree:
    // '?' - queried target
    // '+' - should be returned
    // '-' - shouldn't be returned
    // capital letter - root target
    // ┌──────────┐
    // │    A     │
    // │    ?     │
    // └┬────────┬┘
    // ┌▽──────┐┌▽┐
    // │   b   ││c│
    // │   +   ││+│
    // └┬─────┬┘└─┘
    // ┌▽───┐┌▽┐
    // │ D  ││e│
    // │ +  ││+│
    // └┬──┬┘└─┘
    // ┌▽┐┌▽┐
    // │f││g│
    // │+││+│
    // └─┘└─┘

    // given
    var a = targetInfo("//A", List.of("//b", "//c"));
    var b = targetInfo("//b", List.of("//D", "//e"));
    var c = targetInfo("//c", List.of());
    var d = targetInfo("//D", List.of("//f", "//g"));
    var e = targetInfo("//e", List.of());
    var f = targetInfo("//f", List.of());
    var g = targetInfo("//g", List.of());

    var idToTargetInfo = HashSet.of(a, b, c, d, e, f, g).toMap(TargetInfo::getId, x -> x);
    var rootTargets = HashSet.of("//A", "//D");
    var dependencyTree = new DependencyTree(idToTargetInfo, rootTargets);

    // when
    var dependencies = dependencyTree.transitiveDependenciesWithoutRootTargets("//A");

    // then
    var expectedDependencies = HashSet.of(b, c, d, e, f, g);
    assertThat(dependencies).isEqualTo(expectedDependencies);
  }

  @Test
  public void
      shouldReturnDirectAndTransitiveDependenciesForTargetWithTransitiveDependenciesIncludingDeepRootTargetAndExcludingDirectRootTargets() {
    // tree:
    // '?' - queried target
    // '+' - should be returned
    // '-' - shouldn't be returned
    // capital letter - root target
    // ┌───────┐
    // │   A   │
    // │   ?   │
    // └┬─────┬┘
    // ┌▽───┐┌▽─────────┐
    // │ B  ││    c     │
    // │ -  ││    +     │
    // └┬──┬┘└┬─────┬──┬┘
    // ┌▽┐┌▽┐┌▽───┐┌▽┐┌▽┐
    // │d││e││ F  ││g││h│
    // │-││-││ +  ││+││+│
    // └─┘└─┘└┬──┬┘└─┘└─┘
    //       ┌▽┐┌▽┐
    //       │i││j│
    //       │+││+│
    //       └─┘└┬┘
    //          ┌▽┐
    //          │k│
    //          │+│
    //          └┬┘
    //          ┌▽┐
    //          │L│
    //          │+│
    //          └─┘

    // given
    var a = targetInfo("//A", List.of("//B", "//c"));
    var b = targetInfo("//B", List.of("//d", "//e"));
    var c = targetInfo("//c", List.of("//F", "//g", "//h"));
    var d = targetInfo("//d", List.of());
    var e = targetInfo("//e", List.of());
    var f = targetInfo("//F", List.of("//i", "//j"));
    var g = targetInfo("//g", List.of());
    var h = targetInfo("//h", List.of());
    var i = targetInfo("//i", List.of());
    var j = targetInfo("//j", List.of("//k"));
    var k = targetInfo("//k", List.of("//L"));
    var l = targetInfo("//L", List.of());

    var idToTargetInfo =
        HashSet.of(a, b, c, d, e, f, g, h, i, j, k, l).toMap(TargetInfo::getId, x -> x);
    var rootTargets = HashSet.of("//A", "//B", "//F", "//L");
    var dependencyTree = new DependencyTree(idToTargetInfo, rootTargets);

    // when
    var dependencies = dependencyTree.transitiveDependenciesWithoutRootTargets("//A");

    // then
    var expectedDependencies = HashSet.of(c, f, g, h, i, j, k, l);
    assertThat(dependencies).isEqualTo(expectedDependencies);
  }

  @Test
  public void
      shouldReturnDirectAndTransitiveDependenciesForTargetWithTransitiveDependenciesIncludingDeepRootTargetAndExcludingDirectRootTargetsWhichIsNotARootForTheTree() {
    // tree:
    // '?' - queried target
    // '+' - should be returned
    // '-' - shouldn't be returned
    // capital letter - root target
    // ┌───────┐
    // │   A   │
    // └┬─────┬┘
    // ┌▽───┐┌▽─────────┐
    // │ B  ││    c     │
    // └┬──┬┘└┬─────┬──┬┘
    // ┌▽┐┌▽┐┌▽───┐┌▽┐┌▽┐
    // │d││e││ F  ││g││h│
    // │-││-││ ?  ││-││-│
    // └─┘└─┘└┬──┬┘└─┘└─┘
    //       ┌▽┐┌▽┐
    //       │i││j│
    //       │+││+│
    //       └─┘└┬┘
    //          ┌▽┐
    //          │k│
    //          │+│
    //          └┬┘
    //          ┌▽┐
    //          │L│
    //          │+│
    //          └─┘

    // given
    var a = targetInfo("//A", List.of("//B", "//c"));
    var b = targetInfo("//B", List.of("//d", "//e"));
    var c = targetInfo("//c", List.of("//F", "//g", "//h"));
    var d = targetInfo("//d", List.of());
    var e = targetInfo("//e", List.of());
    var f = targetInfo("//F", List.of("//i", "//j"));
    var g = targetInfo("//g", List.of());
    var h = targetInfo("//h", List.of());
    var i = targetInfo("//i", List.of());
    var j = targetInfo("//j", List.of("//k"));
    var k = targetInfo("//k", List.of("//L"));
    var l = targetInfo("//L", List.of());

    var idToTargetInfo =
        HashSet.of(a, b, c, d, e, f, g, h, i, j, k, l).toMap(TargetInfo::getId, x -> x);
    var rootTargets = HashSet.of("//A", "//B", "//F", "//L");
    var dependencyTree = new DependencyTree(idToTargetInfo, rootTargets);

    // when
    var dependencies = dependencyTree.transitiveDependenciesWithoutRootTargets("//F");

    // then
    var expectedDependencies = HashSet.of(i, j, k, l);
    assertThat(dependencies).isEqualTo(expectedDependencies);
  }

  private TargetInfo targetInfo(String id, List<String> dependenciesIds) {
    var dependencies = dependenciesIds.map(this::dependency);

    return TargetInfo.newBuilder().setId(id).addAllDependencies(dependencies).build();
  }

  private Dependency dependency(String id) {
    return Dependency.newBuilder().setId(id).build();
  }
}
