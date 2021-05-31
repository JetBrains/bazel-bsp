load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "resolvers",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/main/java/org/jetbrains/bsp/bazel/commons",
        "//src/main/java/org/jetbrains/bsp/bazel/projectview/model",
        "//src/main/java/org/jetbrains/bsp/bazel/projectview/model/sections/specific",
        "//src/main/java/org/jetbrains/bsp/bazel/server/bazel",
        "//src/main/java/org/jetbrains/bsp/bazel/server/bazel/data",
        "//src/main/java/org/jetbrains/bsp/bazel/server/bazel/params",
        "//src/main/java/org/jetbrains/bsp/bazel/server/bazel/utils",
        "//src/main/java/org/jetbrains/bsp/bazel/server/bsp/resolvers/actiongraph",
        "//src/main/java/org/jetbrains/bsp/bazel/server/bsp/utils",
        "@io_bazel//src/main/protobuf:analysis_java_proto",
        "@io_bazel//src/main/protobuf:analysis_v2_java_proto",
        "@io_bazel//src/main/protobuf:build_java_proto",
        "@maven//:ch_epfl_scala_bsp4j",
        "@maven//:com_google_guava_guava",
        "@maven//:org_eclipse_lsp4j_org_eclipse_lsp4j_jsonrpc",
    ],
)
