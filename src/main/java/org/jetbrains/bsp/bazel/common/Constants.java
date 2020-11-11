package org.jetbrains.bsp.bazel.common;

import com.google.common.collect.ImmutableList;
import java.util.List;

public class Constants {
  public static final String NAME = "bazelbsp";
  public static final String VERSION = "0.0.0";
  public static final String BSP_VERSION = "2.0.0";
  public static final List<String> SUPPORTED_LANGUAGES =
      ImmutableList.of("scala", "java", "kotlin");
  public static final String ASPECTS_FILE_NAME = "aspects.bzl";
  public static final String BUILD_FILE_NAME = "BUILD";
  public static final String BAZELBSP_DIR_NAME = ".bazelbsp";
  public static final String BSP_DIR_NAME = ".bsp";
  public static final String BAZELBSP_JSON_FILE_NAME = "bazelbsp.json";
  public static final String BAZELBSP_LOG_FILE_NAME = "bazelbsp.log";
  public static final String BAZELBSP_TRACE_JSON_FILE_NAME = "bazelbsp.trace.json";
}
