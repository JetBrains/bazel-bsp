load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

kt_jvm_library(
    name = "bsp",
    srcs = glob([
        "*.java",
        "*.kt",
    ]),
    visibility = ["//server:__subpackages__"],
    deps = [
        "//bazelrunner",
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/sync",
        "@io_bazel//third_party/grpc:grpc-jar",
        "@maven//:com_github_zpp_This_is_fine_build_server_protocol_bsp4j",
        "@maven//:org_apache_logging_log4j_log4j_api",
        "@maven//:org_apache_logging_log4j_log4j_core",
        "@maven//:org_eclipse_lsp4j_org_eclipse_lsp4j_jsonrpc",
    ],
)
