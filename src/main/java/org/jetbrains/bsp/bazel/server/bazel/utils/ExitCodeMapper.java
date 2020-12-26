package org.jetbrains.bsp.bazel.server.bazel.utils;

import ch.epfl.scala.bsp4j.StatusCode;
import com.google.common.collect.ImmutableMap;
import java.util.Map;

public final class ExitCodeMapper {

  private static final Map<Integer, StatusCode> EXIT_CODE_TO_STATUS_CODE =
      ImmutableMap.of(
          0, StatusCode.OK,
          8, StatusCode.CANCELLED);
  private static final StatusCode DEFAULT_STATUS_CODE = StatusCode.ERROR;

  public static StatusCode mapExitCode(int exitCode) {
    return EXIT_CODE_TO_STATUS_CODE.getOrDefault(exitCode, DEFAULT_STATUS_CODE);
  }
}
