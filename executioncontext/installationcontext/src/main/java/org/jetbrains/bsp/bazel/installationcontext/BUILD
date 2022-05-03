load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

kt_jvm_library(
    name = "installationcontext",
    srcs = glob(["*.kt"]),
    visibility = ["//visibility:public"],
    exports = [
        "//executioncontext/api",
        "@maven//:io_vavr_vavr",
    ],
    deps = [
        "//executioncontext/api",
        "@maven//:io_vavr_vavr",
    ],
)
