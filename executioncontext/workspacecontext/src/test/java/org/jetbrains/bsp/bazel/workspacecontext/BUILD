load("//:junit5.bzl", "kt_junit5_test")

kt_junit5_test(
    name = "WorkspaceContextConstructorTest",
    size = "small",
    srcs = ["WorkspaceContextConstructorTest.kt"],
    test_package = "org.jetbrains.bsp.bazel.workspacecontext",
    deps = [
        "//executioncontext/workspacecontext/src/main/java/org/jetbrains/bsp/bazel/workspacecontext",
    ],
)

kt_junit5_test(
    name = "TargetsSpecMapperTest",
    size = "small",
    srcs = ["TargetsSpecMapperTest.kt"],
    associates = [
        "//executioncontext/workspacecontext/src/main/java/org/jetbrains/bsp/bazel/workspacecontext",
    ],
    test_package = "org.jetbrains.bsp.bazel.workspacecontext",
)
