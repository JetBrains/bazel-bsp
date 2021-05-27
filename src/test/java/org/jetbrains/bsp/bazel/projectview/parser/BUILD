load("@rules_java//java:defs.bzl", "java_test")

java_test(
    name = "ProjectViewParserImplTest",
    size = "small",
    srcs = ["ProjectViewParserImplTest.java"],
    resources = ["//src/test/resources:bsp-test-resources"],
    deps = [
        "//src/main/java/org/jetbrains/bsp/bazel/projectview/parser",
        "//src/main/java/org/jetbrains/bsp/bazel/projectview/model",
        "//src/main/java/org/jetbrains/bsp/bazel/projectview/model/sections",
        "//src/main/java/org/jetbrains/bsp/bazel/projectview/model/sections/specific",
    ],
    runtime_deps = [
        "@maven//:junit_junit",
    ]
)
