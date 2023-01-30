load("//:junit5.bzl", "kt_junit5_test")

kt_junit5_test(
    name = "ProjectViewBuilderTest",
    size = "small",
    srcs = ["ProjectViewBuilderTest.kt"],
    test_package = "org.jetbrains.bsp.bazel.projectview.model",
    deps = [
        "//executioncontext/projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/model",
    ],
)
