load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "data",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//bazelrunner/src/main/java/org/jetbrains/bsp/bazel/bazelrunner/utils",
        "//commons",
        "//logger",
        "@maven//:ch_epfl_scala_bsp4j",
        "@maven//:io_vavr_vavr",
    ],
)
