load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "model",
    visibility = ["//visibility:public"],
    exports = [
        "//projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/model",
    ],
)

java_library(
    name = "parser",
    visibility = ["//visibility:public"],
    exports = [
        "//projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/model",
        "//projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/parser",
    ],
)

java_library(
    name = "projectview",
    visibility = ["//visibility:public"],
    exports = [
        "//projectview:model",
        "//projectview:parser",
    ],
)
