load("@rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

kt_jvm_library(
    name = "diagnostics",
    srcs = glob(["*.kt"]),
    visibility = ["//server:__subpackages__"],
    deps = [
        "//bazelrunner",
        "//commons",
        "//server/src/main/kotlin/org/jetbrains/bsp/bazel/server/model",
        "@maven//:ch_epfl_scala_bsp4j",
    ],
)
