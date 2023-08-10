package org.jetbrains.bsp.bazel.commons;

import com.jetbrains.bsp.bsp4kt.StatusCode;
import com.google.common.collect.ImmutableMap;
import java.util.Map;

public final class ExitCodeMapper {

  private static final Map<Integer, StatusCode> EXIT_CODE_TO_STATUS_CODE =
      ImmutableMap.of(
          0, StatusCode.Ok,
          8, StatusCode.Cancelled);
  private static final StatusCode DEFAULT_STATUS_CODE = StatusCode.Error;

  public static StatusCode mapExitCode(int exitCode) {
    return EXIT_CODE_TO_STATUS_CODE.getOrDefault(exitCode, DEFAULT_STATUS_CODE);
  }
}
