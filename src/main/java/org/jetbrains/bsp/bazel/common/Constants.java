package org.jetbrains.bsp.bazel.common;

import com.google.common.collect.ImmutableList;
import java.util.List;

public class Constants {

  public static final String NAME = "bazelbsp";
  public static final String VERSION = "0.0.0";
  public static final String BSP_VERSION = "2.0.0";

  public static final String SCALA = "scala";
  public static final String JAVA = "java";
  public static final String KOTLIN = "kotlin";

  public static final String SCALAC = "Scalac";
  public static final String JAVAC = "Javac";
  public static final String KOTLINC = "KotlinCompile";

  public static final List<String> SUPPORTED_LANGUAGES = ImmutableList.of(SCALA, JAVA, KOTLIN);
  public static final List<String> SUPPORTED_COMPILERS = ImmutableList.of(SCALAC, JAVAC, KOTLINC);

  public static final String SCALA_EXTENSION = ".scala";
  public static final String JAVA_EXTENSION = ".java";
  public static final String KOTLIN_EXTENSION = ".kt";

  public static final List<String> FILE_EXTENSIONS =
      ImmutableList.of(
          SCALA_EXTENSION,
          JAVA_EXTENSION,
          KOTLIN_EXTENSION,
          ".kts",
          ".sh",
          ".bzl",
          ".py",
          ".js",
          ".c",
          ".h",
          ".cpp",
          ".hpp");

  public static final String BAZEL_BUILD_COMMAND = "build";
  public static final String BAZEL_RUN_COMMAND = "run";
  public static final String BAZEL_TEST_COMMAND = "test";
  public static final String BAZEL_CLEAN_COMMAND = "clean";

  public static final String BINARY_RULE_TYPE = "binary";
  public static final String LIBRARY_RULE_TYPE = "library";
  public static final String TEST_RULE_TYPE = "test";

  public static final String BUILD_FILE_NAME = "BUILD";
  public static final String WORKSPACE_FILE_NAME = "WORKSPACE";
  public static final String ASPECTS_FILE_NAME = "aspects.bzl";
  public static final String BAZELBSP_DIR_NAME = ".bazelbsp";
  public static final String BSP_DIR_NAME = ".bsp";
  public static final String BAZELBSP_JSON_FILE_NAME = "bazelbsp.json";
  public static final String BAZELBSP_LOG_FILE_NAME = "bazelbsp.log";
  public static final String BAZELBSP_TRACE_JSON_FILE_NAME = "bazelbsp.trace.json";

  public static final List<String> KNOWN_SOURCE_ROOTS =
      ImmutableList.of("java", "scala", "kotlin", "javatests", "src", "test", "main", "testsrc");
  public static final String DIAGNOSTICS = "diagnostics";
  public static final String EXEC_ROOT_PREFIX = "exec-root://";
  public static final String SCALA_COMPILER_CLASSPATH_FILES = "scala_compiler_classpath_files";
}
