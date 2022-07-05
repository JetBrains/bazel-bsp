load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

kt_jvm_library(
    name = "utils",
    srcs = glob([
        "**/*.java",
        "**/*.kt",
    ]),
    visibility = ["//server:__subpackages__"],
    deps = [
        "//commons",
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/bsp/info",
    ],
)
