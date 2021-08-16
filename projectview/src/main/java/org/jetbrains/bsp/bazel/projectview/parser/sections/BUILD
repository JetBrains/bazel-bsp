load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "sections",
    srcs = glob(["*.java"]),
    visibility = ["//projectview:__subpackages__"],
    deps = [
        "//projectview:model",
        "@maven//:com_google_guava_guava",
        "@maven//:org_apache_logging_log4j_log4j_api",
        "@maven//:org_apache_logging_log4j_log4j_core",
    ],
)
