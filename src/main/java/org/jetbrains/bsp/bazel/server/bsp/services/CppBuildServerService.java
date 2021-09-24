package org.jetbrains.bsp.bazel.server.bsp.services;

import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import ch.epfl.scala.bsp4j.CppOptionsItem;
import ch.epfl.scala.bsp4j.CppOptionsParams;
import ch.epfl.scala.bsp4j.CppOptionsResult;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseError;
import org.jetbrains.bsp.bazel.server.bazel.BazelRunner;
import org.jetbrains.bsp.bazel.server.bazel.params.BazelRunnerFlag;
import org.jetbrains.bsp.bazel.server.bsp.managers.BazelBspAspectsManager;

public class CppBuildServerService {
  public static final int COPTS_LOCATION = 0;
  public static final int DEFINES_LOCATION = 1;
  public static final int LINKOPTS_LOCATION = 2;
  public static final int LINKSHARED_LOCATION = 3;
  private final BazelRunner bazelRunner;
  private static final String FETCH_CPP_TARGET_ASPECT =
      "@//.bazelbsp:aspects.bzl%get_cpp_target_info";

  public CppBuildServerService(BazelRunner bazelRunner) {
    this.bazelRunner = bazelRunner;
  }

  public Either<ResponseError, CppOptionsResult> buildTargetCppOptions(
      CppOptionsParams cppOptionsParams) {
    List<CppOptionsItem> items =
        cppOptionsParams.getTargets().stream().map(this::getOptions).collect(Collectors.toList());
    CppOptionsResult cppOptionsResult = new CppOptionsResult(items);
    return Either.forRight(cppOptionsResult);
  }

  private CppOptionsItem getOptions(BuildTargetIdentifier buildTargetIdentifier) {
    List<String> lines =
        bazelRunner
            .commandBuilder()
            .build()
            .withFlag(BazelRunnerFlag.ASPECTS, FETCH_CPP_TARGET_ASPECT)
            .withArgument(buildTargetIdentifier.getUri())
            .executeBazelBesCommand()
            .getStderr();

    List<String> targetInfo =
        lines.stream()
            .map(line -> Splitter.on(" ").splitToList(line))
            .filter(
                parts ->
                    parts.size() == 3
                        && parts.get(0).equals(BazelBspAspectsManager.DEBUG_MESSAGE)
                        && parts.get(1).contains(BazelBspAspectsManager.ASPECT_LOCATION))
            .map(parts -> parts.get(2))
            .collect(Collectors.toList());

    if (targetInfo.size() != 4) {
      return new CppOptionsItem(
          buildTargetIdentifier, ImmutableList.of(), ImmutableList.of(), ImmutableList.of());
    } else {
      List<String> copts =
          Arrays.stream(targetInfo.get(COPTS_LOCATION).split(",")).collect(Collectors.toList());
      List<String> defines =
          Arrays.stream(targetInfo.get(DEFINES_LOCATION).split(",")).collect(Collectors.toList());
      List<String> linkopts =
          Arrays.stream(targetInfo.get(LINKOPTS_LOCATION).split(",")).collect(Collectors.toList());

      boolean linkshared = false;
      if (targetInfo.get(LINKSHARED_LOCATION).equals("True")) {
        linkshared = true;
      }

      CppOptionsItem cppOptionsItem =
          new CppOptionsItem(buildTargetIdentifier, copts, defines, linkopts);
      cppOptionsItem.setLinkshared(linkshared);
      return cppOptionsItem;
    }
  }
}
