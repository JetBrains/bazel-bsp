load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "cli",
    srcs = glob(["*.java"]),
    visibility = ["//install/src/main/java/org/jetbrains/bsp/bazel/install:__subpackages__"],
    exports = [
        "@maven//:io_vavr_vavr",
    ],
    deps = [
        "@maven//:commons_cli_commons_cli",
        "@maven//:io_vavr_vavr",
    ],
)
