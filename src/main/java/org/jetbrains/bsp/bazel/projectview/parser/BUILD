load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "parser",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/main/java/org/jetbrains/bsp/bazel/commons",
        "//src/main/java/org/jetbrains/bsp/bazel/projectview/model",
        "//src/main/java/org/jetbrains/bsp/bazel/projectview/model/sections",
        "//src/main/java/org/jetbrains/bsp/bazel/projectview/model/sections/specific",
        "//src/main/java/org/jetbrains/bsp/bazel/projectview/parser/sections",
        "//src/main/java/org/jetbrains/bsp/bazel/projectview/parser/sections/specific",
        "//src/main/java/org/jetbrains/bsp/bazel/projectview/parser/splitter",
        "@maven//:com_google_guava_guava",
        "@maven//:io_vavr_vavr",
        "@maven//:org_apache_logging_log4j_log4j_api",
        "@maven//:org_apache_logging_log4j_log4j_core",
    ],
)
