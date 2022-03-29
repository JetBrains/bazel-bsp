package org.jetbrains.bsp.bazel.server.sync.dependencytree;

import static org.assertj.core.api.Assertions.assertThat;

import io.vavr.collection.HashSet;
import org.jetbrains.bsp.bazel.info.BspTargetInfo.Dependency;
import org.jetbrains.bsp.bazel.info.BspTargetInfo.TargetInfo;
import org.junit.Test;

// graphs generated using: https://arthursonzogni.com/Diagon/#GraphDAG
public class DependencyTreeTest {

  @Test
  public void shouldReturnEmptyListForNotExistingTarget() {
    // given
    var dependencyTree = new DependencyTree(HashSet.empty(), HashSet.empty());

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
    var a =
        TargetInfo.newBuilder()
            .setId("//A")
            .addDependencies(Dependency.newBuilder().setId("//B").build())
            .build();
    TargetInfo b = TargetInfo.newBuilder().setId("//B").build();

    var targets = HashSet.of(a, b);
    var rootTargets = HashSet.of("//A", "//B");
    var dependencyTree = new DependencyTree(targets, rootTargets);

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
    var a =
        TargetInfo.newBuilder()
            .setId("//A")
            .addDependencies(Dependency.newBuilder().setId("//b").build())
            .addDependencies(Dependency.newBuilder().setId("//c").build())
            .addDependencies(Dependency.newBuilder().setId("//d").build())
            .build();
    var b = TargetInfo.newBuilder().setId("//b").build();
    var c = TargetInfo.newBuilder().setId("//c").build();
    var d = TargetInfo.newBuilder().setId("//d").build();

    var targets = HashSet.of(a, b, c, d);
    var rootTargets = HashSet.of("//A");
    var dependencyTree = new DependencyTree(targets, rootTargets);

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
    var a =
        TargetInfo.newBuilder()
            .setId("//A")
            .addDependencies(Dependency.newBuilder().setId("//b").build())
            .addDependencies(Dependency.newBuilder().setId("//c").build())
            .build();
    var b =
        TargetInfo.newBuilder()
            .setId("//b")
            .addDependencies(Dependency.newBuilder().setId("//d").build())
            .addDependencies(Dependency.newBuilder().setId("//e").build())
            .build();
    var c = TargetInfo.newBuilder().setId("//c").build();
    var d = TargetInfo.newBuilder().setId("//d").build();
    var e = TargetInfo.newBuilder().setId("//e").build();

    var targets = HashSet.of(a, b, c, d, e);
    var rootTargets = HashSet.of("//A");
    var dependencyTree = new DependencyTree(targets, rootTargets);

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
    var a =
        TargetInfo.newBuilder()
            .setId("//A")
            .addDependencies(Dependency.newBuilder().setId("//b").build())
            .addDependencies(Dependency.newBuilder().setId("//c").build())
            .build();
    var b =
        TargetInfo.newBuilder()
            .setId("//b")
            .addDependencies(Dependency.newBuilder().setId("//D").build())
            .addDependencies(Dependency.newBuilder().setId("//e").build())
            .build();
    var c = TargetInfo.newBuilder().setId("//c").build();
    var d =
        TargetInfo.newBuilder()
            .setId("//D")
            .addDependencies(Dependency.newBuilder().setId("//f").build())
            .addDependencies(Dependency.newBuilder().setId("//g").build())
            .build();
    var e = TargetInfo.newBuilder().setId("//e").build();
    var f = TargetInfo.newBuilder().setId("//f").build();
    var g = TargetInfo.newBuilder().setId("//g").build();

    var targets = HashSet.of(a, b, c, d, e, f, g);
    var rootTargets = HashSet.of("//A", "//D");
    var dependencyTree = new DependencyTree(targets, rootTargets);

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
    var a =
        TargetInfo.newBuilder()
            .setId("//A")
            .addDependencies(Dependency.newBuilder().setId("//B").build())
            .addDependencies(Dependency.newBuilder().setId("//c").build())
            .build();
    var b =
        TargetInfo.newBuilder()
            .setId("//B")
            .addDependencies(Dependency.newBuilder().setId("//d").build())
            .addDependencies(Dependency.newBuilder().setId("//e").build())
            .build();
    var c =
        TargetInfo.newBuilder()
            .setId("//c")
            .addDependencies(Dependency.newBuilder().setId("//F").build())
            .addDependencies(Dependency.newBuilder().setId("//g").build())
            .addDependencies(Dependency.newBuilder().setId("//h").build())
            .build();
    var d = TargetInfo.newBuilder().setId("//d").build();
    var e = TargetInfo.newBuilder().setId("//e").build();
    var f =
        TargetInfo.newBuilder()
            .setId("//F")
            .addDependencies(Dependency.newBuilder().setId("//i").build())
            .addDependencies(Dependency.newBuilder().setId("//j").build())
            .build();
    var g = TargetInfo.newBuilder().setId("//g").build();
    var h = TargetInfo.newBuilder().setId("//h").build();
    var i = TargetInfo.newBuilder().setId("//i").build();
    var j =
        TargetInfo.newBuilder()
            .setId("//j")
            .addDependencies(Dependency.newBuilder().setId("//k").build())
            .build();
    var k =
        TargetInfo.newBuilder()
            .setId("//k")
            .addDependencies(Dependency.newBuilder().setId("//L").build())
            .build();
    var l = TargetInfo.newBuilder().setId("//L").build();

    var targets = HashSet.of(a, b, c, d, e, f, g, h, i, j, k, l);
    var rootTargets = HashSet.of("//A", "//B", "//F", "//L");
    var dependencyTree = new DependencyTree(targets, rootTargets);

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
    var a =
        TargetInfo.newBuilder()
            .setId("//A")
            .addDependencies(Dependency.newBuilder().setId("//B").build())
            .addDependencies(Dependency.newBuilder().setId("//c").build())
            .build();
    var b =
        TargetInfo.newBuilder()
            .setId("//B")
            .addDependencies(Dependency.newBuilder().setId("//d").build())
            .addDependencies(Dependency.newBuilder().setId("//e").build())
            .build();
    var c =
        TargetInfo.newBuilder()
            .setId("//c")
            .addDependencies(Dependency.newBuilder().setId("//F").build())
            .addDependencies(Dependency.newBuilder().setId("//g").build())
            .addDependencies(Dependency.newBuilder().setId("//h").build())
            .build();
    var d = TargetInfo.newBuilder().setId("//d").build();
    var e = TargetInfo.newBuilder().setId("//e").build();
    var f =
        TargetInfo.newBuilder()
            .setId("//F")
            .addDependencies(Dependency.newBuilder().setId("//i").build())
            .addDependencies(Dependency.newBuilder().setId("//j").build())
            .build();
    var g = TargetInfo.newBuilder().setId("//g").build();
    var h = TargetInfo.newBuilder().setId("//h").build();
    var i = TargetInfo.newBuilder().setId("//i").build();
    var j =
        TargetInfo.newBuilder()
            .setId("//j")
            .addDependencies(Dependency.newBuilder().setId("//k").build())
            .build();
    var k =
        TargetInfo.newBuilder()
            .setId("//k")
            .addDependencies(Dependency.newBuilder().setId("//L").build())
            .build();
    var l = TargetInfo.newBuilder().setId("//L").build();

    var targets = HashSet.of(a, b, c, d, e, f, g, h, i, j, k, l);
    var rootTargets = HashSet.of("//A", "//B", "//F", "//L");
    var dependencyTree = new DependencyTree(targets, rootTargets);

    // when
    var dependencies = dependencyTree.transitiveDependenciesWithoutRootTargets("//F");

    // then
    var expectedDependencies = HashSet.of(i, j, k, l);
    assertThat(dependencies).isEqualTo(expectedDependencies);
  }
}
