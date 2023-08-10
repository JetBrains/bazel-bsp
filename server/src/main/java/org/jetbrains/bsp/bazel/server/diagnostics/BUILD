load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

kt_jvm_library(
    name = "diagnostics",
    srcs = glob(["*.kt"]),
    visibility = ["//server:__subpackages__"],
    deps = [
        "//bazelrunner",
        "//commons",
        "@maven//:com_github_agluszak_bsp_generators",
    ],
)
