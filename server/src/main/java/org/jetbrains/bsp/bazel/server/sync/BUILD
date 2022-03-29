load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "sync",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//bazelrunner",
        "//bazelrunner/src/main/java/org/jetbrains/bsp/bazel/bazelrunner/utils",
        "//commons",
        "//executioncontext/projectview:model",
        "//logger",
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/bep",
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/bsp/info",
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/bsp/managers",
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/bsp/utils",
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/sync/proto:bsp_target_info_java_proto",
        "@com_google_protobuf//:protobuf_java",
        "@maven//:ch_epfl_scala_bsp4j",
        "@maven//:com_fasterxml_jackson_core_jackson_annotations",
        "@maven//:com_fasterxml_jackson_core_jackson_databind",
        "@maven//:com_google_guava_guava",
        "@maven//:io_vavr_vavr",
        "@maven//:io_vavr_vavr_jackson",
        "@maven//:org_eclipse_lsp4j_org_eclipse_lsp4j_jsonrpc",
    ],
)
