load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

kt_jvm_library(
    name = "dope",
    srcs = glob(["*.kt"]),
    visibility = [
        "//utils:__pkg__",
        "//utils/src/test/kotlin/org/jetbrains/bsp/bazel/utils/dope:__pkg__",
    ],
    deps = [
        "@maven//:io_vavr_vavr",
    ],
)
