load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "impl",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/main/java/org/jetbrains/bsp/bazel/common",
        "//src/main/java/org/jetbrains/bsp/bazel/server/bsp",
        "//src/main/java/org/jetbrains/bsp/bazel/server/service",
        "@maven//:ch_epfl_scala_bsp4j",
    ],
)
