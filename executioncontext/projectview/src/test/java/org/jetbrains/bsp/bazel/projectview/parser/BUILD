load("@rules_java//java:defs.bzl", "java_library")
load("//:junit5.bzl", "java_junit5_test")

java_junit5_test(
    name = "ProjectViewParserImplTest",
    size = "small",
    srcs = ["ProjectViewParserImplTest.java"],
    resources = ["//executioncontext/projectview/src/test/resources:projectview"],
    test_package = "org.jetbrains.bsp.bazel.projectview.parser",
    deps = [
        "//executioncontext/projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/parser",
        "//executioncontext/projectview/src/test/java/org/jetbrains/bsp/bazel/projectview/parser:parser_mock_test_impl",
    ],
)

java_library(
    name = "parser_mock_test_impl",
    testonly = 1,
    srcs = ["ProjectViewParserMockTestImpl.java"],
    deps = [
        "//executioncontext/projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/parser",
        "@maven//:com_google_guava_guava",
    ],
)
