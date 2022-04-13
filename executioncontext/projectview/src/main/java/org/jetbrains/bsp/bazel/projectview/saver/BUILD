load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

kt_jvm_library(
    name = "saver",
    srcs = glob(["*.kt"]),
    exports = [
    ],
    deps = [
        "//executioncontext/projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/model",
        "@maven//:io_vavr_vavr",
    ],
)
