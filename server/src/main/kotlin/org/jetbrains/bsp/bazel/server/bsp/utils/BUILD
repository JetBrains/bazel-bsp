load("@rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

kt_jvm_library(
    name = "utils",
    srcs = [
        "InternalAspectsResolver.kt",
    ],
    visibility = ["//server:__subpackages__"],
    deps = [
        "//bazelrunner",
        "//commons",
        "//server/src/main/kotlin/org/jetbrains/bsp/bazel/server/bsp/info",
    ],
)

kt_jvm_library(
    name = "fileutils",
    srcs = ["FileUtils.kt"],
    visibility = ["//server:__subpackages__"],
)
