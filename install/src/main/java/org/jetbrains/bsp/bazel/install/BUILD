load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "install",
    srcs = glob(["*.java"]),
    visibility = ["//install:__subpackages__"],
    resources = [ "//install/src/main/resources",],
    deps = [
        "//commons",
        "//executioncontext/projectview:parser",
        "@maven//:ch_epfl_scala_bsp4j",
        "@maven//:com_google_code_gson_gson",
        "@maven//:com_google_guava_guava",
        "@maven//:commons_cli_commons_cli",
        "//executioncontext/installationcontext/src/main/java/org/jetbrains/bsp/bazel/installationcontext",
    ],
)
