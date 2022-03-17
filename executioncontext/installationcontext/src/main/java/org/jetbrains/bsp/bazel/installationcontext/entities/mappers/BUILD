load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "mappers",
    srcs = glob(["*.java"]),
    visibility = [
        "//visibility:public",
    ],
    exports = [
        "//executioncontext/api",
        "//executioncontext/installationcontext/src/main/java/org/jetbrains/bsp/bazel/installationcontext/entities",
        "//executioncontext/projectview:model",
        "@maven//:io_vavr_vavr",
    ],
    deps = [
        "//executioncontext/api",
        "//executioncontext/installationcontext/src/main/java/org/jetbrains/bsp/bazel/installationcontext/entities",
        "//executioncontext/projectview:model",
        "@maven//:io_vavr_vavr",
        "@maven//:org_apache_logging_log4j_log4j_api",
        "@maven//:org_apache_logging_log4j_log4j_core",
    ],
)
