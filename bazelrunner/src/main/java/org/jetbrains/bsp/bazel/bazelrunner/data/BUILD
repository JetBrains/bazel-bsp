load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "data",
    srcs = glob(["*.java"]),
    visibility = ["//bazelrunner:__subpackages__"],
    deps = [
        "//bazelrunner/src/main/java/org/jetbrains/bsp/bazel/bazelrunner/utils",
        "//commons",
        "//logger",
        "@maven//:ch_epfl_scala_bsp4j",
        "@maven//:com_google_guava_guava",
    ],
)
