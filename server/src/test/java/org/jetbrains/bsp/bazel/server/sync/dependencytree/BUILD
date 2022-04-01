load("//:junit5.bzl", "java_junit5_test")

java_junit5_test(
    name = "DependencyTreeTest",
    size = "small",
    srcs = ["DependencyTreeTest.java"],
    test_package = "org.jetbrains.bsp.bazel.server.sync.dependencytree",
    deps = [
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/sync/dependencytree",
    ],
)
