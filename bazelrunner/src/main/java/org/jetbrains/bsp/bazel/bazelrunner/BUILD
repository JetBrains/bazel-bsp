load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "bazelrunner",
    srcs = glob(["*.java"]),
    resources = ["//log4j_config"],
    visibility = ["//bazelrunner:__subpackages__"],
    deps = [
        "//bazelrunner/src/main/java/org/jetbrains/bsp/bazel/bazelrunner/outputs",
        "//bazelrunner/src/main/java/org/jetbrains/bsp/bazel/bazelrunner/params",
        "//bazelrunner/src/main/java/org/jetbrains/bsp/bazel/bazelrunner/utils",
        "//commons",
        "//logger",
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/bsp/info",
        "@maven//:ch_epfl_scala_bsp4j",
        "@maven//:com_fasterxml_jackson_core_jackson_annotations",
        "@maven//:com_fasterxml_jackson_core_jackson_databind",
        "@maven//:com_google_guava_guava",
        "@maven//:io_vavr_vavr",
        "@maven//:org_apache_logging_log4j_log4j_api",
        "@maven//:org_apache_logging_log4j_log4j_core",
        "@maven//:org_eclipse_lsp4j_org_eclipse_lsp4j_jsonrpc",
    ],
)
