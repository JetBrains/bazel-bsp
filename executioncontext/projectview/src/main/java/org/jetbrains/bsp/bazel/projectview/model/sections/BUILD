load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

kt_jvm_library(
    name = "sections",
    srcs = glob(["*.kt"]),
    visibility = [
        "//executioncontext/projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/model:__pkg__",
        "//executioncontext/projectview/src/test/java/org/jetbrains/bsp/bazel/projectview/model/sections:__pkg__",
    ],
    exports = [
        "@maven//:com_github_zpp_This_is_fine_build_server_protocol_bsp4j",
    ],
    deps = [
        "@maven//:com_github_zpp_This_is_fine_build_server_protocol_bsp4j",
    ],
)
