load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "bsp",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/main/java/org/jetbrains/bsp/bazel/commons",
        "//src/main/java/org/jetbrains/bsp/bazel/projectview/model",
        "//src/main/java/org/jetbrains/bsp/bazel/server/bazel",
        "//src/main/java/org/jetbrains/bsp/bazel/server/bazel/data",
        "//src/main/java/org/jetbrains/bsp/bazel/server/bazel/params",
        "//src/main/java/org/jetbrains/bsp/bazel/server/bep",
        "//src/main/java/org/jetbrains/bsp/bazel/server/bsp/config",
        "//src/main/java/org/jetbrains/bsp/bazel/server/bsp/managers",
        "//src/main/java/org/jetbrains/bsp/bazel/server/bsp/resolvers",
        "//src/main/java/org/jetbrains/bsp/bazel/server/bsp/utils",
        "@io_bazel//src/main/protobuf:build_java_proto",
        "@io_bazel//third_party/grpc:grpc-jar",
        "@maven//:ch_epfl_scala_bsp4j",
        "@maven//:com_google_code_gson_gson",
        "@maven//:com_google_guava_guava",
        "@maven//:org_apache_logging_log4j_log4j_api",
        "@maven//:org_apache_logging_log4j_log4j_core",
        "@maven//:org_eclipse_lsp4j_org_eclipse_lsp4j_jsonrpc",
        "@maven//:org_eclipse_xtext_org_eclipse_xtext_xbase_lib",
    ],
)
