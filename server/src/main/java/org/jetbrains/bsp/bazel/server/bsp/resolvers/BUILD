load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "resolvers",
    srcs = glob(["*.java"]),
    visibility = ["//server:__subpackages__"],
    deps = [
        "//bazelrunner",
        "//commons",
        "//executioncontext/projectview:model",
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/bsp/resolvers/actiongraph",
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/bsp/utils",
        "@io_bazel//src/main/protobuf:analysis_java_proto",
        "@io_bazel//src/main/protobuf:analysis_v2_java_proto",
        "@io_bazel//src/main/protobuf:build_java_proto",
        "@maven//:ch_epfl_scala_bsp4j",
        "@maven//:com_google_guava_guava",
        "@maven//:org_eclipse_lsp4j_org_eclipse_lsp4j_jsonrpc",
    ],
)
