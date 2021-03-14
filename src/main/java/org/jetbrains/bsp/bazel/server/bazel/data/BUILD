load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "data",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/main/java/org/jetbrains/bsp/bazel/server/bazel/utils",
        "//src/main/java/org/jetbrains/bsp/bazel/server/loggers",
        "@maven//:ch_epfl_scala_bsp4j",
    ],
)
