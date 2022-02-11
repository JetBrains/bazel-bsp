load("@rules_java//java:defs.bzl", "java_test")

java_test(
    name = "ProjectViewTest",
    size = "small",
    srcs = ["ProjectViewTest.java"],
    runtime_deps = [
        "@maven//:junit_junit",
    ],
    deps = [
        "//projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/model",
    ],
)
