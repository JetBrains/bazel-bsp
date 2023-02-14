load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

kt_jvm_library(
    name = "base",
    srcs = glob(["*.kt"]),
    resources = ["//e2e/src/main/resources:bsp-e2e-resources"],
    visibility = ["//e2e:__subpackages__"],
    deps = [
        "@maven//:com_github_zpp_This_is_fine_build_server_protocol_bsp4j",
        "@maven//:org_apache_logging_log4j_log4j_api",
        "@maven//:org_apache_logging_log4j_log4j_core",
        "@testkit//:bazel-testkit-client",
    ],
)
