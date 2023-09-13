load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

kt_jvm_library(
    name = "generator",
    srcs = glob(["*.kt"]),
    visibility = [
        "//executioncontext/projectview:__pkg__",
        "//executioncontext/projectview/src/test/java/org/jetbrains/bsp/bazel/projectview/generator:__pkg__",
    ],
    exports = [
        "//executioncontext/projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/model",
    ],
    deps = [
        "//executioncontext/projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/generator/sections",
        "//executioncontext/projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/model",
    ],
)
