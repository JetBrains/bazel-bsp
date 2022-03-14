package org.jetbrains.bsp.bazel.server.bsp.services;

import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import ch.epfl.scala.bsp4j.CppOptionsItem;
import ch.epfl.scala.bsp4j.CppOptionsParams;
import ch.epfl.scala.bsp4j.CppOptionsResult;
import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.bsp.bazel.server.bsp.managers.BazelBspAspectsManager;

public class CppBuildServerService {
  public static final int COPTS_LOCATION = 0;
  public static final int DEFINES_LOCATION = 1;
  public static final int LINKOPTS_LOCATION = 2;
  public static final int LINKSHARED_LOCATION = 3;
  private static final String FETCH_CPP_TARGET_ASPECT = "get_cpp_target_info";
  private BazelBspAspectsManager bazelBspAspectsManager;

  public CppBuildServerService(BazelBspAspectsManager bazelBspAspectsManager) {
    this.bazelBspAspectsManager = bazelBspAspectsManager;
  }

  public CppOptionsResult buildTargetCppOptions(CppOptionsParams cppOptionsParams) {
    List<CppOptionsItem> items =
        cppOptionsParams.getTargets().stream().map(this::getOptions).collect(Collectors.toList());
    return new CppOptionsResult(items);
  }

  private CppOptionsItem getOptions(BuildTargetIdentifier buildTargetIdentifier) {
    List<String> targetInfo =
        bazelBspAspectsManager
            .fetchLinesFromAspect(buildTargetIdentifier.getUri(), FETCH_CPP_TARGET_ASPECT, true)
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

      boolean linkshared = targetInfo.get(LINKSHARED_LOCATION).equals("True");

      CppOptionsItem cppOptionsItem =
          new CppOptionsItem(buildTargetIdentifier, copts, defines, linkopts);
      cppOptionsItem.setLinkshared(linkshared);
      return cppOptionsItem;
    }
  }
}
