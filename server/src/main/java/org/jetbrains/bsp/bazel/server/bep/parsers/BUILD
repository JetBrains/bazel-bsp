load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "parsers",
    srcs = glob(["*.java"]),
    resources = ["//server/src/main/resources"],
    visibility = ["//server/src/main/java/org/jetbrains/bsp/bazel/server/bep:__subpackages__"],
    deps = [
        "@maven//:com_google_guava_guava",
    ],
)
