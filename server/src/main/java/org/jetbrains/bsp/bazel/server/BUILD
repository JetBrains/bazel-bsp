load("@rules_java//java:defs.bzl", "java_binary", "java_library")

java_library(
    name = "server",
    srcs = glob(["*.java"]),
    visibility = ["//server:__subpackages__"],
    deps = [
        "//bazelrunner",
        "//commons",
        "//executioncontext/workspacecontext",
        "//logger",
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/bep",
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/bsp",
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/bsp/info",
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/bsp/managers",
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/bsp/services",
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/bsp/utils",
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/diagnostics",
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/sync",
        "@io_bazel//src/main/protobuf:build_java_proto",
        "@io_bazel//third_party/grpc:grpc-jar",
        "@maven//:ch_epfl_scala_bsp4j",
        "@maven//:com_google_guava_guava",
        "@maven//:io_vavr_vavr",
        "@maven//:org_apache_logging_log4j_log4j_api",
        "@maven//:org_apache_logging_log4j_log4j_core",
        "@maven//:org_eclipse_lsp4j_org_eclipse_lsp4j_jsonrpc",
    ],
)

java_binary(
    name = "bsp-run",
    main_class = "org.jetbrains.bsp.bazel.server.ServerInitializer",
    visibility = ["//server:__subpackages__"],
    runtime_deps = ["//server/src/main/java/org/jetbrains/bsp/bazel/server"],
)
