load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "cli",
    srcs = glob(["*.java"]),
    visibility = ["//install:__subpackages__"],
    deps = [
        "@maven//:commons_cli_commons_cli",
        "@maven//:io_vavr_vavr",
    ],
)
