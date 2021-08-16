load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "splitter",
    srcs = glob(["*.java"]),
    visibility = ["//projectview:__subpackages__"],
    deps = [
        "@maven//:com_google_guava_guava",
    ],
)
