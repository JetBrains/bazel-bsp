package org.jetbrains.bsp.bazel.commons;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class Constants {

  public static final String NAME = "bazelbsp";
  public static final String VERSION = "0.0.0";
  public static final String BSP_VERSION = "2.0.0";

  public static final String SCALA = "scala";
  public static final String JAVA = "java";
  public static final String KOTLIN = "kotlin";
  public static final String CPP = "cpp";

  public static final String SCALAC = "Scalac";
  public static final String JAVAC = "Javac";
  public static final String KOTLINC = "KotlinCompile";

  public static final List<String> SUPPORTED_LANGUAGES = ImmutableList.of(SCALA, JAVA, KOTLIN);
  public static final List<String> SUPPORTED_COMPILERS = ImmutableList.of(SCALAC, JAVAC, KOTLINC);

  public static final List<String> SCALA_EXTENSIONS = ImmutableList.of(".scala");
  public static final List<String> JAVA_EXTENSIONS = ImmutableList.of(".java");
  public static final List<String> KOTLIN_EXTENSIONS = ImmutableList.of(".kt");
  public static final List<String> CPP_EXTENSIONS =
      ImmutableList.of(".C", ".cc", ".cpp", ".CPP", ".c++", ".cp", "cxx", ".h", ".hpp");

  public static final String MAIN_CLASS_ATTR_NAME = "main_class";
  public static final String ARGS_ATTR_NAME = "args";
  public static final String JVM_FLAGS_ATTR_NAME = "jvm_flags";

  public static final List<String> OTHER_FILE_EXTENSIONS =
      ImmutableList.of(".kts", ".sh", ".bzl", ".py", ".js", ".c", ".h", ".cpp", ".hpp");

  public static final List<String> FILE_EXTENSIONS =
      ImmutableList.of(SCALA_EXTENSIONS, JAVA_EXTENSIONS, KOTLIN_EXTENSIONS, OTHER_FILE_EXTENSIONS)
          .stream()
          .flatMap(Collection::stream)
          .collect(Collectors.toList());

  public static final String BAZEL_BUILD_COMMAND = "build";

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

  public static final String SCALA_TEST_MAIN_CLASSES_ATTRIBUTE_NAME = "main_class";

  public static final String DEFAULT_PROJECT_VIEW_FILE = "projectview.bazelproject";
}
