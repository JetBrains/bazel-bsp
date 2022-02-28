load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "predefined",
    srcs = glob(["*.java"]),
    visibility = [
        "//executioncontext/api:__pkg__",
        "//executioncontext/api/src/test/java/org/jetbrains/bsp/bazel/executioncontext/api/entries/validators/predefined:__pkg__",
    ],
    exports = [
        "//executioncontext/api/src/main/java/org/jetbrains/bsp/bazel/executioncontext/api/entries/validators",
        "//projectview:model",
        "@maven//:io_vavr_vavr",
    ],
    deps = [
        "//executioncontext/api/src/main/java/org/jetbrains/bsp/bazel/executioncontext/api/entries/validators",
        "//projectview:model",
        "@maven//:io_vavr_vavr",
    ],
)
