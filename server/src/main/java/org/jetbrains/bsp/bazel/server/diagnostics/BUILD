load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

kt_jvm_library(
    name = "diagnostics",
    srcs = glob(["*.kt"]),
    visibility = ["//server:__subpackages__"],
    deps = [
        "//bazelrunner",
        "//commons",
        "@maven//:com_github_zpp_This_is_fine_build_server_protocol_bsp4j",
    ],
)
