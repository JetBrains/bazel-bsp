load("@rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

kt_jvm_library(
    name = "paths",
    srcs = glob(["*.kt"]),
    visibility = ["//server:__subpackages__"],
    deps = [
        "//bazelrunner",
        "//server/src/main/kotlin/org/jetbrains/bsp/bazel/server/sync/proto:bsp_target_info_java_proto",
    ],
)
