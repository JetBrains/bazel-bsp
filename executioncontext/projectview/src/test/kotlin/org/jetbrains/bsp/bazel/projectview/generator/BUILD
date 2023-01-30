load("//:junit5.bzl", "kt_junit5_test")

kt_junit5_test(
    name = "DefaultProjectViewGeneratorTest",
    size = "small",
    srcs = ["DefaultProjectViewGeneratorTest.kt"],
    test_package = "org.jetbrains.bsp.bazel.projectview.generator",
    deps = [
        "//executioncontext/projectview:parser",
        "//executioncontext/projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/generator",
        "//utils:dope",
    ],
)
