load("@rules_java//java:defs.bzl", "java_test")

java_test(
    name = "SemanticVersionTest",
    size = "small",
    srcs = ["SemanticVersionTest.java"],
    resources = ["//src/test/resources:bsp-test-resources"],
    runtime_deps = [
        "@maven//:junit_junit",
    ],
    deps = [
        "//src/main/java/org/jetbrains/bsp/bazel/server/bazel/data",
    ],
)
