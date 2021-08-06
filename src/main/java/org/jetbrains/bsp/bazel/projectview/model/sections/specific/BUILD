load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "specific",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/main/java/org/jetbrains/bsp/bazel/commons",
        "//src/main/java/org/jetbrains/bsp/bazel/projectview/model/sections",
    ],
)
