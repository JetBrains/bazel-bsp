load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "mappers",
    srcs = glob(["*.java"]),
    visibility = [
        "//executioncontext/api:__pkg__",
    ],
    exports = [
        "//executioncontext/api/src/main/java/org/jetbrains/bsp/bazel/executioncontext/api/entries",
        "//executioncontext/projectview:model",
        "@maven//:io_vavr_vavr",
    ],
    deps = [
        "//executioncontext/api/src/main/java/org/jetbrains/bsp/bazel/executioncontext/api/entries",
        "//executioncontext/projectview:model",
        "@maven//:io_vavr_vavr",
    ],
)
