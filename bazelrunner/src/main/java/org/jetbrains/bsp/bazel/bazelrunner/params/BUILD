load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "params",
    srcs = glob(["*.java"]),
    visibility = ["//bazelrunner:__subpackages__"],
    deps = [
        "@maven//:ch_epfl_scala_bsp4j",
        "@maven//:io_vavr_vavr",
    ],
)
