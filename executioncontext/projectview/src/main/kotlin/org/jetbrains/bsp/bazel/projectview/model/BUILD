load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

kt_jvm_library(
    name = "model",
    srcs = glob(["*.kt"]),
    visibility = [
        "//executioncontext/projectview:__pkg__",
        "//executioncontext/projectview/src/main/java/org/jetbrains/bsp/bazel/projectview:__subpackages__",
        "//executioncontext/projectview/src/test/java/org/jetbrains/bsp/bazel/projectview/model:__pkg__",
    ],
    exports = [
        "//executioncontext/projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/model/sections",
        "@maven//:io_vavr_vavr",
    ],
    deps = [
        "//commons",
        "//executioncontext/projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/model/sections",
        "@maven//:io_vavr_vavr",
        "@maven//:org_apache_logging_log4j_log4j_api",
        "@maven//:org_apache_logging_log4j_log4j_core",
    ],
)
