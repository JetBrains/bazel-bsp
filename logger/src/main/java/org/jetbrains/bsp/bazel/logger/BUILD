load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "logger",
    srcs = glob(["*.java"]),
    visibility = ["//logger:__subpackages__"],
    deps = [
        "@maven//:ch_epfl_scala_bsp4j",
    ],
)
