load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "config",
    srcs = glob(["*.java"]),
    visibility = ["//server:__subpackages__"],
    deps = [
        "//projectview:model",
        "@maven//:com_google_guava_guava",
    ],
)
