load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "api",
    srcs = glob(["*.java"]),
    visibility = ["//executioncontext/api:__pkg__"],
    exports = [
        "//executioncontext/projectview:model",
        "@maven//:io_vavr_vavr",
    ],
    deps = [
        "//executioncontext/projectview:model",
        "@maven//:io_vavr_vavr",
    ],
)
