load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

kt_jvm_library(
    name = "installationcontext",
    srcs = glob(["*.kt"]),
    visibility = [
        "//executioncontext/installationcontext:__pkg__",
        "//executioncontext/installationcontext/src/test/kotlin/org/jetbrains/bsp/bazel/installationcontext:__pkg__",
    ],
    exports = [
        "//executioncontext/api",
    ],
    deps = [
        "//executioncontext/api",
        "//executioncontext/projectview:parser",
    ],
)
