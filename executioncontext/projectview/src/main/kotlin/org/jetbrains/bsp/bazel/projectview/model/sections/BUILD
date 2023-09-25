load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

kt_jvm_library(
    name = "sections",
    srcs = glob(["*.kt"]),
    visibility = [
        "//executioncontext/projectview/src/main/kotlin/org/jetbrains/bsp/bazel/projectview/model:__pkg__",
        "//executioncontext/projectview/src/test/kotlin/org/jetbrains/bsp/bazel/projectview/model/sections:__pkg__",
    ],
    exports = [
        "@maven//:ch_epfl_scala_bsp4j_2_13",
    ],
    deps = [
        "@maven//:ch_epfl_scala_bsp4j_2_13",
    ],
)
