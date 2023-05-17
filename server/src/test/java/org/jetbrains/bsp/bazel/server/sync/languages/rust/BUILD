load("//:junit5.bzl", "kt_junit5_test")
load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

kt_jvm_library(
    name = "RustTestUtils",
    srcs = ["RustTestUtils.kt"],
    visibility = ["//visibility:public"],
    deps = [
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/sync",
    ],
)


kt_junit5_test(
    name = "RustWorkspaceResolverTest",
    size = "small",
    srcs = ["RustWorkspaceResolverTest.kt"],
    test_package = "org.jetbrains.bsp.bazel.server.sync.languages.rust",
    deps = [
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/sync",
        "RustTestUtils",
    ],
)

kt_junit5_test(
    name = "RustLanguagePluginTest",
    size = "small",
    srcs = ["RustLanguagePluginTest.kt"],
    test_package = "org.jetbrains.bsp.bazel.server.sync.languages.rust",
    deps = [
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/sync",
        "RustTestUtils",
    ],
)
