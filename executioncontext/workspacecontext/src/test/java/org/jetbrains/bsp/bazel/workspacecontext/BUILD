load("//:junit5.bzl", "java_junit5_test")

java_junit5_test(
    name = "WorkspaceContextConstructorImplTest",
    size = "small",
    srcs = ["WorkspaceContextConstructorImplTest.java"],
    test_package = "org.jetbrains.bsp.bazel.workspacecontext",
    deps = [
        "//executioncontext/workspacecontext/src/main/java/org/jetbrains/bsp/bazel/workspacecontext",
    ],
)

java_junit5_test(
    name = "WorkspaceContextTest",
    size = "small",
    srcs = ["WorkspaceContextTest.java"],
    test_package = "org.jetbrains.bsp.bazel.workspacecontext",
    deps = [
        "//executioncontext/workspacecontext/src/main/java/org/jetbrains/bsp/bazel/workspacecontext",
    ],
)
