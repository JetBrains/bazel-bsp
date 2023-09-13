load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

kt_jvm_library(
    name = "install",
    srcs = glob(["*.kt"]),
    resources = ["//aspects"],
    visibility = [
        "//bspcli:__subpackages__",
        "//e2e:__subpackages__",
        "//install:__subpackages__",
    ],
    deps = [
        "//commons",
        "//executioncontext/installationcontext",
        "//executioncontext/projectview:generator",
        "//executioncontext/projectview:parser",
        "//install/src/main/java/org/jetbrains/bsp/bazel/install/cli",
        "@maven//:com_github_zpp_This_is_fine_build_server_protocol_bsp4j",
        "@maven//:com_google_code_gson_gson",
        "@maven//:commons_cli_commons_cli",
    ],
)
