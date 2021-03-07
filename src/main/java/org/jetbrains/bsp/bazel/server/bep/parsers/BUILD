load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "parsers",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    resources = ["//src/test/resources:bsp-test-resources"],
    deps = [
        "@maven//:com_google_guava_guava",
    ],
)
