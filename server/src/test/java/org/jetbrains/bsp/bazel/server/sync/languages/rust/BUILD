load("//:junit5.bzl", "kt_junit5_test")

kt_junit5_test(
    name = "RustWorkspaceResolverTest",
    size = "small",
    srcs = ["RustWorkspaceResolverTest.kt"],
    test_package = "org.jetbrains.bsp.bazel.server.sync.languages.rust",
    deps = [
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/sync",
    ],
)
