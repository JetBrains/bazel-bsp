load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

kt_jvm_library(
    name = "server",
    srcs = glob([
        "**/*.java",
        "**/*.kt",
    ]),
    visibility = [
        "//bspcli:__subpackages__",
        "//install:__subpackages__",
        "//server:__subpackages__",
    ],
    deps = [
        "//bazelrunner",
        "//commons",
        "//executioncontext/workspacecontext",
        "//logger",
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/bep",
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/bsp",
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/bsp/info",
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/bsp/managers",
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/bsp/utils",
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/common",
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/diagnostics",
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/sync",
        "@maven//:ch_epfl_scala_bsp4j",
        "@maven//:org_eclipse_lsp4j_org_eclipse_lsp4j_jsonrpc",
    ],
)
