load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

kt_jvm_library(
    name = "cli",
    srcs = glob(["*.kt"]),
    visibility = [
        "//install/src/main/java/org/jetbrains/bsp/bazel/install:__subpackages__",
        "//install/src/test/java/org/jetbrains/bsp/bazel/install/cli:__pkg__",
    ],
    deps = [
        "//commons",
        "@maven//:commons_cli_commons_cli",
    ],
)
