load("@rules_java//java:defs.bzl", "java_binary", "java_library")

java_library(
    name = "server",
    srcs = glob(["*.java"]),
    visibility = ["//server:__subpackages__"],
    deps = [
        "//bazelrunner",
        "//commons",
        "//logger",
        "//projectview",
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/bep",
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/bsp",
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/bsp/config",
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/bsp/impl",
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/bsp/managers",
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/bsp/resolvers",
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/bsp/services",
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

java_binary(
    name = "bsp-run",
    main_class = "org.jetbrains.bsp.bazel.server.ServerInitializer",
    visibility = ["//server:__subpackages__"],
    runtime_deps = ["//server/src/main/java/org/jetbrains/bsp/bazel/server"],
)
