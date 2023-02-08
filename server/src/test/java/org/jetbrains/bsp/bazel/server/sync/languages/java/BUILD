load("//:junit5.bzl", "kt_junit5_test")

kt_junit5_test(
    name = "IdeClasspathResolverTest",
    size = "small",
    srcs = ["IdeClasspathResolverTest.kt"],
    test_package = "org.jetbrains.bsp.bazel.server.sync.languages.java",
    deps = [
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/sync",
    ],
)
