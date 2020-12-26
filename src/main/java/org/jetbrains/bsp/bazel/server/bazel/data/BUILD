load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "bazel-data",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/main/java/org/jetbrains/bsp/bazel/server/bazel/utils:bazel-utils",
        "@maven//:ch_epfl_scala_bsp4j",
    ],
)
