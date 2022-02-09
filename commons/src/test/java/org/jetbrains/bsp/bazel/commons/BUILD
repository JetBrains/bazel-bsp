load("@rules_java//java:defs.bzl", "java_test")

java_test(
    name = "BetterFilesTest",
    size = "small",
    srcs = ["BetterFilesTest.java"],
    runtime_deps = [
        "@maven//:junit_junit",
    ],
    deps = [
        "//commons/src/main/java/org/jetbrains/bsp/bazel/commons",
        "@maven//:io_vavr_vavr",
    ],
)
