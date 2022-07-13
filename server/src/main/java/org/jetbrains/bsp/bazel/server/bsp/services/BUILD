load("@rules_java//java:defs.bzl", "java_library")

# TODO it will be removed anyway
java_library(
    name = "services",
    srcs = glob(["*.java"]),
    visibility = ["//server:__subpackages__"],
    deps = [
        "@maven//:ch_epfl_scala_bsp4j",
    ],
)
