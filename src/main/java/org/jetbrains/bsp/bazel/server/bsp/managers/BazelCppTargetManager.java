package org.jetbrains.bsp.bazel.server.bsp.managers;

import ch.epfl.scala.bsp4j.CppBuildTarget;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.jetbrains.bsp.bazel.commons.Lazy;

public class BazelCppTargetManager extends Lazy<CppBuildTarget> {
  private static final String BAZEL_CPP_TOOLCHAIN = "@bazel_tools//tools/cpp:toolchain";
  private static final String FETCH_CPP_ASPECT = "@//.bazelbsp:aspects.bzl%fetch_cpp_compiler";
  private final BazelBspAspectsManager bazelBspAspectsManager;

  public BazelCppTargetManager(BazelBspAspectsManager bazelBspAspectsManager) {
    this.bazelBspAspectsManager = bazelBspAspectsManager;
  }

  private Optional<CppBuildTarget> getCppBuildTarget() {
    List<String> cppInfo =
        bazelBspAspectsManager
            .fetchLinesFromAspect(BAZEL_CPP_TOOLCHAIN, FETCH_CPP_ASPECT)
            .filter(
                parts ->
                    parts.size() == 3
                        && parts.get(0).equals(BazelBspAspectsManager.DEBUG_MESSAGE)
                        && parts.get(1).contains(BazelBspAspectsManager.ASPECT_LOCATION))
            .map(parts -> parts.get(2))
            .limit(2)
            .collect(Collectors.toList());

    if (cppInfo.isEmpty()) return Optional.empty();

    String compiler = cppInfo.get(0);
    String compilerExecutable = cppInfo.get(1);
    return Optional.of(new CppBuildTarget(null, compiler, compilerExecutable, compilerExecutable));
  }

  @Override
  protected Supplier<Optional<CppBuildTarget>> calculateValue() {
    return this::getCppBuildTarget;
  }
}
