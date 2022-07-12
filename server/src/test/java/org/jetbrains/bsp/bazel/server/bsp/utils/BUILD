load("//:junit5.bzl", "java_junit5_test", "kt_junit5_test")

kt_junit5_test(
    name = "SourceRootGuesserTest",
    size = "small",
    srcs = ["SourceRootGuesserTest.kt"],
    test_package = "org.jetbrains.bsp.bazel.server.bsp.utils",
    deps = [
        "//commons",
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/bsp/utils",
    ],
)

java_junit5_test(
    name = "InternalAspectsResolverTest",
    size = "small",
    srcs = ["InternalAspectsResolverTest.java"],
    test_package = "org.jetbrains.bsp.bazel.server.bsp.utils",
    deps = [
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/bsp/info",
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/bsp/utils",
    ],
)
