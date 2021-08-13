load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "specific",
    srcs = glob(["*.java"]),
    visibility = ["//projectview:__subpackages__"],
    deps = [
        "//projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/model/sections",
        "//src/main/java/org/jetbrains/bsp/bazel/commons",
    ],
)
