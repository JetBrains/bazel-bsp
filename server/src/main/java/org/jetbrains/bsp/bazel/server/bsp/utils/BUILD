load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "utils",
    srcs = glob(["*.java"]),
    visibility = ["//server:__subpackages__"],
    deps = [
        "//bazelrunner",
        "//commons",
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/bsp/info",
        "@maven//:ch_epfl_scala_bsp4j",
        "@maven//:com_google_guava_guava",
        "@maven//:io_vavr_vavr",
    ],
)
