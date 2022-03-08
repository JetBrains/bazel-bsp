load("@rules_java//java:defs.bzl", "java_test")

java_test(
    name = "WorkspaceContextTargetsEntityMapperTest",
    size = "small",
    srcs = ["WorkspaceContextTargetsEntityMapperTest.java"],
    runtime_deps = [
        "@maven//:junit_junit",
    ],
    deps = [
        "//executioncontext/workspacecontext/src/main/java/org/jetbrains/bsp/bazel/workspacecontext/entries/mappers",
    ],
)
