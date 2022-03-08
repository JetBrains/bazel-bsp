load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "parser",
    srcs = glob(["*.java"]),
    visibility = [
        "//executioncontext/projectview:__pkg__",
        "//executioncontext/projectview/src/test/java/org/jetbrains/bsp/bazel/projectview/parser:__pkg__",
    ],
    exports = [
        "//executioncontext/projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/model",
        "//executioncontext/projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/parser/sections",
    ],
    deps = [
        "//commons",
        "//executioncontext/projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/model",
        "//executioncontext/projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/parser/sections",
        "@maven//:com_google_guava_guava",
        "@maven//:io_vavr_vavr",
        "@maven//:org_apache_logging_log4j_log4j_api",
        "@maven//:org_apache_logging_log4j_log4j_core",
    ],
)
