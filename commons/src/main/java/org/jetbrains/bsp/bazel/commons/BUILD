load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "commons",
    srcs = glob(["*.java"]),
    visibility = ["//commons:__subpackages__"],
    deps = [
        "@maven//:com_github_agluszak_bsp_generators",
        "@maven//:com_google_guava_guava",
        "@maven//:io_vavr_vavr",
    ],
)
