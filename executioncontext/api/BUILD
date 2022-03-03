load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "api",
    visibility = ["//executioncontext:__subpackages__"],
    exports = [
        "//executioncontext/api/src/main/java/org/jetbrains/bsp/bazel/executioncontext/api",
        "//executioncontext/api/src/main/java/org/jetbrains/bsp/bazel/executioncontext/api/entries",
        "//executioncontext/api/src/main/java/org/jetbrains/bsp/bazel/executioncontext/api/entries/mappers",
    ],
)
