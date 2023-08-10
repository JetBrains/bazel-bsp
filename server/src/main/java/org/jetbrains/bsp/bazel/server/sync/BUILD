load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

kt_jvm_library(
    name = "sync",
    srcs = glob([
        "**/*.java",
        "**/*.kt",
    ]),
    visibility = ["//visibility:public"],
    deps = [
        "//bazelrunner",
        "//bazelrunner/src/main/java/org/jetbrains/bsp/bazel/bazelrunner/utils",
        "//commons",
        "//executioncontext/workspacecontext",
        "//logger",
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/bep",
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/bsp/info",
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/bsp/managers",
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/bsp/utils",
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/sync/dependencytree",
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/sync/proto:bsp_target_info_java_proto",
        "@com_google_protobuf//:protobuf_java",
        "@maven//:com_github_agluszak_bsp_generators",
        "@maven//:com_fasterxml_jackson_core_jackson_annotations",
        "@maven//:com_fasterxml_jackson_core_jackson_databind",
        "@maven//:com_github_agluszak_jsonrpc4kt",
        "@maven//:org_jetbrains_kotlinx_kotlinx_coroutines_core",
        "@maven//:io_grpc_grpc_stub",
        "@maven//:org_jetbrains_kotlinx_kotlinx_serialization_json",
        "//:kotlin_serialization",
    ],
)
