load("@rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

kt_jvm_library(
    name = "managers",
    srcs = glob([
        "*.java",
        "*.kt",
    ]),
    visibility = ["//server:__subpackages__"],
    deps = [
        "//bazelrunner",
        "//commons",
        "//executioncontext/workspacecontext",
        "//server/src/main/kotlin/org/jetbrains/bsp/bazel/server/bep",
        "//server/src/main/kotlin/org/jetbrains/bsp/bazel/server/bsp/utils",
        "//server/src/main/kotlin/org/jetbrains/bsp/bazel/server/diagnostics",
        "@maven//:com_google_code_gson_gson",
        "@maven//:com_google_guava_guava",
        "@maven//:io_grpc_grpc_protobuf",
        "@maven//:org_apache_logging_log4j_log4j_api",
        "@maven//:org_apache_velocity_velocity_engine_core",
        "@maven//:org_eclipse_lsp4j_org_eclipse_lsp4j_jsonrpc",
    ],
)
