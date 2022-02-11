load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "parser",
    srcs = glob(["*.java"]),
    visibility = ["//projectview:__subpackages__"],
    exports = [
        "//projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/model",
        "//projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/parser/sections",
    ],
    deps = [
        "//commons",
        "//projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/model",
        "//projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/parser/sections",
        "@maven//:com_google_guava_guava",
        "@maven//:io_vavr_vavr",
        "@maven//:org_apache_logging_log4j_log4j_api",
        "@maven//:org_apache_logging_log4j_log4j_core",
    ],
)
