load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "error",
    srcs = glob(["*.java"]),
    visibility = ["//server:__subpackages__"],
    deps = [
        "//commons",
        "@maven//:ch_epfl_scala_bsp4j",
        "@maven//:com_google_guava_guava",
        "@maven//:org_apache_logging_log4j_log4j_api",
        "@maven//:org_apache_logging_log4j_log4j_core",
    ],
)
