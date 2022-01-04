load("@rules_java//java:defs.bzl", "java_test")

java_test(
    name = "FileUtilsTest",
    size = "small",
    srcs = ["FileUtilsTest.java"],
    runtime_deps = [
        "@maven//:junit_junit",
    ],
    deps = [
        "//commons/src/main/java/org/jetbrains/bsp/bazel/commons",
    ],
)
