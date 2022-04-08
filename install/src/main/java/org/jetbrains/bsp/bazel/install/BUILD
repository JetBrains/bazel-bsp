load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "install",
    srcs = glob(["*.java"]),
    resources = ["//install/src/main/resources"],
    visibility = ["//install:__subpackages__"],
    deps = [
        "//commons",
        "//executioncontext/installationcontext/src/main/java/org/jetbrains/bsp/bazel/installationcontext",
        "//executioncontext/projectview:parser",
        "//install/src/main/java/org/jetbrains/bsp/bazel/install/cli",
        "@maven//:ch_epfl_scala_bsp4j",
        "@maven//:com_google_code_gson_gson",
        "@maven//:com_google_guava_guava",
        "@maven//:commons_cli_commons_cli",
        "@maven//:io_vavr_vavr",
    ],
)
