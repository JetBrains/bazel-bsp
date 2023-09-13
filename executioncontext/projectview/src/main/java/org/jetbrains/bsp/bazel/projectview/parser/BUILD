load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

kt_jvm_library(
    name = "parser",
    srcs = glob(["*.kt"]),
    resources = ["//log4j_config"],
    visibility = [
        "//executioncontext/projectview:__pkg__",
        "//executioncontext/projectview/src/test/java/org/jetbrains/bsp/bazel/projectview/parser:__pkg__",
    ],
    exports = [
        "//executioncontext/projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/model",
    ],
    deps = [
        "//commons",
        "//executioncontext/projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/model",
        "//executioncontext/projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/parser/sections",
        "@maven//:org_apache_logging_log4j_log4j_api",
        "@maven//:org_apache_logging_log4j_log4j_core",
    ],
)
