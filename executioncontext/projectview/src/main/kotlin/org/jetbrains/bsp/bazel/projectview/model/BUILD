load("@rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

kt_jvm_library(
    name = "model",
    srcs = glob(["*.kt"]),
    visibility = [
        "//executioncontext/projectview:__pkg__",
        "//executioncontext/projectview/src/main/kotlin/org/jetbrains/bsp/bazel/projectview:__subpackages__",
        "//executioncontext/projectview/src/test/kotlin/org/jetbrains/bsp/bazel/projectview/model:__pkg__",
    ],
    exports = [
        "//executioncontext/projectview/src/main/kotlin/org/jetbrains/bsp/bazel/projectview/model/sections",
    ],
    deps = [
        "//commons",
        "//executioncontext/projectview/src/main/kotlin/org/jetbrains/bsp/bazel/projectview/model/sections",
        "@maven//:org_apache_logging_log4j_log4j_api",
        "@maven//:org_apache_logging_log4j_log4j_core",
    ],
)
