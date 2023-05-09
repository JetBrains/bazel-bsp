load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

kt_jvm_library(
    name = "install",
    srcs = glob(["*.kt"]),
    resources = ["//install/src/main/resources"],
    visibility = ["//install:__subpackages__"],
    deps = [
        "//commons",
        "//executioncontext/installationcontext",
        "//executioncontext/projectview:generator",
        "//executioncontext/projectview:parser",
        "//install/src/main/java/org/jetbrains/bsp/bazel/install/cli",
        "@maven//:ch_epfl_scala_bsp4j",
        "@maven//:com_google_code_gson_gson",
        "@maven//:commons_cli_commons_cli",
        "@maven//:io_vavr_vavr",
    ],
)
