load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "workspace",
    srcs = glob(["*.java"]),
    visibility = ["//server:__subpackages__"],
    deps = [
        "//bazelrunner",
        "@maven//:ch_epfl_scala_bsp4j",
    ],
)
