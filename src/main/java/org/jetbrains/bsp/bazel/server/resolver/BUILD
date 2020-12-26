load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "resolver",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/main/java/org/jetbrains/bsp/bazel/common",
        "//src/main/java/org/jetbrains/bsp/bazel/server/bazel",
        "//src/main/java/org/jetbrains/bsp/bazel/server/bazel/data:bazel-data",
        "//src/main/java/org/jetbrains/bsp/bazel/server/bazel/utils:bazel-utils",
        "//src/main/java/org/jetbrains/bsp/bazel/server/data",
        "//src/main/java/org/jetbrains/bsp/bazel/server/util",
        "@io_bazel//src/main/protobuf:analysis_java_proto",
        "@io_bazel//src/main/protobuf:build_java_proto",
        "@maven//:com_google_guava_guava",
        "@maven//:org_eclipse_lsp4j_org_eclipse_lsp4j_jsonrpc",
    ],
)
