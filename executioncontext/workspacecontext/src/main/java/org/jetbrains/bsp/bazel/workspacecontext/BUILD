load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "workspacecontext",
    srcs = glob(["*.java"]),
    visibility = [
        "//executioncontext/workspacecontext:__pkg__",
        "//executioncontext/workspacecontext/src/test/java/org/jetbrains/bsp/bazel/workspacecontext:__pkg__",
    ],
    exports = [
        "//executioncontext/api",
        "//executioncontext/workspacecontext/src/main/java/org/jetbrains/bsp/bazel/workspacecontext/entries",
        "@maven//:io_vavr_vavr",
    ],
    deps = [
        "//executioncontext/api",
        "//executioncontext/workspacecontext/src/main/java/org/jetbrains/bsp/bazel/workspacecontext/entries",
        "//executioncontext/workspacecontext/src/main/java/org/jetbrains/bsp/bazel/workspacecontext/entries/mappers",
        "@maven//:io_vavr_vavr",
        "@maven//:org_apache_logging_log4j_log4j_api",
        "@maven//:org_apache_logging_log4j_log4j_core",
    ],
)
