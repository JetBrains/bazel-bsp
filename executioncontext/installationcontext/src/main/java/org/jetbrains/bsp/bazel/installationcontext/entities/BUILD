load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "entities",
    srcs = glob(["*.java"]),
    visibility = [
        "//visibility:public",
    ],
    exports = [
        "@maven//:com_google_guava_guava",
    ],
    deps = [
        "//executioncontext/api",
        "@maven//:com_google_guava_guava",
    ],
)
