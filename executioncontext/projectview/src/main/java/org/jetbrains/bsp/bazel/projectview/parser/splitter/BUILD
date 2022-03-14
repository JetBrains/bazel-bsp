load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "splitter",
    srcs = glob(["*.java"]),
    visibility = [
        "//executioncontext/projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/parser:__subpackages__",
        "//executioncontext/projectview/src/test/java/org/jetbrains/bsp/bazel/projectview/parser/splitter:__pkg__",
    ],
    exports = [
        "@maven//:io_vavr_vavr",
    ],
    deps = [
        "@maven//:com_google_guava_guava",
        "@maven//:io_vavr_vavr",
        "@maven//:org_apache_commons_commons_collections4",
    ],
)
