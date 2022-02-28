load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "mappers",
    srcs = glob(["*.java"]),
    visibility = [
        "//executioncontext/api:__pkg__",
    ],
    exports = [
        "//projectview:model",
        "@maven//:io_vavr_vavr",
    ],
    deps = [
        "//projectview:model",
        "@maven//:io_vavr_vavr",
    ],
)
