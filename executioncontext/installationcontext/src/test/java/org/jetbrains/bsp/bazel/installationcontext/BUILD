load("@rules_java//java:defs.bzl", "java_test")

java_test(
    name = "InstallationContextTest",
    size = "small",
    srcs = ["InstallationContextTest.java"],
    runtime_deps = [
        "@maven//:junit_junit",
    ],
    deps = [
        "//executioncontext/installationcontext/src/main/java/org/jetbrains/bsp/bazel/installationcontext",
    ],
)

java_test(
    name = "InstallationContextConstructorTest",
    size = "small",
    srcs = ["InstallationContextConstructorTest.java"],
    runtime_deps = [
        "@maven//:junit_junit",
    ],
    deps = [
        "//executioncontext/installationcontext/src/main/java/org/jetbrains/bsp/bazel/installationcontext",
    ],
)
