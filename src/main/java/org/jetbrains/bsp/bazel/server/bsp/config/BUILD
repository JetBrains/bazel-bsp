load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "config",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/main/java/org/jetbrains/bsp/bazel/projectview/model/sections/specific",
        "//src/main/java/org/jetbrains/bsp/bazel/projectview/model",
        "@maven//:com_google_guava_guava",
    ]
)
