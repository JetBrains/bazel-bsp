load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "config",
    srcs = glob(["*.java"]),
    visibility = ["//server:__subpackages__"],
    deps = [
        "//bazelrunner",
        "//executioncontext/projectview:parser",
        "//executioncontext/workspacecontext",
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/bsp/info",
        "@maven//:com_google_guava_guava",
    ],
)

load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

kt_jvm_library(
    name = "config_kt",
    srcs = glob(["*.kt"]),
    visibility = ["//server:__subpackages__"],
    deps = [
        "//executioncontext/projectview:parser",
        "//executioncontext/workspacecontext",
    ],
)
