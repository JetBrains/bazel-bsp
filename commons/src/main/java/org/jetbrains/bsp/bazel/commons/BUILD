load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

kt_jvm_library(
    name = "commons",
    srcs = glob([
        "*.java",
        "*.kt",
    ]),
    visibility = ["//commons:__subpackages__"],
    deps = [
        "@maven//:com_github_zpp_This_is_fine_build_server_protocol_bsp4j",
        "@maven//:com_google_guava_guava",
    ],
)
