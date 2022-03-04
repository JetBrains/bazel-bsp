load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "entries",
    srcs = glob(["*.java"]),
    visibility = [
        "//executioncontext/workspacecontext:__pkg__",
        "//executioncontext/workspacecontext/src/main/java/org/jetbrains/bsp/bazel/workspacecontext:__subpackages__",
    ],
    exports = [
        "//executioncontext/api",
    ],
    deps = [
        "//executioncontext/api",
        "@maven//:org_apache_commons_commons_collections4",
    ],
)
