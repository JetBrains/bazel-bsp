load("//:junit5.bzl", "java_junit5_test")

java_junit5_test(
    name = "ProjectStorageTest",
    size = "small",
    srcs = ["ProjectStorageTest.java"],
    test_package = "org.jetbrains.bsp.bazel.server.sync",
    deps = [
        "//logger",
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/sync",
        "@maven//:io_vavr_vavr",
    ],
)
