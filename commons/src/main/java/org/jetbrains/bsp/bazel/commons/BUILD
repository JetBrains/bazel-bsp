load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "commons",
    srcs = glob(["*.java"]),
    visibility = ["//commons:__subpackages__"],
    deps = [
        "@maven//:ch_epfl_scala_bsp4j_2_13",
        "@maven//:com_google_guava_guava",
        "@maven//:io_vavr_vavr",
    ],
)
