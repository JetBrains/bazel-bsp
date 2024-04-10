package org.jetbrains.bsp.bazel.commons;

import com.google.common.collect.ImmutableList;
import java.util.List;

public class Constants {
  public static final String NAME = "bazelbsp";
  public static final String VERSION = "3.2.0";
  public static final String BSP_VERSION = "2.1.0";
  public static final String SCALA = "scala";
  public static final String JAVA = "java";
  public static final String KOTLIN = "kotlin";
  public static final String CPP = "cpp";
  public static final List<String> SUPPORTED_LANGUAGES =
      ImmutableList.of(SCALA, JAVA, KOTLIN /*, CPP */);
  public static final String BAZEL_BUILD_COMMAND = "build";
  public static final String BUILD_FILE_NAME = "BUILD";
  public static final String WORKSPACE_FILE_NAME = "WORKSPACE";
  public static final String ASPECT_REPOSITORY = "bazelbsp_aspect";
  public static final String ASPECTS_ROOT = "aspects";
  public static final String DOT_BAZELBSP_DIR_NAME = ".bazelbsp";
  public static final String DOT_BSP_DIR_NAME = ".bsp";
  public static final String BAZELBSP_JSON_FILE_NAME = "bazelbsp.json";
  public static final String SERVER_CLASS_NAME = "org.jetbrains.bsp.bazel.server.ServerInitializer";
  public static final String CLASSPATH_FLAG = "-classpath";
  public static final String BAZELBSP_TRACE_JSON_FILE_NAME = "bazelbsp.trace.json";
}
