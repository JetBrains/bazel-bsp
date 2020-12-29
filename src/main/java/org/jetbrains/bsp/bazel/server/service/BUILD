load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "service",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/main/java/org/jetbrains/bsp/bazel/common",
        "//src/main/java/org/jetbrains/bsp/bazel/server/bep",
        "//src/main/java/org/jetbrains/bsp/bazel/server/bsp",
        "//src/main/java/org/jetbrains/bsp/bazel/server/data",
        "//src/main/java/org/jetbrains/bsp/bazel/server/logger",
        "//src/main/java/org/jetbrains/bsp/bazel/server/resolver",
        "//src/main/java/org/jetbrains/bsp/bazel/server/util",
        "@io_bazel//src/main/protobuf:build_java_proto",
        "@io_bazel//third_party/grpc:grpc-jar",
        "@maven//:ch_epfl_scala_bsp4j",
        "@maven//:com_google_guava_guava",
        "@maven//:org_eclipse_lsp4j_org_eclipse_lsp4j_jsonrpc",
    ],
)
