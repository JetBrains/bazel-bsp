load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "config",
    srcs = glob(["*.java"]),
    visibility = ["//server:__subpackages__"],
    deps = [
        "//bazelrunner",
        "//executioncontext/projectview:parser",
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/bsp/info",
        "@maven//:com_google_guava_guava",
    ],
)
