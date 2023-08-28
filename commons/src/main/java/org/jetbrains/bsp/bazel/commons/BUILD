load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

kt_jvm_library(
    name = "commons",
    srcs = glob([
        "*.java",
        "*.kt",
    ]),
    visibility = ["//commons:__subpackages__"],
    deps = [
        "@maven//:ch_epfl_scala_bsp4j_2_13",
        "@maven//:com_google_guava_guava",
    ],
)
