load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "commons",
    srcs = glob(["*.java"]),
    visibility = ["//commons:__subpackages__"],
    deps = [
        "@maven//:com_google_guava_guava",
    ],
)
