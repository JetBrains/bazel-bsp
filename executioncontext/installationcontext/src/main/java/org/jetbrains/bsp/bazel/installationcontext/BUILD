load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "installationcontext",
    srcs = glob(["*.java"]),
    visibility = [
        "//visibility:public",
    ],
    exports = [
        "//executioncontext/api",
        "//executioncontext/installationcontext/src/main/java/org/jetbrains/bsp/bazel/installationcontext/entities",
        "@maven//:io_vavr_vavr",
    ],
    deps = [
        "//executioncontext/api",
        "//executioncontext/installationcontext/src/main/java/org/jetbrains/bsp/bazel/installationcontext/entities",
        "//executioncontext/installationcontext/src/main/java/org/jetbrains/bsp/bazel/installationcontext/entities/mappers",
        "@maven//:io_vavr_vavr",
    ],
)
