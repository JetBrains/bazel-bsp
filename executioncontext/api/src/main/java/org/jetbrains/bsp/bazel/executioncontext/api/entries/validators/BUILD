load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "validators",
    srcs = glob(["*.java"]),
    visibility = [
        "//executioncontext/api:__pkg__",
        "//executioncontext/api/src/main/java/org/jetbrains/bsp/bazel/executioncontext/api/entries/validators:__subpackages__",
    ],
    exports = [
        "//projectview:model",
        "@maven//:io_vavr_vavr",
    ],
    deps = [
        "//projectview:model",
        "@maven//:io_vavr_vavr",
    ],
)
