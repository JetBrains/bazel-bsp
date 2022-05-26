load("//:junit5.bzl", "kt_junit5_test")

kt_junit5_test(
    name = "DependencyTreeTest",
    size = "small",
    srcs = ["DependencyTreeTest.kt"],
    test_package = "org.jetbrains.bsp.bazel.server.sync.dependencytree",
    deps = [
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/sync/dependencytree",
    ],
)
