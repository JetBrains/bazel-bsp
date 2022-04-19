load("@rules_java//java:defs.bzl", "java_library")
load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

java_library(
    name = "model",
    srcs = glob(["*.java"]),
    visibility = [
        "//executioncontext/projectview:__pkg__",
        "//executioncontext/projectview/src/main/java/org/jetbrains/bsp/bazel/projectview:__subpackages__",
        "//executioncontext/projectview/src/test/java/org/jetbrains/bsp/bazel/projectview/model:__pkg__",
    ],
    exports = [
        "//executioncontext/projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/model:model_kotlin",
        "//executioncontext/projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/model/sections",
        "@maven//:io_vavr_vavr",
    ],
    deps = [
        "//commons",
        "//executioncontext/projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/model:model_kotlin",
        "//executioncontext/projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/model/sections",
        "@maven//:com_google_guava_guava",
        "@maven//:io_vavr_vavr",
        "@maven//:org_apache_logging_log4j_log4j_api",
        "@maven//:org_apache_logging_log4j_log4j_core",
    ],
)

kt_jvm_library(
    name = "model_kotlin",
    srcs = glob(["*.kt"]),
    visibility = [
        "//executioncontext/projectview:__pkg__",
        "//executioncontext/projectview/src/main/java/org/jetbrains/bsp/bazel/projectview:__subpackages__",
        "//executioncontext/projectview/src/test/java/org/jetbrains/bsp/bazel/projectview/model:__pkg__",
    ],
    exports = [
        "//executioncontext/projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/model/sections",
        "@maven//:io_vavr_vavr",
    ],
    deps = [
        "//commons",
        "//executioncontext/projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/model/sections",
        "@maven//:com_google_guava_guava",
        "@maven//:io_vavr_vavr",
        "@maven//:org_apache_logging_log4j_log4j_api",
        "@maven//:org_apache_logging_log4j_log4j_core",
    ],
)
