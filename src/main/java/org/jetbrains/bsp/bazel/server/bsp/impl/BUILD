load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "impl",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/main/java/org/jetbrains/bsp/bazel/commons",
        "//src/main/java/org/jetbrains/bsp/bazel/server/bsp",
        "//src/main/java/org/jetbrains/bsp/bazel/server/bsp/services",
        "@maven//:ch_epfl_scala_bsp4j",
        "@maven//:com_google_guava_guava",
    ],
)
