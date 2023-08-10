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
        "@maven//:com_github_agluszak_bsp_generators",
        "@maven//:io_vavr_vavr",
        "@maven//:org_apache_logging_log4j_log4j_api",
        "@maven//:org_apache_logging_log4j_log4j_core",
        "@maven//:com_github_agluszak_jsonrpc4kt",
        "//:kotlin_serialization",
    ],
)
