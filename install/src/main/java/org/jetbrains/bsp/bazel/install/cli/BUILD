load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "cli",
    srcs = glob(["*.java"]),
    visibility = [
        "//install/src/main/java/org/jetbrains/bsp/bazel/install:__subpackages__",
        "//install/src/test/java/org/jetbrains/bsp/bazel/install/cli:__pkg__",
    ],
    exports = [
        "@maven//:io_vavr_vavr",
        "@maven//:com_google_guava_guava",
    ],
    deps = [
        "@maven//:commons_cli_commons_cli",
        "@maven//:com_google_guava_guava",
        "@maven//:io_vavr_vavr",
    ],
)
