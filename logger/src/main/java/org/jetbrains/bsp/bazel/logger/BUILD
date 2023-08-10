load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

kt_jvm_library(
    name = "logger",
    srcs = glob([
        "*.java",
        "*.kt",
    ]),
    visibility = ["//logger:__subpackages__"],
    deps = [
        "//commons",
        "@maven//:com_github_agluszak_bsp_generators",
    ],
)
