load("//:junit5.bzl", "java_junit5_test")

java_junit5_test(
    name = "BetterFilesTest",
    size = "small",
    srcs = ["BetterFilesTest.java"],
    test_package = "org.jetbrains.bsp.bazel.commons.test",
    deps = [
        "//commons/src/main/java/org/jetbrains/bsp/bazel/commons",
        "@maven//:io_vavr_vavr",
    ],
)
