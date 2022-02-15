load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "model",
    srcs = glob(["*.java"]),
    visibility = ["//projectview:__subpackages__"],
    exports = [
        "//projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/model/sections",
        "@maven//:io_vavr_vavr",
    ],
    deps = [
        "//commons",
        "//projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/model/sections",
        "@maven//:com_google_guava_guava",
        "@maven//:io_vavr_vavr",
    ],
)
