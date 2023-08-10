load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

kt_jvm_library(
    name = "outputs",
    srcs = glob(["*.kt"]),
    visibility = ["//visibility:public"],
    deps = [
        "//bazelrunner/src/main/java/org/jetbrains/bsp/bazel/bazelrunner/utils",
        "//commons",
        "//logger",
        "@maven//:com_github_agluszak_bsp_generators",
    ],
)
