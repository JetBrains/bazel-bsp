load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "entries",
    srcs = glob(["*.java"]),
    visibility = [
        "//executioncontext/api:__pkg__",
        "//executioncontext/api/src/main/java/org/jetbrains/bsp/bazel/executioncontext/api/entries:__subpackages__",
    ],
    exports = [
        "@maven//:io_vavr_vavr",
    ],
    deps = [
        "@maven//:io_vavr_vavr",
    ],
)
