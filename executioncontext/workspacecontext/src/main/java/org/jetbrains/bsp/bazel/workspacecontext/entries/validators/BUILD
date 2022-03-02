load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "validators",
    srcs = glob(["*.java"]),
    visibility = ["//executioncontext/workspacecontext:__subpackages__"],
    exports = [
        "//executioncontext/api",
        "//projectview:model",
        "@maven//:io_vavr_vavr",
    ],
    deps = [
        "//executioncontext/api",
        "//projectview:model",
        "@maven//:io_vavr_vavr",
        "@maven//:org_apache_logging_log4j_log4j_api",
        "@maven//:org_apache_logging_log4j_log4j_core",
    ],
)
