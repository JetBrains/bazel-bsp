load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "entries",
    srcs = glob(["*.java"]),
    visibility = ["//executioncontext/workspacecontext:__subpackages__"],
    exports = [
        "//executioncontext/api",
    ],
    deps = [
        "//executioncontext/api",
    ],
)
