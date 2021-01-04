load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "utils",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/main/java/org/jetbrains/bsp/bazel/server/bazel/params",
        "@maven//:ch_epfl_scala_bsp4j",
        "@maven//:com_google_guava_guava",
    ],
)
