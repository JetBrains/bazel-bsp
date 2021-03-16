load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "bazel",
    srcs = glob(["*.java"]),
    resources = ["//src/main/resources:bsp-main-resources"],
    visibility = ["//visibility:public"],
    deps = [
        "//src/main/java/org/jetbrains/bsp/bazel/commons",
        "//src/main/java/org/jetbrains/bsp/bazel/server/bazel/data",
        "//src/main/java/org/jetbrains/bsp/bazel/server/bazel/params",
        "//src/main/java/org/jetbrains/bsp/bazel/server/bazel/utils",
        "//src/main/java/org/jetbrains/bsp/bazel/server/loggers",
        "@io_bazel//src/main/protobuf:analysis_java_proto",
        "@io_bazel//src/main/protobuf:build_java_proto",
        "@maven//:ch_epfl_scala_bsp4j",
        "@maven//:com_google_guava_guava",
        "@maven//:org_apache_logging_log4j_log4j_api",
        "@maven//:org_apache_logging_log4j_log4j_core",
        "@maven//:org_eclipse_lsp4j_org_eclipse_lsp4j_jsonrpc",
    ],
)
