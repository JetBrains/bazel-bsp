load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "parsers",
    srcs = glob(["*.java"]),
    resources = ["//src/main/resources:bsp-main-resources"],
    visibility = ["//visibility:public"],
    deps = [
        "@maven//:com_google_guava_guava",
    ],
)
