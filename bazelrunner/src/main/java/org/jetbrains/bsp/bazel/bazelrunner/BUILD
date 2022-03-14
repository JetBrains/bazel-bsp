load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "bazelrunner",
    srcs = glob(["*.java"]),
    resources = ["//bazelrunner/src/main/resources"],
    visibility = ["//bazelrunner:__subpackages__"],
    deps = [
        "//bazelrunner/src/main/java/org/jetbrains/bsp/bazel/bazelrunner/data",
        "//bazelrunner/src/main/java/org/jetbrains/bsp/bazel/bazelrunner/params",
        "//bazelrunner/src/main/java/org/jetbrains/bsp/bazel/bazelrunner/utils",
        "//commons",
        "//logger",
        "@maven//:ch_epfl_scala_bsp4j",
        "@maven//:com_google_guava_guava",
        "@maven//:io_vavr_vavr",
        "@maven//:org_apache_logging_log4j_log4j_api",
        "@maven//:org_apache_logging_log4j_log4j_core",
        "@maven//:org_eclipse_lsp4j_org_eclipse_lsp4j_jsonrpc",
    ],
)
