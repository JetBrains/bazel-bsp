load("@rules_java//java:defs.bzl", "java_test")

java_test(
    name = "SourceRootGuesserTest",
    size = "small",
    srcs = ["SourceRootGuesserTest.java"],
    runtime_deps = [
        "@maven//:junit_junit",
    ],
    deps = [
        "//commons",
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/bsp/utils",
    ],
)

java_test(
    name = "InternalAspectsResolverTest",
    size = "small",
    srcs = ["InternalAspectsResolverTest.java"],
    runtime_deps = [
        "@maven//:junit_junit",
    ],
    deps = [
        "//bazelrunner/src/main/java/org/jetbrains/bsp/bazel/bazelrunner/data",
        "//commons",
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/bsp/utils",
    ],
)
