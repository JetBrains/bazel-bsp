load("@rules_java//java:defs.bzl", "java_library")
load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

java_library(
    name = "parser",
    srcs = glob(["*.java"]),
    resources = ["//log4j_config"],
    visibility = [
        "//executioncontext/projectview:__pkg__",
        "//executioncontext/projectview/src/test/java/org/jetbrains/bsp/bazel/projectview/parser:__pkg__",
    ],
    exports = [
        "//executioncontext/projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/model",
        "//executioncontext/projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/parser:parser_kotlin",
    ],
    deps = [
        "//commons",
        "//executioncontext/projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/model",
        "//executioncontext/projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/parser:parser_kotlin",
        "//executioncontext/projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/parser/sections",
        "@maven//:com_google_guava_guava",
        "@maven//:io_vavr_vavr",
        "@maven//:org_apache_logging_log4j_log4j_api",
        "@maven//:org_apache_logging_log4j_log4j_core",
    ],
)

kt_jvm_library(
    name = "parser_kotlin",
    srcs = glob(["*.kt"]),
    resources = ["//log4j_config"],
    deps = [
        "//executioncontext/projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/model",
        "//executioncontext/projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/parser/sections",
        "@maven//:io_vavr_vavr",
        "@maven//:org_apache_logging_log4j_log4j_api",
        "@maven//:org_apache_logging_log4j_log4j_core",
    ],
)
