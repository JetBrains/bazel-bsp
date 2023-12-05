load("@rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

kt_jvm_library(
    name = "utils",
    srcs = glob(["*.kt"]),
    visibility = ["//visibility:public"],
    deps = [
        "//bazelrunner/src/main/kotlin/org/jetbrains/bsp/bazel/bazelrunner/params",
        "@maven//:ch_epfl_scala_bsp4j",
    ],
)
