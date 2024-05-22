load("@rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

kt_jvm_library(
    name = "languages",
    srcs = glob(["**/*.kt"]),
    visibility = ["//visibility:public"],
    deps = [
        "//protocol",
        "//server/src/main/kotlin/org/jetbrains/bsp/bazel/server/dependencygraph",
        "//server/src/main/kotlin/org/jetbrains/bsp/bazel/server/model",
        "//server/src/main/kotlin/org/jetbrains/bsp/bazel/server/paths",
        "//server/src/main/kotlin/org/jetbrains/bsp/bazel/server/sync/proto:bsp_target_info_java_proto",
        "@maven//:ch_epfl_scala_bsp4j",
    ],
)
