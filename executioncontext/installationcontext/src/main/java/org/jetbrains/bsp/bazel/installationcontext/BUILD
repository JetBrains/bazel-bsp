load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "installationcontext",
    srcs = glob(["*.java"]),
    visibility = [
        "//visibility:public"
    ],

    deps = [
        "//executioncontext/api",
        "@maven//:io_vavr_vavr",
        "//executioncontext/installationcontext/src/main/java/org/jetbrains/bsp/bazel/installationcontext/entities"
    ],
)
