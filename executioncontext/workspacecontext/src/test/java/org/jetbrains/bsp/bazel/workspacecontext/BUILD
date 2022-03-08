load("@rules_java//java:defs.bzl", "java_test")

java_test(
    name = "WorkspaceContextConstructorImplTest",
    size = "small",
    srcs = ["WorkspaceContextConstructorImplTest.java"],
    runtime_deps = [
        "@maven//:junit_junit",
    ],
    deps = [
        "//executioncontext/workspacecontext/src/main/java/org/jetbrains/bsp/bazel/workspacecontext",
    ],
)

java_test(
    name = "WorkspaceContextTest",
    size = "small",
    srcs = ["WorkspaceContextTest.java"],
    runtime_deps = [
        "@maven//:junit_junit",
    ],
    deps = [
        "//executioncontext/workspacecontext/src/main/java/org/jetbrains/bsp/bazel/workspacecontext",
    ],
)
