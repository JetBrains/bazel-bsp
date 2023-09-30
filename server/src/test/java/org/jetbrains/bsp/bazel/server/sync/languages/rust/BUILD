load("@//rules/kotlin:junit5.bzl", "kt_test")
load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

kt_jvm_library(
    name = "RustTestUtils",
    srcs = ["RustTestUtils.kt"],
    visibility = ["//visibility:public"],
    deps = [
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/sync",
    ],
)

kt_test(
    name = "RustPackageResolverTest",
    size = "small",
    src = "RustPackageResolverTest.kt",
    deps = [
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/sync",
        "RustTestUtils",
    ],
)

kt_test(
    name = "RustDependencyResolverTest",
    size = "small",
    src = "RustDependencyResolverTest.kt",
    deps = [
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/sync",
        "RustTestUtils",
    ],
)

kt_test(
    name = "RustLanguagePluginTest",
    size = "small",
    src = "RustLanguagePluginTest.kt",
    deps = [
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/sync",
        "RustTestUtils",
    ],
)
