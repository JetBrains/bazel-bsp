load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

kt_jvm_library(
    name = "api",
    srcs = glob(["*.kt"]),
    visibility = ["//executioncontext/api:__pkg__"],
    exports = [
        "//executioncontext/projectview:model",
    ],
    deps = [
        "//executioncontext/projectview:model",
    ],
)
