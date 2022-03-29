load("@rules_java//java:defs.bzl", "java_test")

java_test(
    name = "DependencyTreeTest",
    size = "small",
    srcs = ["DependencyTreeTest.java"],
    runtime_deps = [
        "@maven//:junit_junit",
    ],
    deps = [
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/sync/dependencytree",
        "@maven//:org_assertj_assertj_core",
    ],
)
