load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "commons",
    srcs = glob(["*.java"]),
    visibility = ["//commons:__subpackages__"],
    deps = [
        "@maven//:com_github_zpp_This_is_fine_build_server_protocol_bsp4j",
        "@maven//:com_google_guava_guava",
        "@maven//:io_vavr_vavr",
    ],
)
