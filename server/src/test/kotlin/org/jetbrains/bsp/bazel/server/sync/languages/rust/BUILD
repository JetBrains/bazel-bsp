load("@//rules/kotlin:junit5.bzl", "kt_test")
load("@rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

kt_jvm_library(
    name = "RustTestUtils",
    srcs = ["RustTestUtils.kt"],
    visibility = ["//visibility:public"],
    deps = [
        "//server/src/main/kotlin/org/jetbrains/bsp/bazel/server/sync",
    ],
)

kt_test(
    name = "RustPackageResolverTest",
    size = "small",
    src = "RustPackageResolverTest.kt",
    deps = [
        "RustTestUtils",
        "//server/src/main/kotlin/org/jetbrains/bsp/bazel/server/sync",
    ],
)

kt_test(
    name = "RustDependencyResolverTest",
    size = "small",
    src = "RustDependencyResolverTest.kt",
    deps = [
        "RustTestUtils",
        "//server/src/main/kotlin/org/jetbrains/bsp/bazel/server/sync",
    ],
)

kt_test(
    name = "RustLanguagePluginTest",
    size = "small",
    src = "RustLanguagePluginTest.kt",
    deps = [
        "RustTestUtils",
        "//server/src/main/kotlin/org/jetbrains/bsp/bazel/server/sync",
    ],
)
