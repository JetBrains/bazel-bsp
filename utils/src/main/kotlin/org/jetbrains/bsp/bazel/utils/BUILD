load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

kt_jvm_library(
    name = "dope_files",
    srcs = ["DopeFiles.kt"],
    visibility = [
        "//utils:__pkg__",
        "//utils/src/test/kotlin/org/jetbrains/bsp/bazel/utils:__pkg__",
    ],
    deps = [
        "@maven//:io_vavr_vavr",
    ],
)
